package com.smartsparrow.rtm.message.recv.courseware.pathway;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.courseware.pathway.PreloadPathway;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreatePathwayMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID activityId;
    private PathwayType pathwayType;
    private String config;
    private UUID pathwayId;
    private PreloadPathway preloadPathway;

    public UUID getActivityId() {
        return activityId;
    }

    public PathwayType getPathwayType() {
        return pathwayType;
    }

    public PreloadPathway getPreloadPathway() {
        return preloadPathway;
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

    public UUID getPathwayId() {
        return pathwayId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreatePathwayMessage that = (CreatePathwayMessage) o;
        return Objects.equals(activityId, that.activityId) &&
                pathwayType == that.pathwayType &&
                Objects.equals(config, that.config) &&
                Objects.equals(pathwayId, that.pathwayId) &&
                Objects.equals(preloadPathway, that.preloadPathway);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, pathwayType, config, pathwayId, preloadPathway);
    }

    @Override
    public String toString() {
        return "CreatePathwayMessage{" +
                "activityId=" + activityId +
                ", pathwayType=" + pathwayType +
                ", config='" + config + '\'' +
                ", pathwayId=" + pathwayId +
                ", preloadPathway=" + preloadPathway +
                '}';
    }
}
