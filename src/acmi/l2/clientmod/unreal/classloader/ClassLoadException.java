package acmi.l2.clientmod.unreal.classloader;

public class ClassLoadException extends Exception {
    public ClassLoadException() {
        super();
    }

    public ClassLoadException(String message) {
        super(message);
    }

    public ClassLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClassLoadException(Throwable cause) {
        super(cause);
    }

    protected ClassLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
