package com.smartsparrow.learner.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioCorrectness;

import io.leangen.graphql.annotations.GraphQLIgnore;

public class Evaluation {

    private UUID id;
    private UUID elementId;
    private CoursewareElementType elementType;
    private Deployment deployment;
    private UUID studentId;
    private UUID attemptId;
    private Map<UUID, String> elementScopeDataMap;
    private UUID studentScopeURN;
    private UUID parentId;
    private CoursewareElementType parentType;
    private UUID parentAttemptId;
    private Boolean completed;
    private List<UUID> triggeredScenarioIds;
    private ScenarioCorrectness scenarioCorrectness;
    private String triggeredActions;

    public UUID getId() {
        return id;
    }

    public Evaluation setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public Evaluation setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public Evaluation setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    @GraphQLIgnore
    public Deployment getDeployment() {
        return deployment;
    }

    public Evaluation setDeployment(Deployment deployment) {
        this.deployment = deployment;
        return this;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public Evaluation setStudentId(UUID studentId) {
        this.studentId = studentId;
        return this;
    }

    public UUID getAttemptId() {
        return attemptId;
    }

    public Evaluation setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
        return this;
    }

    @GraphQLIgnore
    public Map<UUID, String> getElementScopeDataMap() {
        return elementScopeDataMap;
    }

    public Evaluation setElementScopeDataMap(Map<UUID, String> elementScopeDataMap) {
        this.elementScopeDataMap = elementScopeDataMap;
        return this;
    }

    public UUID getStudentScopeURN() {
        return studentScopeURN;
    }

    public Evaluation setStudentScopeURN(UUID studentScopeURN) {
        this.studentScopeURN = studentScopeURN;
        return this;
    }

    @GraphQLIgnore
    public UUID getParentId() {
        return parentId;
    }

    public Evaluation setParentId(UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    @GraphQLIgnore
    public CoursewareElementType getParentType() {
        return parentType;
    }

    public Evaluation setParentType(CoursewareElementType parentType) {
        this.parentType = parentType;
        return this;
    }

    @GraphQLIgnore
    public UUID getParentAttemptId() {
        return parentAttemptId;
    }

    public Evaluation setParentAttemptId(UUID parentAttemptId) {
        this.parentAttemptId = parentAttemptId;
        return this;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public Evaluation setCompleted(Boolean completed) {
        this.completed = completed;
        return this;
    }

    @GraphQLIgnore
    public List<UUID> getTriggeredScenarioIds() {
        return triggeredScenarioIds;
    }

    public Evaluation setTriggeredScenarioIds(List<UUID> triggeredScenarioIds) {
        this.triggeredScenarioIds = triggeredScenarioIds;
        return this;
    }

    @Nullable
    public ScenarioCorrectness getScenarioCorrectness() {
        return scenarioCorrectness;
    }

    public Evaluation setScenarioCorrectness(@Nullable ScenarioCorrectness scenarioCorrectness) {
        this.scenarioCorrectness = scenarioCorrectness;
        return this;
    }

    public String getTriggeredActions() {
        return triggeredActions;
    }

    public Evaluation setTriggeredActions(String triggeredActions) {
        this.triggeredActions = triggeredActions;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evaluation that = (Evaluation) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(elementId, that.elementId) &&
                elementType == that.elementType &&
                Objects.equals(deployment, that.deployment) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(attemptId, that.attemptId) &&
                Objects.equals(elementScopeDataMap, that.elementScopeDataMap) &&
                Objects.equals(studentScopeURN, that.studentScopeURN) &&
                Objects.equals(parentId, that.parentId) &&
                parentType == that.parentType &&
                Objects.equals(parentAttemptId, that.parentAttemptId) &&
                Objects.equals(completed, that.completed) &&
                Objects.equals(triggeredScenarioIds, that.triggeredScenarioIds) &&
                scenarioCorrectness == that.scenarioCorrectness &&
                Objects.equals(triggeredActions, that.triggeredActions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, elementId, elementType, deployment, studentId, attemptId, elementScopeDataMap,
                studentScopeURN, parentId, parentType, parentAttemptId, completed, triggeredScenarioIds,
                scenarioCorrectness, triggeredActions);
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", elementId=" + elementId +
                ", elementType=" + elementType +
                ", deployment=" + deployment +
                ", studentId=" + studentId +
                ", attemptId=" + attemptId +
                ", elementScopeDataMap=" + elementScopeDataMap +
                ", studentScopeURN=" + studentScopeURN +
                ", parentId=" + parentId +
                ", parentType=" + parentType +
                ", parentAttemptId=" + parentAttemptId +
                ", completed=" + completed +
                ", triggeredScenarioIds=" + triggeredScenarioIds +
                ", scenarioCorrectness=" + scenarioCorrectness +
                ", triggeredActions='" + triggeredActions + '\'' +
                '}';
    }
}
