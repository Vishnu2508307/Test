package com.smartsparrow.rtm.message.recv.courseware.scenario;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ReorderScenariosMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID parentId;
    private ScenarioLifecycle lifecycle;
    private List<UUID> scenarioIds;

    public UUID getParentId() {
        return parentId;
    }

    public ScenarioLifecycle getLifecycle() {
        return lifecycle;
    }

    public List<UUID> getScenarioIds() {
        return scenarioIds;
    }

    @JsonIgnore
    @Override
    public UUID getElementId() {
        return parentId;
    }

    @JsonIgnore
    @Override
    public CoursewareElementType getElementType() {
        return ScenarioLifecycle.getCoursewareElementType(lifecycle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReorderScenariosMessage that = (ReorderScenariosMessage) o;
        return Objects.equals(parentId, that.parentId) &&
                lifecycle == that.lifecycle &&
                Objects.equals(scenarioIds, that.scenarioIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, lifecycle, scenarioIds);
    }


    @Override
    public String toString() {
        return "ReorderScenariosMessage{" +
                "parentId=" + parentId +
                ", lifecycle=" + lifecycle +
                ", scenarioIds=" + scenarioIds +
                '}';
    }
}
