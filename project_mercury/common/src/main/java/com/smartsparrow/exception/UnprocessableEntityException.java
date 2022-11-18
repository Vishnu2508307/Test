package com.smartsparrow.exception;

public class UnprocessableEntityException extends RuntimeException implements ErrorResponseType {

    public UnprocessableEntityException(String message) {
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
