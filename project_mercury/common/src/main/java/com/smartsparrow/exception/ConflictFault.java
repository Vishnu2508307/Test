package com.smartsparrow.exception;

public class ConflictFault extends Fault {

    public ConflictFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return 409;
    }

    @Override
    public String getType() {
        return "CONFLICT";
    }
}
