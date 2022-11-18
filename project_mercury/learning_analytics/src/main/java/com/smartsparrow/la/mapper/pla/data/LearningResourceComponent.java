package com.smartsparrow.la.mapper.pla.data;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningResourceComponent {
    private String activityDefinedByMessageTypeCode;
    private String activityUniqueIdentifier;
    private String assessmentItemId;
    private String assessmentItemIdType;
    private String userTaskType;
    private String assessmentItemSourceCode;
    private Integer sequenceNumber;
    private List<String> explanatoryNotes;

    @JsonProperty("activityDefinedByMessageTypeCode")
    public String getActivityDefinedByMessageTypeCode() {
        return activityDefinedByMessageTypeCode;
    }

    public LearningResourceComponent setActivityDefinedByMessageTypeCode(String activityDefinedByMessageTypeCode) {
        this.activityDefinedByMessageTypeCode = activityDefinedByMessageTypeCode;
        return this;
    }

    @JsonProperty("activityUniqueIdentifier")
    public String getActivityUniqueIdentifier() {
        return activityUniqueIdentifier;
    }

    public LearningResourceComponent setActivityUniqueIdentifier(String activityUniqueIdentifier) {
        this.activityUniqueIdentifier = activityUniqueIdentifier;
        return this;
    }

    @JsonProperty("assessmentItemId")
    public String getAssessmentItemId() {
        return assessmentItemId;
    }

    public LearningResourceComponent setAssessmentItemId(String assessmentItemId) {
        this.assessmentItemId = assessmentItemId;
        return this;
    }

    @JsonProperty("assessmentItemIdType")
    public String getAssessmentItemIdType() {
        return assessmentItemIdType;
    }

    public LearningResourceComponent setAssessmentItemIdType(String assessmentItemIdType) {
        this.assessmentItemIdType = assessmentItemIdType;
        return this;
    }

    @JsonProperty("userTaskType")
    public String getUserTaskType() {
        return userTaskType;
    }

    public LearningResourceComponent setUserTaskType(String userTaskType) {
        this.userTaskType = userTaskType;
        return this;
    }

    @JsonProperty("assessmentItemSourceCode")
    public String getAssessmentItemSourceCode() {
        return assessmentItemSourceCode;
    }

    public LearningResourceComponent setAssessmentItemSourceCode(String assessmentItemSourceCode) {
        this.assessmentItemSourceCode = assessmentItemSourceCode;
        return this;
    }

    @JsonProperty("sequenceNumber")
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public LearningResourceComponent setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    @JsonProperty("explanatoryNotes")
    public List<String> getExplanatoryNotes() {
        return explanatoryNotes;
    }

    public LearningResourceComponent setExplanatoryNotes(List<String> explanatoryNotes) {
        this.explanatoryNotes = explanatoryNotes;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearningResourceComponent that = (LearningResourceComponent) o;
        return Objects.equals(activityDefinedByMessageTypeCode, that.activityDefinedByMessageTypeCode) &&
                Objects.equals(activityUniqueIdentifier, that.activityUniqueIdentifier) &&
                Objects.equals(assessmentItemId, that.assessmentItemId) &&
                Objects.equals(assessmentItemIdType, that.assessmentItemIdType) &&
                Objects.equals(userTaskType, that.userTaskType) &&
                Objects.equals(assessmentItemSourceCode, that.assessmentItemSourceCode) &&
                Objects.equals(sequenceNumber, that.sequenceNumber) &&
                Objects.equals(explanatoryNotes, that.explanatoryNotes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityDefinedByMessageTypeCode, activityUniqueIdentifier, assessmentItemId, assessmentItemIdType, userTaskType, assessmentItemSourceCode, sequenceNumber, explanatoryNotes);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
