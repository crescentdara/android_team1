package bitc.full502.spring.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
