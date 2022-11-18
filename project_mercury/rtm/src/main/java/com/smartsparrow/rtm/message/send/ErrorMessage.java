package com.smartsparrow.rtm.message.send;

import java.util.HashMap;
import java.util.Map;

import com.smartsparrow.rtm.message.ResponseMessageType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


public class ErrorMessage implements ResponseMessageType {
    @SuppressFBWarnings(value = "SS_SHOULD_BE_STATIC",
            justification = "Type is a field but with default value. It needs for correct serialization to JSON.")
    private String type = "error";

    private Integer code;
    private String message;
    private String replyTo;
    private Map<String, Object> context;

    public ErrorMessage() {
    }

    /**
     * Create an error message with a strong type. If the passed type is null the constructor keeps the generic
     * `error` type
     *
     * @param type the message type
     */
    public ErrorMessage(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

    public ErrorMessage setMessage(String message) {
        this.message = message;
        return this;
    }

    public ErrorMessage setCode(Integer code) {
        this.code = code;
        return this;
    }

    @Override
    public String getReplyTo() {
        return replyTo;
    }

    public ErrorMessage setReplyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public ErrorMessage setContext(Map<String, Object> context) {
        this.context = context;
        return this;
    }

    public ErrorMessage addContext(String key, Object value) {
        if (context == null) {
            context = new HashMap<>();
        }
        context.put(key, value);
        return this;
    }
}
