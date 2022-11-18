package com.smartsparrow.courseware.eventmessage;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.eventmessage.EventMessage;

public class ActivityChangeLogEventMessage implements EventMessage<CoursewareChangeLogBroadcastMessage> {

    private static final String CHANNEL_FORMAT = "project.activity.changelog/%s";
    private static final long serialVersionUID = -1872059557540647259L;

    private String name;
    private String producingClientId;
    private CoursewareChangeLogBroadcastMessage content;

    @SuppressWarnings("unused")
    public ActivityChangeLogEventMessage() {
    }

    public ActivityChangeLogEventMessage(final UUID activityId) {
        this.name = buildChannelName(activityId.toString());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getProducingClientId() {
        return producingClientId;
    }

    @Override
    public CoursewareChangeLogBroadcastMessage getContent() {
        return content;
    }

    @Override
    public String buildChannelName(String values) {
        return String.format(CHANNEL_FORMAT, values);
    }

    public ActivityChangeLogEventMessage setProducingClientId(String producingClientId) {
        this.producingClientId = producingClientId;
        return this;
    }

    public ActivityChangeLogEventMessage setContent(CoursewareChangeLogBroadcastMessage content) {
        this.content = content;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityChangeLogEventMessage that = (ActivityChangeLogEventMessage) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(producingClientId, that.producingClientId) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, producingClientId, content);
    }

    @Override
    public String toString() {
        return "ActivityChangeLogEventMessage{" +
                "name='" + name + '\'' +
                ", producingClientId='" + producingClientId + '\'' +
                ", content=" + content +
                '}';
    }
}
