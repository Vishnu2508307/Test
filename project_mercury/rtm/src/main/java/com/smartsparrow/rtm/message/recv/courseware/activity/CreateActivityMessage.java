package com.smartsparrow.rtm.message.recv.courseware.activity;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateActivityMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID pluginId;

    @JsonProperty("pluginVersion")
    private String pluginVersionExpr;

    private String config;

    private String theme;

    private UUID parentPathwayId;

    private UUID activityId;

    public UUID getPluginId() {
        return pluginId;
    }

    public String getPluginVersionExpr() {
        return pluginVersionExpr;
    }

    public String getConfig() {
        return config;
    }

    public String getTheme() {
        return theme;
    }

    public UUID getParentPathwayId() {
        return parentPathwayId;
    }

    public UUID getActivityId() {
        return activityId;
    }

    @JsonIgnore
    @Override
    public UUID getElementId() {
        return parentPathwayId;
    }

    @JsonIgnore
    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.PATHWAY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateActivityMessage that = (CreateActivityMessage) o;
        return Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(pluginVersionExpr, that.pluginVersionExpr) &&
                Objects.equals(config, that.config) &&
                Objects.equals(theme, that.theme) &&
                Objects.equals(parentPathwayId, that.parentPathwayId) &&
                Objects.equals(activityId, that.activityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, pluginVersionExpr, config, theme, parentPathwayId, activityId);
    }

    @Override
    public String toString() {
        return "CreateActivityMessage{" +
                "pluginId=" + pluginId +
                ", pluginVersionExpr='" + pluginVersionExpr + '\'' +
                ", config='" + config + '\'' +
                ", theme='" + theme + '\'' +
                ", parentPathwayId=" + parentPathwayId +
                ", activityId=" + activityId +
                '}';
    }
}
