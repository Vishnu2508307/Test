package com.smartsparrow.learner.data;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.smartsparrow.courseware.data.CoursewareElementType;

public class LearnerSearchableDocument {
    private UUID deploymentId;
    private UUID elementId;
    private UUID searchableFieldId;
    private UUID changeId;
    @JsonProperty("pearsonId")
    private String productId;
    private UUID cohortId;
    private CoursewareElementType elementType;
    private List<UUID> elementPath;
    private String contentType;

    @JsonIgnore
    private List<String> elementPathType;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String summary;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String body;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String source;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String preview;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String tag;

    /**
     * CSG index id, it is composed of other ids in the pattern deploymentId
     */
    public String getId() {
        return getDeploymentId() + ":" + getElementId();
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerSearchableDocument setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getSearchableFieldId() {
        return searchableFieldId;
    }

    public LearnerSearchableDocument setSearchableFieldId(UUID searchableFieldId) {
        this.searchableFieldId = searchableFieldId;
        return this;
    }

    public UUID getChangeId() {
        return changeId;
    }

    public LearnerSearchableDocument setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    /**
     * In Json this should be exported as "pearsonId", because this is what the CSG index calls it
     * until we get a change to change it
     */
    public String getProductId() {
        return productId;
    }

    public LearnerSearchableDocument setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public UUID getCohortId() {
        return cohortId;
    }

    public LearnerSearchableDocument setCohortId(UUID cohortId) {
        this.cohortId = cohortId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public LearnerSearchableDocument setElementId(UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public LearnerSearchableDocument setElementType(CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public List<String> getElementPath() {
        return getElementPathType();
    }

    public List<UUID> getMutatorElementPath() {
        return elementPath;
    }

    public LearnerSearchableDocument setElementPath(List<UUID> elementPath) {
        this.elementPath = elementPath;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public LearnerSearchableDocument setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public List<String> getElementPathType() {
        return elementPathType;
    }

    public LearnerSearchableDocument setElementPathType(List<String> elementPathType) {
        this.elementPathType = elementPathType;
        return this;
    }

    public String getSummary() {
        return summary;
    }

    public LearnerSearchableDocument setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public String getBody() {
        return body;
    }

    public LearnerSearchableDocument setBody(String body) {
        this.body = body;
        return this;
    }

    public String getSource() {
        return source;
    }

    public LearnerSearchableDocument setSource(String source) {
        this.source = source;
        return this;
    }

    public String getPreview() {
        return preview;
    }

    public LearnerSearchableDocument setPreview(String preview) {
        this.preview = preview;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public LearnerSearchableDocument setTag(String tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerSearchableDocument that = (LearnerSearchableDocument) o;
        return Objects.equal(deploymentId, that.deploymentId) &&
                Objects.equal(elementId, that.elementId) &&
                Objects.equal(searchableFieldId, that.searchableFieldId) &&
                Objects.equal(changeId, that.changeId) &&
                Objects.equal(productId, that.productId) &&
                Objects.equal(cohortId, that.cohortId) &&
                Objects.equal(elementType, that.elementType) &&
                Objects.equal(elementPath, that.elementPath) &&
                Objects.equal(elementPathType, that.elementPathType) &&
                Objects.equal(contentType, that.contentType) &&
                Objects.equal(summary, that.summary) &&
                Objects.equal(body, that.body) &&
                Objects.equal(source, that.source) &&
                Objects.equal(preview, that.preview) &&
                Objects.equal(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(deploymentId, elementId, searchableFieldId, changeId, productId, cohortId, elementType,
                elementPath, elementPathType, contentType, summary, body, source, preview, tag);
    }

    @Override
    public String toString() {
        return "LearnerSearchableDocument{" +
                "deploymentId=" + deploymentId +
                ", elementId=" + elementId +
                ", searchableFieldId=" + searchableFieldId +
                ", changeId=" + changeId +
                ", pearsonId='" + productId + '\'' +
                ", cohortId=" + cohortId +
                ", elementType='" + elementType + '\'' +
                ", elementPath=" + elementPath +
                ", elementPathType=" + elementPathType +
                ", contentType='" + contentType + '\'' +
                ", summary='" + summary + '\'' +
                ", body='" + body + '\'' +
                ", source='" + source + '\'' +
                ", preview='" + preview + '\'' +
                ", tag='" + tag + '\'' +
                '}';
    }
}