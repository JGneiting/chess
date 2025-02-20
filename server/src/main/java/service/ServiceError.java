package service;

public class ServiceError extends RuntimeException {
    private final int code;
    public ServiceError(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
