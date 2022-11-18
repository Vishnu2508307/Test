package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.util.Json;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultingAction {
    private String activityStatusCode;
    private String activityScoreAction;
    private String activityScoreOperationCode;
    private String activityScoreOperationNum;
    private String activityStatusFeedbackCode;
    private String activityStatusFeedbackText;
    private String activityStatusPathwayCode;

    @JsonProperty("activityStatusCode")
    public String getActivityStatusCode() {
        return activityStatusCode;
    }

    public ResultingAction setActivityStatusCode(String activityStatusCode) {
        this.activityStatusCode = activityStatusCode;
        return this;
    }

    @JsonProperty("activityScoreAction")
    public String getActivityScoreAction() {
        return activityScoreAction;
    }

    public ResultingAction setActivityScoreAction(String activityScoreAction) {
        this.activityScoreAction = activityScoreAction;
        return this;
    }

    @JsonProperty("activityScoreOperationCode")
    public String getActivityScoreOperationCode() {
        return activityScoreOperationCode;
    }

    public ResultingAction setActivityScoreOperationCode(String activityScoreOperationCode) {
        this.activityScoreOperationCode = activityScoreOperationCode;
        return this;
    }

    @JsonProperty("activityScoreOperationNum")
    public String getActivityScoreOperationNum() {
        return activityScoreOperationNum;
    }

    public ResultingAction setActivityScoreOperationNum(String activityScoreOperationNum) {
        this.activityScoreOperationNum = activityScoreOperationNum;
        return this;
    }

    @JsonProperty("activityStatusFeedbackCode")
    public String getActivityStatusFeedbackCode() {
        return activityStatusFeedbackCode;
    }

    public ResultingAction setActivityStatusFeedbackCode(String activityStatusFeedbackCode) {
        this.activityStatusFeedbackCode = activityStatusFeedbackCode;
        return this;
    }

    @JsonProperty("activityStatusFeedbackText")
    public String getActivityStatusFeedbackText() {
        return activityStatusFeedbackText;
    }

    public ResultingAction setActivityStatusFeedbackText(String activityStatusFeedbackText) {
        this.activityStatusFeedbackText = activityStatusFeedbackText;
        return this;
    }

    @JsonProperty("activityStatusPathwayCode")
    public String getActivityStatusPathwayCode() {
        return activityStatusPathwayCode;
    }

    public ResultingAction setActivityStatusPathwayCode(String activityStatusPathwayCode) {
        this.activityStatusPathwayCode = activityStatusPathwayCode;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultingAction that = (ResultingAction) o;
        return Objects.equals(activityStatusCode, that.activityStatusCode) &&
                Objects.equals(activityScoreAction, that.activityScoreAction) &&
                Objects.equals(activityScoreOperationCode, that.activityScoreOperationCode) &&
                Objects.equals(activityScoreOperationNum, that.activityScoreOperationNum) &&
                Objects.equals(activityStatusFeedbackCode, that.activityStatusFeedbackCode) &&
                Objects.equals(activityStatusFeedbackText, that.activityStatusFeedbackText) &&
                Objects.equals(activityStatusPathwayCode, that.activityStatusPathwayCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityStatusCode, activityScoreAction, activityScoreOperationCode, activityScoreOperationNum, activityStatusFeedbackCode, activityStatusFeedbackText, activityStatusPathwayCode);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
