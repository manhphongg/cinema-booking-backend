package vn.cineshow.exception;

public class AuthenticatedException extends RuntimeException {
    public AuthenticatedException(String message) {
        super(message);
    }

    public AuthenticatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
