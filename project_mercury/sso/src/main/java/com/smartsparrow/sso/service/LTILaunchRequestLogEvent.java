package com.smartsparrow.sso.service;

import java.util.Objects;
import java.util.UUID;

public class LTILaunchRequestLogEvent {

    public enum Action {
        RECEIVED,
        ACCOUNT_PROVISIONED,
        ACCOUNT_LOCATED_BY_EMAIL,
        ACCOUNT_LOCATED_BY_FEDERATION,
        COMPLETED,
        ERROR
    }

    private UUID launchRequestId;
    //
    private UUID id;
    private Action action;
    private String message;

    public UUID getLaunchRequestId() {
        return launchRequestId;
    }

    public LTILaunchRequestLogEvent setLaunchRequestId(final UUID launchRequestId) {
        this.launchRequestId = launchRequestId;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public LTILaunchRequestLogEvent setId(final UUID id) {
        this.id = id;
        return this;
    }

    public Action getAction() {
        return action;
    }

    public LTILaunchRequestLogEvent setAction(final Action action) {
        this.action = action;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public LTILaunchRequestLogEvent setMessage(final String message) {
        this.message = message;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTILaunchRequestLogEvent that = (LTILaunchRequestLogEvent) o;
        return Objects.equals(launchRequestId, that.launchRequestId) &&
                Objects.equals(id, that.id) &&
                action == that.action &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(launchRequestId, id, action, message);
    }

    @Override
    public String toString() {
        return "LTILaunchRequestLogEvent{" +
                "launchRequestId=" + launchRequestId +
                ", id=" + id +
                ", action=" + action +
                ", message='" + message + '\'' +
                '}';
    }
}
