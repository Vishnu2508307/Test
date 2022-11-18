package com.smartsparrow.exception;

public class RestError {

    private int status;
    private String type;
    private String message;

    RestError(ErrorResponseType errorResponseType) {
        this.status = errorResponseType.getResponseStatusCode();
        this.type = errorResponseType.getType();
        this.message = errorResponseType.getMessage();
    }

    RestError(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    protected RestError setStatus(int status) {
        this.status = status;
        return this;
    }

    protected RestError setType(String type) {
        this.type = type;
        return this;
    }

    protected RestError setMessage(String message) {
        this.message = message;
        return this;
    }
}
