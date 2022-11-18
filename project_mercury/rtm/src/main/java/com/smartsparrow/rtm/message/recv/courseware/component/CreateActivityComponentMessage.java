package com.smartsparrow.rtm.message.recv.courseware.component;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateActivityComponentMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID activityId;
    private UUID pluginId;
    @JsonProperty("pluginVersion")
    private String pluginVersionExpr;
    private String config;
    private UUID componentId;

    public UUID getActivityId() {
        return activityId;
    }

    public UUID getPluginId() {
        return pluginId;
    }

    public String getPluginVersionExpr() {
        return pluginVersionExpr;
    }

    @JsonIgnore
    @Override
    public UUID getElementId() {
        return activityId;
    }

    @JsonIgnore
    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.ACTIVITY;
    }

    public String getConfig() {
        return config;
    }

    public UUID getComponentId() {
        return componentId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateActivityComponentMessage that = (CreateActivityComponentMessage) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(pluginVersionExpr, that.pluginVersionExpr) &&
                Objects.equals(config, that.config) &&
                Objects.equals(componentId, that.componentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, pluginId, pluginVersionExpr, config, componentId);
    }

    @Override
    public String toString() {
        return "CreateActivityComponentMessage{" +
                "activityId=" + activityId +
                ", pluginId=" + pluginId +
                ", pluginVersionExpr='" + pluginVersionExpr + '\'' +
                ", config='" + config + '\'' +
                ", componentId=" + componentId +
                "} " + super.toString();
    }
}
