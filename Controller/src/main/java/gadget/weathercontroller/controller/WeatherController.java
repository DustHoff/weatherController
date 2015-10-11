package gadget.weathercontroller.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.*;
import gadget.component.api.data.SysInfoResponse;
import gadget.weathercontroller.controller.comm.Api;
import gadget.weathercontroller.controller.comm.ApiException;


public class WeatherController extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static Context context;
    private LinearLayout weatherInfo;
    private LinearLayout ambient;

    public static Context getContext() {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_weather_controller);
        Switch button = (Switch) findViewById(R.id.modeSwitch);
        button.setOnCheckedChangeListener(this);
        weatherInfo = (LinearLayout) findViewById(R.id.info);
        ambient = (LinearLayout) findViewById(R.id.ambient);
        SeekBar red = (SeekBar) findViewById(R.id.red);
        red.setOnSeekBarChangeListener(new AmbientPicker());
        SeekBar green = (SeekBar) findViewById(R.id.green);
        green.setOnSeekBarChangeListener(new AmbientPicker());
        SeekBar blue = (SeekBar) findViewById(R.id.blue);
        blue.setOnSeekBarChangeListener(new AmbientPicker());
        SeekBar rain = (SeekBar) findViewById(R.id.rain);
        rain.setOnSeekBarChangeListener(new AmbientPicker());
        Spinner mist = (Spinner) findViewById(R.id.mist);
        mist.setOnItemClickListener(new AmbientPicker());
    }

    @Override
    protected void onPostResume() {
        updateWeather();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        updateWeather();
    }

    private void updateWeather() {
        try {
            SysInfoResponse weather = Api.call().getSystemInfo();
            ((TextView) findViewById(R.id.temperature)).setText(weather.getTemperature());
            ((TextView) findViewById(R.id.precipitation)).setText(weather.getPrecipitation());
            ((TextView) findViewById(R.id.humidity)).setText(weather.getHumidity());
            ((TextView) findViewById(R.id.clouds)).setText(weather.getClouds());

        } catch (ApiException e) {
            Toast.makeText(this, R.string.notConnected, Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Crossfade
     *
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            try {
                Api.call().disableWeatherUpdate();
            } catch (ApiException e) {
                return;
            }
            ambient.setVisibility(View.VISIBLE);
            ambient.setAlpha(0f);
            ambient.animate().alpha(1f).setDuration(1000).setListener(null);

            weatherInfo.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    weatherInfo.setVisibility(View.GONE);
                }
            });
        } else {
            try {
                Api.call().enableWeatherUpdate();
            } catch (ApiException e) {
                //nothing to do
            }
            weatherInfo.setVisibility(View.VISIBLE);
            weatherInfo.setAlpha(0f);
            weatherInfo.animate().alpha(1f).setDuration(1000).setListener(null);

            ambient.animate().alpha(0f).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ambient.setVisibility(View.GONE);
                }
            });
        }
    }
}
