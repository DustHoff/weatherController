package gadget.weathercontroller.controller;

import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import gadget.component.api.data.Weather;
import gadget.component.hardware.data.CloudType;
import gadget.component.hardware.data.SkyLightType;
import gadget.weatherbox.Client;

import java.util.Timer;
import java.util.TimerTask;


public class WeatherController extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener {

    public static String URL = "http://weatherbox:8080";
    private Client client = new Client(URL);
    private LinearLayout weatherInfo;
    private LinearLayout ambient;
    private int red;
    private int green;
    private int blue;
    private int rain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateWeather();
            }
        }, 5000, 60000);
    }

    public void updateWeather() {
        final Weather weather = client.getWeather();
        if (weather == null) return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.temperature)).setText(weather.getTemperature());
                ((TextView) findViewById(R.id.precipitation)).setText(weather.getPrecipitation());
                ((TextView) findViewById(R.id.humidity)).setText(weather.getHumidity());
                ((TextView) findViewById(R.id.clouds)).setText(weather.getClouds());
            }
        });
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
            Log.i(getClass().getPackage().getName(), "disable Weather update");
            client.disableAutoUpdate();
            Log.i(getClass().getPackage().getName(), "update color seekbar position");
            final SkyLightType color = client.getSkyLight();
            ((SeekBar) findViewById(R.id.blue)).setProgress(color.getBlue());
            ((SeekBar) findViewById(R.id.green)).setProgress(color.getGreen());
            ((SeekBar) findViewById(R.id.red)).setProgress(color.getRed());

            Log.i(getClass().getPackage().getName(), "update rain seekbar position");
            ((SeekBar) findViewById(R.id.rain)).setProgress(client.getRain());

            final ImageView colorView = (ImageView) findViewById(R.id.SkyColor);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    colorView.setBackgroundColor(Color.rgb(color.getRed(), color.getGreen(), color.getBlue()));
                }
            });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ambient.setVisibility(View.VISIBLE);
                    weatherInfo.setVisibility(View.GONE);
                }
            });
        } else {
            client.enableAutoUpdate();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    weatherInfo.setVisibility(View.VISIBLE);
                    ambient.setVisibility(View.GONE);
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
        final ImageView color = (ImageView) findViewById(R.id.SkyColor);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                color.setBackgroundColor(Color.rgb(red, green, blue));
            }
        });
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        SkyLightType type = SkyLightType.FADED;
        type.modify((short) red, (short) green, (short) blue);
        client.setSkyLight(type);
        client.setRain(rain);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        CloudType type = (CloudType) parent.getItemAtPosition(position);
        client.setClouds(type);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            ConfigDialog.newInstance().show(getSupportFragmentManager(), "Config");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
