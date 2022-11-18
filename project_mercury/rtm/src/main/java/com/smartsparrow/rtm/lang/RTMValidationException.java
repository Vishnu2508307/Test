package com.smartsparrow.rtm.lang;

import org.apache.http.HttpStatus;

public class RTMValidationException extends RTMWebSocketHandlerException {

    private String type;

    public RTMValidationException(String errorMessage, String replyTo, String type) {
        super(errorMessage, replyTo, HttpStatus.SC_BAD_REQUEST);
        this.type = type;
    }

    public String getErrorMessage() {
        return super.getErrorMessage();
    }

    public String getReplyTo() {
        return super.getReplyTo();
    }

    public String getType() {
        return type;
    }

    public int getStatusCode() {
        return super.getStatusCode();
    }
}
