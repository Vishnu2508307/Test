package com.smartsparrow.ingestion.data;

import java.util.Objects;
import java.util.UUID;

public class IngestionEvent {

    private UUID id;
    private UUID ingestionId;
    private UUID projectId;
    private IngestionEventType eventType;
    private String code;
    private String message;
    private String error;
    private String action;
    private String location;

    public UUID getId() {
        return id;
    }

    public IngestionEvent setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getIngestionId() {
        return ingestionId;
    }

    public IngestionEvent setIngestionId(UUID ingestionId) {
        this.ingestionId = ingestionId;
        return this;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public IngestionEvent setProjectId(UUID projectId) {
        this.projectId = projectId;
        return this;
    }

    public IngestionEventType getEventType() {
        return eventType;
    }

    public IngestionEvent setEventType(final IngestionEventType eventType) {
        this.eventType = eventType;
        return this;
    }

    public String getCode() {
        return code;
    }

    public IngestionEvent setCode(String code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public IngestionEvent setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getError() {
        return error;
    }

    public IngestionEvent setError(String error) {
        this.error = error;
        return this;
    }

    public String getAction() {
        return action;
    }

    public IngestionEvent setAction(String action) {
        this.action = action;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public IngestionEvent setLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngestionEvent that = (IngestionEvent) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(ingestionId, that.ingestionId) &&
                Objects.equals(projectId, that.projectId) &&
                Objects.equals(eventType, that.eventType) &&
                Objects.equals(code, that.code) &&
                Objects.equals(message, that.message) &&
                Objects.equals(error, that.error) &&
                Objects.equals(action, that.action) &&
                Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ingestionId, projectId, eventType, code, message, error, action, location);
    }

    @Override
    public String toString() {
        return "IngestionEvent{" +
                "id=" + id +
                ", ingestionId=" + ingestionId +
                ", projectId=" + projectId +
                ", eventType='" + eventType + '\'' +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", error='" + error + '\'' +
                ", action='" + action + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}