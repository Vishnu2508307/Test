package com.smartsparrow.ext_http.service;

import java.util.List;
import java.util.Objects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson using field reflection, obj is immutable conceptually")
public class ResultNotification implements Notification {

    private NotificationState state;
    private List<HttpEvent> result;

    public ResultNotification() {
    }

    @Override
    public NotificationState getState() {
        return state;
    }

    public List<HttpEvent> getResult() {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultNotification that = (ResultNotification) o;
        return Objects.equals(state, that.state) &&
                Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, result);
    }

    @Override
    public String toString() {
        return "ResultNotification{" + "state=" + state + ", result=" + result + '}';
    }
}
