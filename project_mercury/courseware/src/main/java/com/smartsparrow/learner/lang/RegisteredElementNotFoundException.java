package com.smartsparrow.learner.lang;

public class RegisteredElementNotFoundException extends RuntimeException {

    public RegisteredElementNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
