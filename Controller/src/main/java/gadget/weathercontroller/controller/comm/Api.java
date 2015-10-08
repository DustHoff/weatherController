package gadget.weathercontroller.controller.comm;

import com.google.gson.Gson;
import gadget.component.api.data.AmbientRequest;
import gadget.component.api.data.Response;
import gadget.component.hardware.data.CloudType;
import gadget.component.hardware.data.SkyLightType;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.StringWriter;

/**
 * Created by Dustin on 06.10.2015.
 */
public class Api {

    private static Api instance;
    private final DefaultHttpClient httpClient;
    private final Gson gson;

    private Api() {
        gson = new Gson();
        httpClient = new DefaultHttpClient();
    }

    public static Api call() {
        if (instance == null) instance = new Api();
        return instance;
    }

    private Response callGetRequest(String url) throws ApiException {
        try {
            HttpGet post = new HttpGet(url);
            HttpResponse response = httpClient.execute(post);
            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntity().getContent(), writer);
            Response r = gson.fromJson(writer.toString(), Response.class);
            if (r.getCode() != 200) {
                Throwable t = (Throwable) r.convert();
                throw new ApiException(r.getCode(), t.getMessage());
            }
            return r;
        } catch (Throwable t) {
            throw new ApiException(500, t.getMessage());
        }
    }

    private Response callPostRequest(String url, Object data) throws ApiException {
        try {
            HttpPost post = new HttpPost(url);
            post.setEntity(new StringEntity(gson.toJson(data)));
            HttpResponse response = httpClient.execute(post);
            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntity().getContent(), writer);
            Response r = gson.fromJson(writer.toString(), Response.class);
            if (r.getCode() != 200) {
                Throwable t = (Throwable) r.convert();
                throw new ApiException(r.getCode(), t.getMessage());
            }
            return r;
        } catch (Throwable t) {
            throw new ApiException(500, t.getMessage());
        }
    }

    public void setSkylightRGB(short red, short green, short blue) throws ApiException {
        AmbientRequest request = new AmbientRequest();
        request.setComponent("SkyLight");
        request.setValue(red + "," + green + "," + blue);
        Response response = callPostRequest("http://weatherbox:8080/ambient", request);
        try {
            boolean success = (Boolean) response.convert();
            if (!success) throw new ApiException(200, "Could not change Clouds");
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }


    public SkyLightType getSkylightRGB() throws ApiException {
        Response response = callGetRequest("http://weatherbox:8080/ambient/Skylight");
        try {
            String[] split = ((String) response.convert()).split(",");
            SkyLightType type = SkyLightType.FADED;
            type.modify(Short.parseShort(split[0]), Short.parseShort(split[1]), Short.parseShort(split[2]));
            return type;
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    public void setCloudIntensitiy(CloudType value) throws ApiException {
        AmbientRequest request = new AmbientRequest();
        request.setComponent("Cloud");
        request.setComponent(value.name());
        Response response = callPostRequest("http://weatherbox:8080/ambient", request);
        try {
            boolean success = (Boolean) response.convert();
            if (!success) throw new ApiException(200, "Could not change Clouds");
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    public CloudType getCloudIntensity() throws ApiException {
        Response response = callGetRequest("http://weatherbox:8080/ambient/Clouds");
        try {
            return CloudType.valueOf((String) response.convert());
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    public int getRainIntensity() throws ApiException {
        Response response = callGetRequest("http://weatherbox:8080/ambient/Rain");
        try {
            return Integer.parseInt((String) response.convert());
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
    }

    public void setRainIntensity(int value) throws ApiException {
        AmbientRequest request = new AmbientRequest();
        request.setComponent("Rain");
        request.setValue(value + "");
        Response response = callPostRequest("http://weatherbox:8080/ambient", request);
        try {
            boolean success = (Boolean) response.convert();
            if (!success) throw new ApiException(200, "Could not change Rain");
        } catch (ClassNotFoundException e) {
            throw new ApiException(500, e.getMessage());
        }
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
