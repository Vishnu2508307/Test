package com.smartsparrow.pubsub.subscriptions.activityIngestion;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.BroadcastMessage;

public class ActivityIngestionBroadcastMessage implements BroadcastMessage {
    private static final long serialVersionUID = 890864583550050529L;


    private final UUID projectId;
    private final UUID ingestionId;
    private final UUID rootElementId;
    private final Object ingestionStatus;

    public ActivityIngestionBroadcastMessage(UUID projectId, UUID ingestionId, UUID rootElementId, Object ingestionStatus) {
        this.projectId = projectId;
        this.ingestionId = ingestionId;
        this.rootElementId = rootElementId;
        this.ingestionStatus = ingestionStatus;
    }

    public UUID getProjectId() {
        return projectId;
    }
    public UUID getIngestionId() {
        return ingestionId;
    }

    public UUID getRootElementId() {
        return rootElementId;
    }

    public Object getIngestionStatus() {
        return ingestionStatus;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityIngestionBroadcastMessage that = (ActivityIngestionBroadcastMessage) o;
        return Objects.equals(projectId, that.projectId) &&
                Objects.equals(ingestionId, that.ingestionId) &&
                Objects.equals(rootElementId, that.rootElementId) &&
                ingestionStatus == that.ingestionStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, ingestionId, rootElementId, ingestionStatus);
    }

    @Override
    public String toString() {
        return "ActivityIngestionBroadcastMessage{" +
                ", projectId=" + projectId +
                ", ingestionId=" + ingestionId +
                ", rootElementId=" + rootElementId +
                ", ingestionStatus=" + ingestionStatus +
                '}';
    }
}
