package gadget.weathercontroller.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.*;
import gadget.component.api.data.SysInfoResponse;
import gadget.component.hardware.data.CloudType;
import gadget.weathercontroller.controller.comm.Api;
import gadget.weathercontroller.controller.comm.ApiException;


public class WeatherController extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener {

    private static Context context;
    private LinearLayout weatherInfo;
    private LinearLayout ambient;
    private int red;
    private int green;
    private int blue;
    private int rain;

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
        red.setOnSeekBarChangeListener(this);
        SeekBar green = (SeekBar) findViewById(R.id.green);
        green.setOnSeekBarChangeListener(this);
        SeekBar blue = (SeekBar) findViewById(R.id.blue);
        blue.setOnSeekBarChangeListener(this);
        SeekBar rain = (SeekBar) findViewById(R.id.rain);
        rain.setOnSeekBarChangeListener(this);
        Spinner mist = (Spinner) findViewById(R.id.mist);
        mist.setAdapter(new ArrayAdapter<CloudType>(this, android.R.layout.simple_spinner_item, CloudType.values()));
        mist.setOnItemSelectedListener(this);
    }

    @Override
    protected void onPostResume() {
        updateWeather();
        super.onPostResume();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        updateWeather();
        super.onPostCreate(savedInstanceState);
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
                Log.e(getClass().getPackage().getName(), e.getMessage());
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ambient.setVisibility(View.VISIBLE);
                    ambient.setAlpha(0f);
                    ambient.animate().alpha(1f).setDuration(1000).start();

                    ViewPropertyAnimator ani = weatherInfo.animate().alpha(0f).setDuration(1000);
                    ani.setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            weatherInfo.setVisibility(View.GONE);
                        }
                    });
                    ani.start();
                }
            });
        } else {
            try {
                Api.call().enableWeatherUpdate();
            } catch (ApiException e) {
                //nothing to do
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    weatherInfo.setVisibility(View.VISIBLE);
                    weatherInfo.setAlpha(0f);
                    weatherInfo.animate().alpha(1f).setDuration(1000).start();

                    ViewPropertyAnimator ani = ambient.animate().alpha(0f).setDuration(1000);
                    ani.setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ambient.setVisibility(View.GONE);
                        }
                    });
                    ani.start();
                }
            });
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) return;
        if (seekBar.getId() == R.id.red) red = progress;
        if (seekBar.getId() == R.id.green) green = progress;
        if (seekBar.getId() == R.id.blue) blue = progress;
        if (seekBar.getId() == R.id.rain) rain = progress;
        ImageView color = (ImageView) findViewById(R.id.weather);
        color.setBackgroundColor(Color.rgb(red, green, blue));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            Api.call().setSkylightRGB((short) red, (short) green, (short) blue);
            Api.call().setRainIntensity(rain);
        } catch (ApiException e) {
            Toast.makeText(this, R.string.notConnected, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        CloudType type = (CloudType) parent.getItemAtPosition(position);

        try {
            Api.call().setCloudIntensitiy(type);
        } catch (ApiException e) {
            Toast.makeText(this, R.string.notConnected, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
