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
public class CreateInteractiveComponentMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID interactiveId;
    private UUID pluginId;
    @JsonProperty("pluginVersion")
    private String pluginVersionExpr;
    private String config;
    private UUID componentId;

    public UUID getInteractiveId() {
        return interactiveId;
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
        return interactiveId;
    }

    @JsonIgnore
    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.INTERACTIVE;
    }

    public String getConfig() {
        return config;
    }

    public UUID getComponentId() {
        return componentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateInteractiveComponentMessage that = (CreateInteractiveComponentMessage) o;
        return Objects.equals(interactiveId, that.interactiveId) &&
                Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(pluginVersionExpr, that.pluginVersionExpr) &&
                Objects.equals(config, that.config) &&
                Objects.equals(componentId, that.componentId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(interactiveId, pluginId, pluginVersionExpr, config, componentId);
    }

    @Override
    public String toString() {
        return "CreateInteractiveComponentMessage{" +
                "interactiveId=" + interactiveId +
                ", pluginId=" + pluginId +
                ", pluginVersionExpr='" + pluginVersionExpr + '\'' +
                ", config='" + config + '\'' +
                ", componentId=" + componentId +
                "} " + super.toString();
    }
}
