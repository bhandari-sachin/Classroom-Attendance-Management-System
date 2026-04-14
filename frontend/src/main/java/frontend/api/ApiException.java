package frontend.api;

/**
 * Exception thrown when an API request fails.
 */
public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }

}