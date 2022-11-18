package com.smartsparrow.learner.wiring;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GradePassbackConfig {
    @JsonProperty("masteringGradeSyncUrl")
    private String masteringGradeSyncUrl;

    @JsonProperty("checkGradePassbackQuestionCount")
    private Boolean checkGradePassbackQuestionCount;

    public String getMasteringGradeSyncUrl() {
        return masteringGradeSyncUrl;
    }

    public GradePassbackConfig setMasteringGradeSyncUrl(String masteringGradeSyncUrl) {
        this.masteringGradeSyncUrl = masteringGradeSyncUrl;
        return this;
    }

    public Boolean isCheckGradePassbackQuestionCount() {
        return checkGradePassbackQuestionCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradePassbackConfig that = (GradePassbackConfig) o;
        return Objects.equals(masteringGradeSyncUrl, that.masteringGradeSyncUrl)
                && Objects.equals(checkGradePassbackQuestionCount,
                                                        that.checkGradePassbackQuestionCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(masteringGradeSyncUrl, checkGradePassbackQuestionCount);
    }

    @Override
    public String toString() {
        return "GradePassbackConfig{" +
                "masteringGradeSyncUrl='" + masteringGradeSyncUrl + '\'' +
                "checkGradePassbackQuestionCount='" + checkGradePassbackQuestionCount + '\'' +
                '}';
    }
}
