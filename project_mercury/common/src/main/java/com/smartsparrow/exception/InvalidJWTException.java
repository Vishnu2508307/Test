package com.smartsparrow.exception;

public class InvalidJWTException extends Exception {

    private static final long serialVersionUID = 6424295345704681543L;

    public InvalidJWTException(String s) {
        super(s);
    }
}
