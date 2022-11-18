package com.smartsparrow.rtm.message.recv.courseware.interactive;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateInteractiveMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID pathwayId;
    private UUID pluginId;
    @JsonProperty("pluginVersion")
    private String pluginVersionExpr;
    private String config;
    private UUID interactiveId;

    public UUID getPathwayId() {
        return pathwayId;
    }

    public UUID getPluginId() {
        return pluginId;
    }

    public String getPluginVersionExpr() {
        return pluginVersionExpr;
    }

    public String getConfig() {
        return config;
    }

    public UUID getInteractiveId() {
        return interactiveId;
    }

    @JsonIgnore
    @Override
    public UUID getElementId() {
        return pathwayId;
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
        CreateInteractiveMessage that = (CreateInteractiveMessage) o;
        return Objects.equals(pathwayId, that.pathwayId) &&
                Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(pluginVersionExpr, that.pluginVersionExpr) &&
                Objects.equals(config, that.config) &&
                Objects.equals(interactiveId, that.interactiveId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pathwayId, pluginId, pluginVersionExpr, config, interactiveId);
    }

    @Override
    public String toString() {
        return "CreateInteractiveMessage{" +
                "pathwayId=" + pathwayId +
                ", pluginId=" + pluginId +
                ", pluginVersionExpr='" + pluginVersionExpr + '\'' +
                ", config='" + config + '\'' +
                ", interactiveId=" + interactiveId +
                "} " + super.toString();
    }
}
