package cp.to4slpn.notifier.api.exception;

public final class AuthException extends Exception {
    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
