package acmi.l2.clientmod.unreal;

public class UnrealException extends RuntimeException {
    public UnrealException() {
    }

    public UnrealException(String message) {
        super(message);
    }

    public UnrealException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnrealException(Throwable cause) {
        super(cause);
    }

    public UnrealException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
