package com.smartsparrow.rtm.lang;

import org.apache.http.HttpStatus;

public class RTMWebSocketHandlerException extends Exception {

    private String errorMessage;
    private String replyTo;
    private String type = "error";
    private int statusCode;

    public RTMWebSocketHandlerException(String errorMessage, String replyTo) {
        this.errorMessage = errorMessage;
        this.replyTo = replyTo;
        this.statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }

    public RTMWebSocketHandlerException(String errorMessage, String replyTo, int statusCode) {
        this(errorMessage, replyTo);
        this.statusCode = statusCode;
    }

    public RTMWebSocketHandlerException(String errorMessage, String replyTo, int statusCode, String subType) {
        this(errorMessage, replyTo, statusCode);
        this.type = String.format("%s.%s", subType, type);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getType() {
        return type;
    }
}
