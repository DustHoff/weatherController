package gadget.weathercontroller.controller.comm;

/**
 * Created by Dustin on 07.10.2015.
 */
public class ApiException extends Exception {

    private final int responseCode;
    private final String message;

    public ApiException(int responseCode, String message) {
        this.responseCode = responseCode;
        this.message = message;
    }
}
