package com.smartsparrow.rtm.lang;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;

/**
 * An exception to represent an unsupported message type.
 */
public class UnsupportedMessageType extends JsonProcessingException {

    private String type;
    private String replyTo;

    public UnsupportedMessageType(String type, String replyTo) {
        super(String.format("Unsupported message type %s", type));
        this.type = type;
        this.replyTo = replyTo;
    }

    public String getType() {
        return type;
    }

    public UnsupportedMessageType setType(String type) {
        this.type = type;
        return this;
    }

    public String getReplyTo() {
        return replyTo;
    }
}
