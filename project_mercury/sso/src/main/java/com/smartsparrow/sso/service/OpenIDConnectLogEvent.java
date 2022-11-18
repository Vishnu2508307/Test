package com.smartsparrow.sso.service;

import java.util.UUID;

import com.google.common.base.Objects;

/**
 * A concrete log event object.
 */
public class OpenIDConnectLogEvent {

    public enum Action {
        ERROR,
        START,
        RETRIEVE_METADATA,
        RETRIEVE_METADATA_RESULT,
        REDIRECT,
        PROCESS_CALLBACK,
        PROCESS_CALLBACK_PARAMETERS,
        TOKEN_REQUEST,
        TOKEN_RESPONSE,
        TOKEN_RESPONSE_OK,
        TOKEN_RESPONSE_OK_RESULT,
        TOKEN_RESPONSE_FAIL,
        JWT_CLAIM,
        ACCOUNT_PROVISIONED,
        ACCOUNT_LOCATED_BY_EMAIL,
        ACCOUNT_LOCATED_BY_FEDERATION,
        SUCCESS,
        START_LOGOUT,
        TOKEN_REVOKE_REQUEST,
        TOKEN_REVOKE_RESPONSE
    }

    private String sessionId;
    //
    private UUID id;
    private Action action;
    private String message;
    private Integer ttl;

    public OpenIDConnectLogEvent() {
    }

    public String getSessionId() {
        return sessionId;
    }

    public OpenIDConnectLogEvent setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public OpenIDConnectLogEvent setId(UUID id) {
        this.id = id;
        return this;
    }

    public Action getAction() {
        return action;
    }

    public OpenIDConnectLogEvent setAction(Action action) {
        this.action = action;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public OpenIDConnectLogEvent setMessage(String message) {
        this.message = message;
        return this;
    }

    public Integer getTtl() {
        return ttl;
    }

    public OpenIDConnectLogEvent setTtl(Integer ttl) {
        this.ttl = ttl;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OpenIDConnectLogEvent that = (OpenIDConnectLogEvent) o;
        return Objects.equal(sessionId, that.sessionId) && Objects.equal(id, that.id) && action == that.action
                && Objects.equal(message, that.message) && Objects.equal(ttl, that.ttl);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sessionId, id, action, message, ttl);
    }

    @Override
    public String toString() {
        return "OpenIDConnectLogEvent{" + "sessionId='" + sessionId + '\'' + ", id=" + id + ", action=" + action
                + ", message='" + message + '\'' + ", ttl=" + ttl + '}';
    }
}
