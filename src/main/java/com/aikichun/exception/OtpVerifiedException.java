package com.aikichun.exception;

public class OtpVerifiedException extends RuntimeException {
    public OtpVerifiedException(String message) {
        super(message);
    }

    public OtpVerifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
