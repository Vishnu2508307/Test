package com.smartsparrow.annotation.service;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Object to store an Annotation within the Workspace/Builder role
 */
public class CoursewareAnnotation implements Annotation {

    protected UUID id;
    protected UUID version;
    protected Motivation motivation;
    protected UUID creatorAccountId;
    protected JsonNode bodyJson;
    protected JsonNode targetJson;
    //
    protected UUID rootElementId;
    protected UUID elementId;
    protected boolean resolved;

    public CoursewareAnnotation() {
    }

    @Override
    public UUID getId() {
        return id;
    }

    public CoursewareAnnotation setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public UUID getVersion() {
        return version;
    }

    public CoursewareAnnotation setVersion(UUID version) {
        this.version = version;
        return this;
    }

    @Override
    public Motivation getMotivation() {
        return motivation;
    }

    public CoursewareAnnotation setMotivation(Motivation motivation) {
        this.motivation = motivation;
        return this;
    }

    @Override
    public UUID getCreatorAccountId() {
        return creatorAccountId;
    }

    public CoursewareAnnotation setCreatorAccountId(UUID creatorAccountId) {
        this.creatorAccountId = creatorAccountId;
        return this;
    }

    @Override
    public JsonNode getBodyJson() {
        return bodyJson;
    }

    public CoursewareAnnotation setBodyJson(JsonNode bodyJson) {
        this.bodyJson = bodyJson;
        return this;
    }

    @Override
    public JsonNode getTargetJson() {
        return targetJson;
    }

    public CoursewareAnnotation setTargetJson(JsonNode targetJson) {
        this.targetJson = targetJson;
        return this;
    }

    public UUID getRootElementId() {
        return rootElementId;
    }

    public CoursewareAnnotation setRootElementId(UUID rootElementId) {
        this.rootElementId = rootElementId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public CoursewareAnnotation setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public boolean getResolved() {
        return resolved;
    }

    public CoursewareAnnotation setResolved(boolean resolved) {
        this.resolved = resolved;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CoursewareAnnotation that = (CoursewareAnnotation) o;
        return Objects.equals(id, that.id) && Objects.equals(version, that.version) && motivation == that.motivation
                && Objects.equals(creatorAccountId, that.creatorAccountId) && Objects.equals(bodyJson, that.bodyJson)
                && Objects.equals(targetJson, that.targetJson) && Objects.equals(rootElementId, that.rootElementId)
                && Objects.equals(elementId, that.elementId) && Objects.equals(resolved, that.resolved);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, motivation, creatorAccountId, bodyJson, targetJson, rootElementId, elementId, resolved);
    }

    @Override
    public String toString() {
        return "CoursewareAnnotation{"
                + "id=" + id
                + ", version=" + version
                + ", motivation=" + motivation
                + ", creatorAccountId=" + creatorAccountId
                + ", bodyJson=" + bodyJson
                + ", targetJson=" + targetJson
                + ", rootElementId=" + rootElementId
                + ", elementId=" + elementId
                + ", resolved=" + resolved
                + '}';
    }
}
