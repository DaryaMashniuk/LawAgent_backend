package by.masnhyuk.lawAgent.exception;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(String message, HttpStatus code) {
        super(message);
        this.status = code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
