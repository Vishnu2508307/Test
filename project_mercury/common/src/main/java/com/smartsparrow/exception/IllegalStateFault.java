package com.smartsparrow.exception;

public class IllegalStateFault extends Fault {

    public IllegalStateFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return 500;
    }

    @Override
    public String getType() {
        return "INTERNAL_SERVER_ERROR";
    }
}
