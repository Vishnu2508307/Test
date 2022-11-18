package com.smartsparrow.courseware.eventmessage;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.eventmessage.EventMessage;

public class ActivityEventMessage implements EventMessage<CoursewareElementBroadcastMessage> {

    private static final String CHANNEL_FORMAT = "author.activity/%s";
    private static final long serialVersionUID = -1872059557540647259L;

    private String name;
    private String producingClientId;
    private CoursewareElementBroadcastMessage content;

    /**
     * Private empty constructor for serialization.
     *
     */
    @SuppressWarnings("unused")
    public ActivityEventMessage() {
    }

    public ActivityEventMessage(UUID activityId) {
        this.name = buildChannelName(activityId.toString());
    }

    @Override
    public String getName() {
        return name;
    }

    public ActivityEventMessage setProducingClientId(String producingClientId) {
        this.producingClientId = producingClientId;
        return this;
    }

    public ActivityEventMessage setContent(CoursewareElementBroadcastMessage content) {
        this.content = content;
        return this;
    }

    @Override
    public String getProducingClientId() {
        return producingClientId;
    }

    @Override
    public CoursewareElementBroadcastMessage getContent() {
        return content;
    }

    @Override
    public String buildChannelName(String values) {
        return String.format(CHANNEL_FORMAT, values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityEventMessage that = (ActivityEventMessage) o;
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
        return "ActivityEventMessage{" +
                "name='" + name + '\'' +
                ", producingClientId='" + producingClientId + '\'' +
                ", content=" + content +
                '}';
    }
}
