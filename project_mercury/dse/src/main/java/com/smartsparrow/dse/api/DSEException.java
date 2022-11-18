package com.smartsparrow.dse.api;

public class DSEException extends Exception {

    public DSEException() {
    }

    public DSEException(String message) {
        super(message);
    }

    public DSEException(String message, Throwable cause) {
        super(message, cause);
    }
}
