package com.smartsparrow.rtm.message.recv.courseware.feedback;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateInteractiveFeedbackMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID interactiveId;
    private UUID pluginId;
    @JsonProperty("pluginVersion")
    private String pluginVersionExp;

    public UUID getInteractiveId() {
        return interactiveId;
    }

    public UUID getPluginId() {
        return pluginId;
    }

    public String getPluginVersionExp() {
        return pluginVersionExp;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateInteractiveFeedbackMessage that = (CreateInteractiveFeedbackMessage) o;
        return Objects.equals(interactiveId, that.interactiveId) &&
                Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(pluginVersionExp, that.pluginVersionExp);
    }

    @Override
    public int hashCode() {

        return Objects.hash(interactiveId, pluginId, pluginVersionExp);
    }

    @Override
    public String toString() {
        return "CreateInteractiveFeedbackMessage{" +
                "interactiveId=" + interactiveId +
                ", pluginId=" + pluginId +
                ", pluginVersionExp='" + pluginVersionExp + '\'' +
                '}';
    }
}
