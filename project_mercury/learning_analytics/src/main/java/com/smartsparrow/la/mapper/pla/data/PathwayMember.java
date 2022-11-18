package com.smartsparrow.la.mapper.pla.data;

import java.util.Objects;

public class PathwayMember {

    private Integer sequenceNum;
    private String activityDefinedByMessageTypeCode;
    private String activityUniqueIdentifier;
    private String learningResourceId;
    private String learningResourceIdType;

    public Integer getSequenceNum() {
        return sequenceNum;
    }

    public PathwayMember setSequenceNum(Integer sequenceNum) {
        this.sequenceNum = sequenceNum;
        return this;
    }

    public String getActivityDefinedByMessageTypeCode() {
        return activityDefinedByMessageTypeCode;
    }

    public PathwayMember setActivityDefinedByMessageTypeCode(String activityDefinedByMessageTypeCode) {
        this.activityDefinedByMessageTypeCode = activityDefinedByMessageTypeCode;
        return this;
    }

    public String getActivityUniqueIdentifier() {
        return activityUniqueIdentifier;
    }

    public PathwayMember setActivityUniqueIdentifier(String activityUniqueIdentifier) {
        this.activityUniqueIdentifier = activityUniqueIdentifier;
        return this;
    }

    public String getLearningResourceId() {
        return learningResourceId;
    }

    public PathwayMember setLearningResourceId(String learningResourceId) {
        this.learningResourceId = learningResourceId;
        return this;
    }

    public String getLearningResourceIdType() {
        return learningResourceIdType;
    }

    public PathwayMember setLearningResourceIdType(String learningResourceIdType) {
        this.learningResourceIdType = learningResourceIdType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathwayMember that = (PathwayMember) o;
        return Objects.equals(sequenceNum, that.sequenceNum) &&
                Objects.equals(activityDefinedByMessageTypeCode, that.activityDefinedByMessageTypeCode) &&
                Objects.equals(activityUniqueIdentifier, that.activityUniqueIdentifier) &&
                Objects.equals(learningResourceId, that.learningResourceId) &&
                Objects.equals(learningResourceIdType, that.learningResourceIdType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequenceNum, activityDefinedByMessageTypeCode, activityUniqueIdentifier, learningResourceId, learningResourceIdType);
    }

    @Override
    public String toString() {
        return "PathwayMember{" +
                "sequenceNum=" + sequenceNum +
                ", activityDefinedByMessageTypeCode='" + activityDefinedByMessageTypeCode + '\'' +
                ", activityUniqueIdentifier='" + activityUniqueIdentifier + '\'' +
                ", learningResourceId='" + learningResourceId + '\'' +
                ", learningResourceIdType='" + learningResourceIdType + '\'' +
                '}';
    }
}
