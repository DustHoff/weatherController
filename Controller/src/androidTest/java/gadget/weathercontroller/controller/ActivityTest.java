package gadget.weathercontroller.controller;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.SeekBar;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import gadget.component.hardware.data.SkyLightType;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Created by Dustin on 12.10.2015.
 */
public class ActivityTest extends ActivityInstrumentationTestCase2<WeatherController> {

    private WeatherController weatherController;
    private MockWebServer webServer;

    public ActivityTest() {
        super(WeatherController.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        setActivityInitialTouchMode(true);
        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
        webServer = new MockWebServer();
        webServer.start();
        weatherController = getActivity();
        weatherController.changeURL("http://" + webServer.getHostName() + ":" + webServer.getPort());
    }

    public void testCheckWeatherText() throws Throwable {
        webServer.enqueue(new MockResponse().setBody("{\n" +
                "  \"temperature\": \"10.73 - 12.29 celsius\",\n" +
                "  \"clouds\": \"few clouds %\",\n" +
                "  \"precipitation\": \"null null\",\n" +
                "  \"humidity\": \"81.0 %\"\n" +
                "}"));
        weatherController.updateWeather();
        Espresso.onView(ViewMatchers.withId(R.id.temperature)).check(ViewAssertions.matches(ViewMatchers.withText("10.73 - 12.29 celsius")));
        Espresso.onView(ViewMatchers.withId(R.id.clouds)).check(ViewAssertions.matches(ViewMatchers.withText("few clouds %")));
    }

    public void testChangeModeAmbient() throws Throwable {
        webServer.enqueue(new MockResponse().setBody("{\n" +
                "  \"city\": \"Osnabruck\",\n" +
                "  \"url\": \"http://api.openweathermap.org/data/2.5/forecast?mode\\u003dxml\\u0026APPID\\u003d$KEY\\u0026id\\u003d$CITY\",\n" +
                "  \"key\": \"828ce75931f9fef098daf3930139f6cd\",\n" +
                "  \"dlcity\": \"http://bulk.openweathermap.org/sample/city.list.json.gz\",\n" +
                "  \"forecast\": 0,\n" +
                "  \"skyled\": 40,\n" +
                "  \"useSky\": true,\n" +
                "  \"useClouds\": true,\n" +
                "  \"useRain\": true,\n" +
                "  \"delay\": 180,\n" +
                "  \"autoupdate\": true\n" +
                "}"));
        webServer.enqueue(new MockResponse().setBody("{\n" +
                "  \"city\": \"Osnabruck\",\n" +
                "  \"url\": \"http://api.openweathermap.org/data/2.5/forecast?mode\\u003dxml\\u0026APPID\\u003d$KEY\\u0026id\\u003d$CITY\",\n" +
                "  \"key\": \"828ce75931f9fef098daf3930139f6cd\",\n" +
                "  \"dlcity\": \"http://bulk.openweathermap.org/sample/city.list.json.gz\",\n" +
                "  \"forecast\": 0,\n" +
                "  \"skyled\": 40,\n" +
                "  \"useSky\": true,\n" +
                "  \"useClouds\": true,\n" +
                "  \"useRain\": true,\n" +
                "  \"delay\": 180,\n" +
                "  \"autoupdate\": false\n" +
                "}"));
        webServer.enqueue(new MockResponse().setBody("\"" + SkyLightType.DAY.getRed() + "," + SkyLightType.DAY.getGreen() + "," + SkyLightType.DAY.getBlue() + "\""));
        webServer.enqueue(new MockResponse().setBody("100"));
        Espresso.onView(ViewMatchers.withId(R.id.info)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.ambient)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        Espresso.onView(ViewMatchers.withId(R.id.modeSwitch)).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.ambient)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.info)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        Espresso.onView(ViewMatchers.withId(R.id.red)).check(ViewAssertions.matches(SeekbarPosition(SkyLightType.DAY.getRed())));
        Espresso.onView(ViewMatchers.withId(R.id.green)).check(ViewAssertions.matches(SeekbarPosition(SkyLightType.DAY.getGreen())));
        Espresso.onView(ViewMatchers.withId(R.id.blue)).check(ViewAssertions.matches(SeekbarPosition(SkyLightType.DAY.getBlue())));
        Espresso.onView(ViewMatchers.withId(R.id.rain)).check(ViewAssertions.matches(SeekbarPosition(100)));
    }

    private Matcher<View> SeekbarPosition(final int value) {
        return new BoundedMatcher<View, SeekBar>(SeekBar.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("Seekbar position exprected: " + value);
            }

            @Override
            protected boolean matchesSafely(SeekBar seekBar) {
                return seekBar.getProgress() == value;
            }
        };
    }
}
