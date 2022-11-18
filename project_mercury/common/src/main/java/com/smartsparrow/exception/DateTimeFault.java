package com.smartsparrow.exception;

public class DateTimeFault extends Fault {

    public DateTimeFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return 400;
    }

    @Override
    public String getType() {
        return "BAD_REQUEST";
    }
}
