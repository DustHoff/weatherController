package gadget.weathercontroller.controller.comm;

import org.apache.http.client.HttpClient;

/**
 * Created by Dustin on 06.10.2015.
 */
public class Api {

    private static Api instance;

    private Api() {
    }

    public static Api call() {
        if (instance == null) instance = new Api();
        return instance;
    }

    public void setSkylightRGB(short red, short green, short blue) {

    }

    /**
     * @return new short[]{red,green,blue}
     */
    public short[] getSkylightRGB() {
        return null;
    }

    public void setCloudIntensitiy(int percent) {

    }

    public int getCloudIntensity() {
        return 0;
    }

    public int getRainIntensity() {
        return 0;
    }

    public void setRainIntensity(int percent) {

    }

    public void setCity(String name) {

    }

    public Object[] getAvailableCities() {
        return null;
    }

    public int getForecastHours() {
        return 0;
    }

    public void setForecastHours(int forecastHours) {

    }

    public String getApiKey() {
        return "";
    }

    public void setApiKey(String key) {

    }

    public String getWeatherURL() {
        return "";
    }

    public void setWeatherURL(String url) {

    }
}
