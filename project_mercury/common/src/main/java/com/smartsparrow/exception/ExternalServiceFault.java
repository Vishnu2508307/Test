package com.smartsparrow.exception;

public class ExternalServiceFault extends Fault {

    public ExternalServiceFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return 502;
    }

    @Override
    public String getType() {
        return "BAD_GATEWAY";
    }

}
