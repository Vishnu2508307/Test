package com.smartsparrow.ext_http.service;

import java.util.Objects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This encapsulates an error which was caused by the underlying infrastructure or request parameters.
 *
 * It is not intended to handle Dead-Letter Queue error messages.
 */
@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson using field reflection, obj is immutable conceptually")
public class ErrorNotification implements Notification {

    private NotificationState state;
    private String error;

    public ErrorNotification() {
    }

    @Override
    public NotificationState getState() {
        return state;
    }

    public String getError() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorNotification that = (ErrorNotification) o;
        return Objects.equals(state, that.state) &&
                Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, error);
    }

    @Override
    public String toString() {
        return "ErrorNotification{" + "state=" + state + ", error='" + error + '\'' + '}';
    }
}
