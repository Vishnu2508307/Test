package com.smartsparrow.rtm.message.recv.courseware.scenario;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreateScenarioMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID parentId;
    private String condition;
    private String actions;
    private ScenarioLifecycle lifecycle;
    private String name;
    private String description;
    private ScenarioCorrectness correctness;

    public UUID getParentId() {
        return parentId;
    }

    public String getCondition() {
        return condition;
    }

    public String getActions() {
        return actions;
    }

    public ScenarioLifecycle getLifecycle() {
        return lifecycle;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ScenarioCorrectness getCorrectness() {
        return correctness;
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
        CreateScenarioMessage that = (CreateScenarioMessage) o;
        return Objects.equals(parentId, that.parentId) &&
                Objects.equals(condition, that.condition) &&
                Objects.equals(actions, that.actions) &&
                lifecycle == that.lifecycle &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(correctness, that.correctness);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, condition, actions, lifecycle, name, description, correctness);
    }

    @Override
    public String toString() {
        return "CreateScenarioMessage{" +
                "parentId='" + parentId + '\'' +
                ", condition='" + condition + '\'' +
                ", actions='" + actions + '\'' +
                ", lifecycle=" + lifecycle +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", correctness='" + correctness + '\'' +
                '}';
    }
}
