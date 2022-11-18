package com.smartsparrow.annotation.service;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Object to store an Annotation within the Learner role. Represent an annotation created by the learner
 */
public class LearnerAnnotation implements Annotation {

    protected UUID id;
    protected UUID version;
    protected Motivation motivation;
    protected UUID creatorAccountId;
    protected JsonNode bodyJson;
    protected JsonNode targetJson;
    //
    protected UUID deploymentId;
    protected UUID elementId;

    public LearnerAnnotation() {
    }


    public LearnerAnnotation(final CoursewareAnnotation coursewareAnnotation, final UUID deploymentId) {

        id = coursewareAnnotation.getId();
        version = coursewareAnnotation.getVersion();
        motivation = coursewareAnnotation.getMotivation();
        creatorAccountId = coursewareAnnotation.getCreatorAccountId();
        elementId = coursewareAnnotation.getElementId();
        bodyJson = coursewareAnnotation.getBodyJson();
        targetJson = coursewareAnnotation.getTargetJson();
        this.deploymentId = deploymentId;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public LearnerAnnotation setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public UUID getVersion() {
        return version;
    }

    public LearnerAnnotation setVersion(UUID version) {
        this.version = version;
        return this;
    }

    @Override
    public Motivation getMotivation() {
        return motivation;
    }

    public LearnerAnnotation setMotivation(Motivation motivation) {
        this.motivation = motivation;
        return this;
    }

    @Override
    public UUID getCreatorAccountId() {
        return creatorAccountId;
    }

    public LearnerAnnotation setCreatorAccountId(UUID creatorAccountId) {
        this.creatorAccountId = creatorAccountId;
        return this;
    }

    @Override
    public JsonNode getBodyJson() {
        return bodyJson;
    }

    public LearnerAnnotation setBodyJson(JsonNode bodyJson) {
        this.bodyJson = bodyJson;
        return this;
    }

    @Override
    public JsonNode getTargetJson() {
        return targetJson;
    }

    public LearnerAnnotation setTargetJson(JsonNode targetJson) {
        this.targetJson = targetJson;
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerAnnotation setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public LearnerAnnotation setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        LearnerAnnotation that = (LearnerAnnotation) o;
        return Objects.equals(id, that.id) && Objects.equals(version, that.version) && motivation == that.motivation
                && Objects.equals(creatorAccountId, that.creatorAccountId) && Objects.equals(bodyJson, that.bodyJson)
                && Objects.equals(targetJson, that.targetJson) && Objects.equals(deploymentId, that.deploymentId)
                && Objects.equals(elementId, that.elementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, motivation, creatorAccountId, bodyJson, targetJson, deploymentId, elementId);
    }

    @Override
    public String toString() {
        return "LearnerAnnotation{" + "id=" + id + ", version=" + version + ", motivation=" + motivation
                + ", creatorAccountId=" + creatorAccountId + ", bodyJson=" + bodyJson + ", targetJson=" + targetJson
                + ", deploymentId=" + deploymentId + ", elementId=" + elementId + '}';
    }
}
