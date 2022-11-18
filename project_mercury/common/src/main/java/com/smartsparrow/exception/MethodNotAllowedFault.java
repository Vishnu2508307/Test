package com.smartsparrow.exception;

public class MethodNotAllowedFault extends Fault {

    public MethodNotAllowedFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return 405;
    }

    @Override
    public String getType() {
        return "METHOD_NOT_ALLOWED";
    }
}
