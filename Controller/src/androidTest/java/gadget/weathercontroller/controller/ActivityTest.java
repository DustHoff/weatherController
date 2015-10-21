package gadget.weathercontroller.controller;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;
import gadget.component.api.data.SysInfoResponse;
import gadget.component.hardware.data.CloudType;
import gadget.weathercontroller.controller.comm.Api;
import gadget.weathercontroller.controller.comm.ApiException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;

/**
 * Created by Dustin on 12.10.2015.
 */
public class ActivityTest extends ActivityInstrumentationTestCase2<WeatherController> {

    private WeatherController weatherController;

    public ActivityTest() {
        super(WeatherController.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        setActivityInitialTouchMode(true);
        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
        Api api = Mockito.mock(Api.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                throw new ApiException(500, "not Mocked");
            }
        });
        Mockito.doNothing().when(api).enableWeatherUpdate();
        Mockito.doNothing().when(api).disableWeatherUpdate();

        SysInfoResponse response = new SysInfoResponse();
        response.setUptime(1000);
        response.setMode(false);
        response.setClouds(CloudType.CLOUDY.name());
        response.setTemperature("10");
        response.setPrecipitation("10%");
        Mockito.doReturn(response).when(api).getSystemInfo();
        Field field = Api.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(api, api);
        weatherController = getActivity();
    }

    public void testCheckWeatherText() throws Throwable {
        Espresso.onView(ViewMatchers.withId(R.id.temperature)).check(ViewAssertions.matches(ViewMatchers.withText("10")));
        Espresso.onView(ViewMatchers.withId(R.id.clouds)).check(ViewAssertions.matches(ViewMatchers.withText(CloudType.CLOUDY.name())));
    }

    public void testChangeModeAmbient() throws Throwable {
        Espresso.onView(ViewMatchers.withId(R.id.info)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.ambient)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        Espresso.onView(ViewMatchers.withId(R.id.modeSwitch)).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.ambient)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.info)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }
}
