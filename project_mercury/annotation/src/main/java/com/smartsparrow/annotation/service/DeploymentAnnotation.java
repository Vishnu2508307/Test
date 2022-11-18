package com.smartsparrow.annotation.service;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Object to store an Annotation created in the workspace and published along to a deployment
 */
public class DeploymentAnnotation extends LearnerAnnotation implements Annotation {

    private UUID changeId;

    public DeploymentAnnotation(final CoursewareAnnotation coursewareAnnotation, final UUID deploymentId,
                                final UUID changeId) {
        id = coursewareAnnotation.getId();
        version = coursewareAnnotation.getVersion();
        motivation = coursewareAnnotation.getMotivation();
        creatorAccountId = coursewareAnnotation.getCreatorAccountId();
        elementId = coursewareAnnotation.getElementId();
        bodyJson = coursewareAnnotation.getBodyJson();
        targetJson = coursewareAnnotation.getTargetJson();
        this.deploymentId = deploymentId;
        this.changeId = changeId;
    }

    public DeploymentAnnotation(final LearnerAnnotation learnerAnnotation, final UUID changeId) {
        id = learnerAnnotation.getId();
        version = learnerAnnotation.getVersion();
        motivation = learnerAnnotation.getMotivation();
        creatorAccountId = learnerAnnotation.getCreatorAccountId();
        elementId = learnerAnnotation.getElementId();
        bodyJson = learnerAnnotation.getBodyJson();
        targetJson = learnerAnnotation.getTargetJson();
        this.deploymentId = learnerAnnotation.getDeploymentId();
        this.changeId = changeId;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public DeploymentAnnotation setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public UUID getVersion() {
        return version;
    }

    public DeploymentAnnotation setVersion(UUID version) {
        this.version = version;
        return this;
    }

    @Override
    public Motivation getMotivation() {
        return motivation;
    }

    public DeploymentAnnotation setMotivation(Motivation motivation) {
        this.motivation = motivation;
        return this;
    }

    @Override
    public UUID getCreatorAccountId() {
        return creatorAccountId;
    }

    public DeploymentAnnotation setCreatorAccountId(UUID creatorAccountId) {
        this.creatorAccountId = creatorAccountId;
        return this;
    }

    @Override
    public JsonNode getBodyJson() {
        return bodyJson;
    }

    public DeploymentAnnotation setBodyJson(JsonNode bodyJson) {
        this.bodyJson = bodyJson;
        return this;
    }

    @Override
    public JsonNode getTargetJson() {
        return targetJson;
    }

    public DeploymentAnnotation setTargetJson(JsonNode targetJson) {
        this.targetJson = targetJson;
        return this;
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public DeploymentAnnotation setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @JsonIgnore
    public UUID getChangeId() {
        return changeId;
    }

    public DeploymentAnnotation setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public DeploymentAnnotation setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DeploymentAnnotation that = (DeploymentAnnotation) o;
        return Objects.equals(changeId, that.changeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), changeId);
    }

    @Override
    public String toString() {
        return "DeploymentAnnotation{" +
                "changeId=" + changeId +
                "} " + super.toString();
    }
}
