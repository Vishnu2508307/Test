package com.smartsparrow.learner.lang;

public class DataValidationException extends RuntimeException {

    public DataValidationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
