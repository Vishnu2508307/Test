package com.smartsparrow.rtm.message.send;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.jetty.websocket.api.Session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.ResponseMessageType;
import com.smartsparrow.rtm.util.Responses;

/**
 * A basic response type message that is rendered on the wire as a top field named "response"
 */
public class BasicResponseMessage implements ResponseMessageType {

    private String type;
    private int code;
    private String replyTo;
    private Map<String, Object> response = new HashMap<>();

    /**
     * For testing purposes
     *
     * was added to be able to deserialize response message in tests
     */
    public BasicResponseMessage() {
    }

    /**
     * Build a basic response.
     *
     * @param type the type field of the message
     * @param replyTo the id field from the received message
     */
    public BasicResponseMessage(final String type, @Nullable final String replyTo) {
        this.type = type;
        this.replyTo = replyTo;
    }

    /**
     * Build a basic response.
     *
     * @param type the type field of the message
     * @param code the status code
     * @param replyTo the id field from the received message
     */
    public BasicResponseMessage(String type, int code, String replyTo) {
        this(type, replyTo);
        this.code = code;
    }

    /**
     * Write the message in an async manner to the session.
     *
     * @param session the WebSocket session to write to.
     */
    public void write(Session session) throws WriteResponseException {
        Responses.write(session, this);
    }

    @JsonProperty("replyTo")
    @Override
    public String getReplyTo() {
        return replyTo;
    }

    @Override
    public String getType() {
        return type;
    }

    @JsonIgnore
    public BasicResponseMessage addField(String key, Object value) {
        getResponse().put(key, value);
        return this;
    }

    @JsonIgnore
    public BasicResponseMessage addAllFields(final Map<String, ?> fields) {
        getResponse().putAll(fields);
        return this;
    }

    /**
     * The response field of the reply, contains all the set fields.
     *
     * @return a map of fields which encompass the response field of the message.
     */
    public Map<String, Object> getResponse() {
        return response;
    }

    public int getCode() {
        return code;
    }

    public BasicResponseMessage setCode(int code) {
        this.code = code;
        return this;
    }
}
