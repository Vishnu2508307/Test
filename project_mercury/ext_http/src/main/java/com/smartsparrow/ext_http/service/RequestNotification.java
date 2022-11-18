package com.smartsparrow.ext_http.service;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartsparrow.exception.IllegalStateFault;

public class RequestNotification implements Notification {

    private static final ObjectMapper mapper = new ObjectMapper();

    private NotificationState state;
    private ObjectNode params;

    public RequestNotification() {
    }

    @Override
    public NotificationState getState() {
        return state;
    }

    public RequestNotification setState(NotificationState state) {
        this.state = state;
        return this;
    }

    public ObjectNode getParams() {
        return params;
    }

    public RequestNotification setParams(ObjectNode params) {
        this.params = params;
        return this;
    }

    /*
     * Convenience methods
     */

    /**
     * Get the params as Json
     *
     * @return the params as Json
     * @throws IllegalStateFault raised when converting the params to JSON fails
     */
    @JsonIgnore
    public String getParamsAsJson() {
        try {
            return mapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new IllegalStateFault(e.getMessage());
        }
    }

    /**
     * Set the raw internal parameters
     *
     * @param paramsJson the string representation of the params
     * @return this
     */
    @JsonIgnore
    public RequestNotification setParamsFromJson(final String paramsJson) {
        try {
            setParams((ObjectNode) mapper.readTree(paramsJson));
        } catch (IOException e) {
            throw new IllegalStateFault(e.getMessage());
        }
        return this;
    }

    /*
     *
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestNotification that = (RequestNotification) o;
        return Objects.equals(state, that.state) &&
                Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, params);
    }

    @Override
    public String toString() {
        return "RequestNotification{" + "state=" + state + ", params=" + params + '}';
    }
}
