package gadget.weathercontroller.controller;

import android.app.Application;
import android.test.ApplicationTestCase;
import gadget.weathercontroller.controller.comm.Api;
import org.mockito.Mockito;

import java.lang.reflect.Field;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        Api api = Mockito.mock(Api.class);
        Field field = Api.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(api, api);
    }
}