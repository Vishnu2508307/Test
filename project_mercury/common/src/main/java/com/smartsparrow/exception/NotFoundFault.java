package com.smartsparrow.exception;

public class NotFoundFault extends Fault  {

    public NotFoundFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return 404;
    }

    @Override
    public String getType() {
        return "NOT_FOUND";
    }
}
