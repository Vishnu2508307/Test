package com.smartsparrow.rtm.subscription.courseware.message;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;

public class ScenarioReOrderedBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = 8707161625496935074L;

    private final List<UUID> scenarioIds;
    private final ScenarioLifecycle lifecycle;

    public ScenarioReOrderedBroadcastMessage(UUID activityId,
                                             UUID elementId,
                                             CoursewareElementType elementType,
                                             List<UUID> scenarioIds,
                                             ScenarioLifecycle lifecycle) {
        super(activityId, elementId, elementType);
        this.scenarioIds = scenarioIds;
        this.lifecycle = lifecycle;
    }

    public List<UUID> getScenarioIds() {
        return scenarioIds;
    }

    public ScenarioLifecycle getLifecycle() {
        return lifecycle;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ScenarioReOrderedBroadcastMessage that = (ScenarioReOrderedBroadcastMessage) o;
        return Objects.equals(scenarioIds, that.scenarioIds) && lifecycle == that.lifecycle;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), scenarioIds, lifecycle);
    }

    @Override
    public String toString() {
        return "ScenarioReOrderedBroadcastMessage{" +
                "scenarioIds=" + scenarioIds +
                ", lifecycle=" + lifecycle +
                "} " + super.toString();
    }
}
