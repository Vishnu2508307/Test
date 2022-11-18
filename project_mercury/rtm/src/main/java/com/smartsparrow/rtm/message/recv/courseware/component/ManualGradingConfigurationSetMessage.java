package com.smartsparrow.rtm.message.recv.courseware.component;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ManualGradingConfigurationSetMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID componentId;
    private Double maxScore;

    @JsonIgnore
    @Override
    public UUID getElementId() {
        return componentId;
    }

    @JsonIgnore
    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.COMPONENT;
    }

    public UUID getComponentId() {
        return componentId;
    }

    @Nullable
    public Double getMaxScore() {
        return maxScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManualGradingConfigurationSetMessage that = (ManualGradingConfigurationSetMessage) o;
        return Objects.equals(componentId, that.componentId) &&
                Objects.equals(maxScore, that.maxScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentId, maxScore);
    }

    @Override
    public String toString() {
        return "ManualGradingConfigurationSetMessage{" +
                "componentId=" + componentId +
                ", maxScore=" + maxScore +
                '}';
    }
}
