package com.smartsparrow.rtm.message.send;

import java.util.Map;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A Response Message which will serialize null values which have been added.
 */
public class LiteralBasicResponseMessage extends BasicResponseMessage {

    public LiteralBasicResponseMessage() {
        super();
    }

    public LiteralBasicResponseMessage(String type, @Nullable String replyTo) {
        super(type, replyTo);
    }

    public LiteralBasicResponseMessage(String type, int code, String replyTo) {
        super(type, code, replyTo);
    }

    @JsonInclude //implies JsonInclude.Include.ALWAYS
    @Override
    public Map<String, Object> getResponse() {
        return super.getResponse();
    }
}
