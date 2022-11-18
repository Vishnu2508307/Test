package com.smartsparrow.rtm.subscription.courseware.message;

import static com.smartsparrow.courseware.data.CoursewareElementType.SCENARIO;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;

public class ScenarioCreatedBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = 119562454581737396L;

    private final UUID parentElementId;
    private final CoursewareElementType parentElementType;
    private final ScenarioLifecycle lifecycle;

    public ScenarioCreatedBroadcastMessage(UUID activityId,
                                           UUID scenarioId,
                                           UUID parentElementId,
                                           CoursewareElementType parentElementType,
                                           ScenarioLifecycle lifecycle) {
        super(activityId, scenarioId, SCENARIO);
        this.parentElementId = parentElementId;
        this.parentElementType = parentElementType;
        this.lifecycle = lifecycle;
    }

    public UUID getParentElementId() {
        return parentElementId;
    }

    public CoursewareElementType getParentElementType() {
        return parentElementType;
    }

    public ScenarioLifecycle getLifecycle() {
        return lifecycle;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ScenarioCreatedBroadcastMessage that = (ScenarioCreatedBroadcastMessage) o;
        return Objects.equals(parentElementId, that.parentElementId) &&
                parentElementType == that.parentElementType &&
                lifecycle == that.lifecycle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parentElementId, parentElementType, lifecycle);
    }

    @Override
    public String toString() {
        return "ScenarioCreatedBroadcastMessage{" +
                "parentElementId=" + parentElementId +
                ", parentElementType=" + parentElementType +
                ", lifecycle=" + lifecycle +
                "} " + super.toString();
    }
}
