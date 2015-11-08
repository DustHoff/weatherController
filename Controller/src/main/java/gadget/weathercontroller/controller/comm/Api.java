package gadget.weathercontroller.controller.comm;

import android.util.Log;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import gadget.component.api.data.*;
import gadget.component.hardware.data.CloudType;
import gadget.component.hardware.data.SkyLightType;
import gadget.component.job.owm.data.City;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by Dustin on 06.10.2015.
 */
public class Api {

    private static Api instance;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private WeatherResponse config;

    private Api() {
        gson = new Gson();
        httpClient = new OkHttpClient();
    }

    public static Api call() {
        if (instance == null) instance = new Api();
        return instance;
    }

    private Response analyse(com.squareup.okhttp.Response response) throws IOException, ApiException, ClassNotFoundException {
        String message = response.body().string();
        Log.d(getClass().getPackage().getName(), "Response: " + message);
        Response r = gson.fromJson(message, Response.class);
        Log.d(getClass().getPackage().getName(), "Code: " + r.getCode());
        Log.d(getClass().getPackage().getName(), "Type: " + r.getType());
        if (r.getCode() != 200) {
            Throwable t = (Throwable) r.convert();
            throw new ApiException(r.getCode(), t.getMessage());
        }
        return r;
    }
    private Response callGetRequest(String url) throws ApiException {
        if (!isAvailable()) throw new ApiException(500, "Device not available");
        try {
            Request request = new Request.Builder().url("http://weatherbox:8080" + url).build();
            Log.d(getClass().getPackage().getName(), "URL: " + request.urlString());

            com.squareup.okhttp.Response response = httpClient.newCall(request).execute();
            Log.d(getClass().getPackage().getName(), "Requesst successful: " + response.isSuccessful());
            if (response.isSuccessful()) {
                return analyse(response);
            } else throw new IOException("Something went wrong");
        } catch (IOException t) {
            Log.e(getClass().getPackage().getName(), "Problem while Get Request", t);
            throw new ApiException(500, t.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e(getClass().getPackage().getName(), "Problem while Get Request", e);
            throw new ApiException(500, e.getMessage());
        }
    }

    private Response callPostRequest(String url, Object data) throws ApiException {
        if (!isAvailable()) throw new ApiException(500, "Device not available");
        try {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, gson.toJson(data));
            Request request = new Request.Builder()
                    .url("http://weatherbox:8080" + url)
                    .post(body)
                    .build();
            Log.d(getClass().getPackage().getName(), "URL: " + request.urlString());
            com.squareup.okhttp.Response response = httpClient.newCall(request).execute();
            Log.d(getClass().getPackage().getName(), "Requesst successful: " + response.isSuccessful());
            if (response.isSuccessful()) {
                return analyse(response);
            } else throw new IOException(response.message());
        } catch (IOException t) {
            Log.e(getClass().getPackage().getName(), "Problem while Post Request", t);
            throw new ApiException(500, t.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e(getClass().getPackage().getName(), "Problem while Post Request", e);
            throw new ApiException(500, e.getMessage());
        }
    }

    private void loadConfig() throws ApiException {
        Response response = callGetRequest("/weather");
        try {
            config = (WeatherResponse) response.convert();
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    private WeatherRequest changeConfig() {
        WeatherRequest request = new WeatherRequest();
        request.setCity(config.getCity());
        request.setUseRain(config.isUseRain());
        request.setUseSky(config.isUseSky());
        request.setUseClouds(config.isUseClouds());
        request.setSkyled(config.getSkyled());
        request.setUrl(config.getUrl());
        request.setDlcity(config.getDlcity());
        request.setForecast(config.getForecast());
        request.setKey(config.getKey());
        return request;
    }

    /**
     * bevor diese Methode aufgerufen wird muss Api.call().disableWeatherUpdate() aufgerufen werden.
     * setzt den Farbton des Himmels
     *
     * @param red   Wert 0-255
     * @param green Wert 0-255
     * @param blue  Wert 0-255
     * @throws ApiException wird geworften wenn ein Problem mit der Kommunikation existert
     */
    public void setSkylightRGB(short red, short green, short blue) throws ApiException {
        AmbientRequest request = new AmbientRequest();
        request.setComponent("SkyLight");
        request.setValue(red + "," + green + "," + blue);
        Response response = callPostRequest("/ambient", request);
        try {
            boolean success = (Boolean) response.convert();
            if (!success) throw new ApiException(200, "Could not change Clouds");
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }


    /**
     * gibt den aktuellen Farbwert des simulierten Himmels an
     *
     * @return SkyLightType (enum) es gibt vordefinierte Werte. Jedoch gibt es auch SkyLightType.FADED dieser hat veraenderbare RGB Werte
     * @throws ApiException wird geworfen wenn ein Problem mit der Komminikation existiert
     */
    public SkyLightType getSkylightRGB() throws ApiException {
        Response response = callGetRequest("/ambient/Skylight");
        try {
            String[] split = ((String) response.convert()).split(",");
            SkyLightType type = SkyLightType.FADED;
            type.modify(Short.parseShort(split[0]), Short.parseShort(split[1]), Short.parseShort(split[2]));
            return type;
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    /**
     * bevor diese Methode aufgerufen wird muss Api.call().disableWeatherUpdate() aufgerufen werden.
     * setzt den Nebel/Wolken wert
     *
     * @param value
     * @throws ApiException
     */
    public void setCloudIntensitiy(CloudType value) throws ApiException {
        AmbientRequest request = new AmbientRequest();
        request.setComponent("Clouds");
        request.setValue(value.name());
        Response response = callPostRequest("/ambient", request);
        try {
            boolean success = (Boolean) response.convert();
            if (!success) throw new ApiException(200, "Could not change Clouds");
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    /**
     * gibt den aktuellen Nebel/Wolken wert zurueck
     *
     * @return CloudType
     * @throws ApiException
     */
    public CloudType getCloudIntensity() throws ApiException {
        Response response = callGetRequest("/ambient/Clouds");
        try {
            return CloudType.valueOf((String) response.convert());
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    /**
     * gibt den aktuellen Regen wert zurueck
     *
     * @return (0-3000)
     * @throws ApiException
     */
    public int getRainIntensity() throws ApiException {
        Response response = callGetRequest("/ambient/Rain");
        try {
            return Integer.parseInt((String) response.convert());
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    /**
     * bevor diese Methode aufgerufen wird muss Api.call().disableWeatherUpdate() aufgerufen werden.
     *
     * @param value
     * @throws ApiException
     */
    public void setRainIntensity(int value) throws ApiException {
        AmbientRequest request = new AmbientRequest();
        request.setComponent("Rain");
        request.setValue(value + "");
        Response response = callPostRequest("/ambient", request);
        try {
            boolean success = (Boolean) response.convert();
            if (!success) throw new ApiException(200, "Could not change Rain");
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    /**
     * setzt die aktuelle aktuelle stadt muss ein wert von Api.call().getAvailableCities(); sein
     *
     * @param city
     * @throws ApiException
     */
    public void setCity(City city) throws ApiException {
        loadConfig();
        WeatherRequest request = changeConfig();
        request.setCity(city.getName());
        Response reponse = callPostRequest("/weather", request);
        try {
            config = (WeatherResponse) reponse.convert();
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    /**
     * gibt eine Liste der zu unuterstuezenden Staedte von OpenWeatherMap zurueck
     *
     * @return
     * @throws ApiException
     */
    public List<City> getAvailableCities() throws ApiException {
        loadConfig();
        return config.getCities();
    }

    /**
     * gibt zurueck wieviele Stunden die Wettervorhersage nutzen soll
     *
     * @return
     * @throws ApiException
     */
    public int getForecastHours() throws ApiException {
        loadConfig();
        return config.getForecast();
    }

    /**
     * setzt den wert fuer die Wettervorhersage in Stunden
     *
     * @param forecastHours
     * @throws ApiException
     */
    public void setForecastHours(int forecastHours) throws ApiException {
        loadConfig();
        WeatherRequest request = changeConfig();
        request.setForecast(forecastHours);
        Response reponse = callPostRequest("/weather", request);
        try {
            config = (WeatherResponse) reponse.convert();
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    /**
     * gibt den ApiKey fuer OpenWeatherMap zurueck
     *
     * @return
     * @throws ApiException
     */
    public String getApiKey() throws ApiException {
        loadConfig();
        return config.getKey();
    }

    /**
     * setzt den ApiKey fuer OpenWeatherMap
     *
     * @param key
     * @throws ApiException
     */
    public void setApiKey(String key) throws ApiException {
        loadConfig();
        WeatherRequest request = changeConfig();
        request.setKey(key);
        Response reponse = callPostRequest("/weather", request);
        try {
            config = (WeatherResponse) reponse.convert();
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    /**
     * gibt die Webservice URL fuer OpenWeatherMap zurueck
     *
     * @return
     * @throws ApiException
     */
    public String getWeatherURL() throws ApiException {
        loadConfig();
        return config.getUrl();
    }

    /**
     * setzt die Webservice URL f+r OpenWeatherMap
     *
     * @param url
     * @throws ApiException
     */
    public void setWeatherURL(String url) throws ApiException {
        loadConfig();
        WeatherRequest request = changeConfig();
        request.setUrl(url);
        Response reponse = callPostRequest("/weather", request);
        try {
            config = (WeatherResponse) reponse.convert();
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }

    }

    /**
     * deaktiviert die automatischen Wetterupdates in der Box
     *
     * @throws ApiException
     */
    public void disableWeatherUpdate() throws ApiException {
        SysInfoRequest request = new SysInfoRequest();
        request.setMode(false);
        callPostRequest("/system", request);
    }

    /**
     * aktiviert die automatischen Wetterupdates in der Box
     *
     * @throws ApiException
     */
    public void enableWeatherUpdate() throws ApiException {
        SysInfoRequest request = new SysInfoRequest();
        request.setMode(true);
        callPostRequest("/system", request);
    }

    /**
     * gibt die SystemInfos zurueck
     *
     * @return
     * @throws ApiException
     */
    public SysInfoResponse getSystemInfo() throws ApiException {
        Response response = callGetRequest("/system");
        try {
            return (SysInfoResponse) response.convert();
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    public boolean isAvailable() {
        try {
            InetAddress.getByName("weatherbox");
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return false;
    }
}
