package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssessmentItem {

    private String assessmentItemId;
    private String assessmentItemIdType;
    private String itemDefinedByMessageTypeCode;
    private String userTaskType;
    private String assessmentItemSourceCode;
    private Integer sequenceNumber;
    private Double itemPossiblePoints;

    @JsonProperty("assessmentItemId")
    public String getAssessmentItemId() {
        return assessmentItemId;
    }

    public AssessmentItem setAssessmentItemId(String assessmentItemId) {
        this.assessmentItemId = assessmentItemId;
        return this;
    }

    @JsonProperty("assessmentItemIdType")
    public String getAssessmentItemIdType() {
        return assessmentItemIdType;
    }

    public AssessmentItem setAssessmentItemIdType(String assessmentItemIdType) {
        this.assessmentItemIdType = assessmentItemIdType;
        return this;
    }

    @JsonProperty("itemDefinedByMessageTypeCode")
    public String getItemDefinedByMessageTypeCode() {
        return itemDefinedByMessageTypeCode;
    }

    public AssessmentItem setItemDefinedByMessageTypeCode(String itemDefinedByMessageTypeCode) {
        this.itemDefinedByMessageTypeCode = itemDefinedByMessageTypeCode;
        return this;
    }

    @JsonProperty("userTaskType")
    public String getUserTaskType() {
        return userTaskType;
    }

    public AssessmentItem setUserTaskType(String userTaskType) {
        this.userTaskType = userTaskType;
        return this;
    }

    @JsonProperty("assessmentItemSourceCode")
    public String getAssessmentItemSourceCode() {
        return assessmentItemSourceCode;
    }

    public AssessmentItem setAssessmentItemSourceCode(String assessmentItemSourceCode) {
        this.assessmentItemSourceCode = assessmentItemSourceCode;
        return this;
    }

    @JsonProperty("sequenceNumber")
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public AssessmentItem setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    @JsonProperty("itemPossiblePoints")
    public Double getItemPossiblePoints() {
        return itemPossiblePoints;
    }

    public AssessmentItem setItemPossiblePoints(Double itemPossiblePoints) {
        this.itemPossiblePoints = itemPossiblePoints;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssessmentItem that = (AssessmentItem) o;
        return Objects.equals(assessmentItemId, that.assessmentItemId) &&
                Objects.equals(assessmentItemIdType, that.assessmentItemIdType) &&
                Objects.equals(itemDefinedByMessageTypeCode, that.itemDefinedByMessageTypeCode) &&
                Objects.equals(userTaskType, that.userTaskType) &&
                Objects.equals(assessmentItemSourceCode, that.assessmentItemSourceCode) &&
                Objects.equals(sequenceNumber, that.sequenceNumber) &&
                Objects.equals(itemPossiblePoints, that.itemPossiblePoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assessmentItemId, assessmentItemIdType, itemDefinedByMessageTypeCode, userTaskType, assessmentItemSourceCode, sequenceNumber, itemPossiblePoints);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
