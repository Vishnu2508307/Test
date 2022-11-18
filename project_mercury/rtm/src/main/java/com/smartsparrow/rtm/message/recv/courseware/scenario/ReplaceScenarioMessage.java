package com.smartsparrow.rtm.message.recv.courseware.scenario;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ReplaceScenarioMessage extends ReceivedMessage implements CoursewareElementMessage {

    private UUID scenarioId;
    private String condition;
    private String actions;
    private String name;
    private String description;
    private ScenarioCorrectness correctness;

    public UUID getScenarioId() {
        return scenarioId;
    }

    public String getCondition() {
        return condition;
    }

    public String getActions() {
        return actions;
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
        return scenarioId;
    }

    @JsonIgnore
    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.SCENARIO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReplaceScenarioMessage that = (ReplaceScenarioMessage) o;
        return Objects.equals(scenarioId, that.scenarioId) &&
                Objects.equals(condition, that.condition) &&
                Objects.equals(actions, that.actions) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                Objects.equals(correctness, that.correctness);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scenarioId, condition, actions, name, description, correctness);
    }

    @Override
    public String toString() {
        return "ReplaceScenarioMessage{" +
                "scenarioId=" + scenarioId +
                ", condition='" + condition + '\'' +
                ", action='" + actions + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", correctness='" + correctness + '\'' +
                '}';
    }
}
