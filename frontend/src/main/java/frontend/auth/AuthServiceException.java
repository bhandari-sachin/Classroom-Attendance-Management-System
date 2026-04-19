package frontend.auth;

import java.io.IOException;

/**
 * Exception thrown when authentication service operations fail.
 */
public class AuthServiceException extends IOException {

    public AuthServiceException(String message) {
        super(message);
    }

    public AuthServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}