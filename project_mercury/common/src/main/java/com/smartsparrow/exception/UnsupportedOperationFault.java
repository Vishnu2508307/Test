package com.smartsparrow.exception;

public class UnsupportedOperationFault extends Fault {

    public UnsupportedOperationFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return 422;
    }

    @Override
    public String getType() {
        return "UNPROCESSABLE_ENTITY";
    }
}
