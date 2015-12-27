package gadget.weathercontroller.controller;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import gadget.component.api.data.Config;
import gadget.component.api.data.Weather;
import gadget.component.hardware.data.CloudType;
import gadget.component.hardware.data.SkyLightType;
import gadget.weatherbox.Client;

import java.util.Timer;
import java.util.TimerTask;


public class WeatherController extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, AdapterView.OnItemSelectedListener {

    private Client client = new Client("http://weatherbox:8080");
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

/*    @Override
    protected void onPostResume() {
        super.onPostResume();
        updateWeather();
    }*/

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //updateWeather();
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                try {
                    final Config config = client.getConfig();
                    final EditText owmUrl = (EditText) findViewById(R.id.owmUrl);
                    owmUrl.setText(config.getUrl());
                    EditText owmCityDL = (EditText) findViewById(R.id.owmCityDL);
                    owmCityDL.setText(config.getDlcity());
                    final EditText owmKey = (EditText) findViewById(R.id.owmKey);
                    owmKey.setText(config.getKey());
                    final EditText forecast = (EditText) findViewById(R.id.forecast);
                    forecast.setText(config.getForecast() + "");
                    CheckBox useClouds = (CheckBox) findViewById(R.id.useClouds);
                    useClouds.setChecked(config.isUseClouds());
                    CheckBox useSky = (CheckBox) findViewById(R.id.useSkylight);
                    useSky.setChecked(config.isUseSky());
                    CheckBox useRain = (CheckBox) findViewById(R.id.useRain);
                    useRain.setChecked(config.isUseRain());

                    //ArrayAdapter<City> adapter = new ArrayAdapter<City>(this, android.R.layout.simple_spinner_item, config.getCities());
                    //((Spinner) findViewById(R.id.selectedCity)).setAdapter(adapter);

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Configuration");
                    builder.setPositiveButton("save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            config.setKey(owmKey.getText().toString());
                            config.setUrl(owmUrl.getText().toString());
                            config.setForecast(Integer.parseInt(forecast.getText().toString()));
                        }
                    });
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    builder.setView(inflater.inflate(R.layout.dialog_configuration_dialog, null));
                    builder.create().show();
                } catch (Throwable e) {
                }
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void changeURL(String url) {
        client = new Client(url);
    }
}
