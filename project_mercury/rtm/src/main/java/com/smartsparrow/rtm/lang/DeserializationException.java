package com.smartsparrow.rtm.lang;

public class DeserializationException extends Exception {

    private String errorMessage;
    private String replyTo;
    private String type = "error";
    private int statusCode;

    public DeserializationException(String errorMessage, String replyTo, int statusCode, String subType) {
        this.errorMessage = errorMessage;
        this.replyTo = replyTo;
        this.statusCode = statusCode;
        this.type = (subType != null ? String.format("%s.%s", subType, type) : type);
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
