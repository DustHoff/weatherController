package gadget.weathercontroller.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import gadget.weathercontroller.controller.comm.Api;
import gadget.weathercontroller.controller.util.SystemUiHider;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class SplashScreen extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            private int retries = 0;
            @Override
            public void run() {
                if (Api.call().isAvailable()) {
                    Intent i = new Intent(SplashScreen.this, WeatherController.class);
                    startActivity(i);
                    exec.shutdown();
                    finish();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) findViewById(R.id.status)).setText("Connecting... retry " + retries);
                        }
                    });
                    retries++;
                    if (retries > 5) {
                        exec.shutdown();
                        finish();
                    }
                }
            }
        }, 1000, 500, TimeUnit.MILLISECONDS);
    }
}
