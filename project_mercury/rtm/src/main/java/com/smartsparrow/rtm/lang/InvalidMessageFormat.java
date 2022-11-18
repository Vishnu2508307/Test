package com.smartsparrow.rtm.lang;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * An exception to represent an invalid message format
 */
public class InvalidMessageFormat extends IllegalArgumentException {

    private String messageType;
    private List<String> fields = new ArrayList<>(); //fields name which can't be parsed
    private String originalMessage; //original error message from json parser - for logging purpose only, it should not be shown to the user
    private String replyTo;

    public InvalidMessageFormat(String messageType, InvalidFormatException cause, String replyTo) {
        super("Invalid message format", cause);
        this.messageType = messageType;
        this.originalMessage = cause.getMessage();
        List<JsonMappingException.Reference> paths = cause.getPath();
        for (JsonMappingException.Reference path : paths) {
            fields.add(path.getFieldName());
        }
        this.replyTo = replyTo;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public String getReplyTo() {
        return replyTo;
    }

    @Override
    public String getMessage() {
        if (!fields.isEmpty()) {
            return super.getMessage() + String.format(": '%s' has invalid format", fields);
        }
        return super.getMessage();
    }
}
