package com.smartsparrow.learner.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.smartsparrow.util.Json;
import org.apache.commons.collections4.map.HashedMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


/**
 * This class should accommodate the AssignmentGrade schema defined in the MX Grade Sync API doc:
 *   https://one-confluence.pearson.com/pages/viewpage.action?spaceKey=M&title=MX+Generic+Grade+Sync+API
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GradePassbackAssignment {
    private Long correlationId;
    private String userId;
    private Integer assignmentId;
    private String courseId;
    private Integer attemptNo;
    private Float assignmentScore;
    private GradePassbackProgressType assignmentProgress;
    private Integer assignmentProgressPercentage;
    private Long assignmentProgressDateTime; // epoch time in millis
    private List<GradePassbackItem> itemScore = new ArrayList<>();
    private String letterGrading;
    private List<Map<String, String>> extendedGradeProperties = new ArrayList<>();
    private String assignmentCallerCode;
    private UUID rootElementId;
    private String callerCode;
    private String userIdType;

    public Long getCorrelationId() {
        return correlationId;
    }

    public GradePassbackAssignment setCorrelationId(Long correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public GradePassbackAssignment setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public Integer getAssignmentId() {
        return assignmentId;
    }

    public GradePassbackAssignment setAssignmentId(Integer assignmentId) {
        this.assignmentId = assignmentId;
        return this;
    }

    public String getCourseId() {
        return courseId;
    }

    public GradePassbackAssignment setCourseId(String courseId) {
        this.courseId = courseId;
        return this;
    }

    public Integer getAttemptNo() {
        return attemptNo;
    }

    public GradePassbackAssignment setAttemptNo(Integer attemptNo) {
        this.attemptNo = attemptNo;
        return this;
    }

    public Float getAssignmentScore() {
        return assignmentScore;
    }

    public GradePassbackAssignment setAssignmentScore(Float assignmentScore) {
        this.assignmentScore = assignmentScore;
        return this;
    }

    public GradePassbackProgressType getAssignmentProgress() {
        return assignmentProgress;
    }

    public GradePassbackAssignment setAssignmentProgress(GradePassbackProgressType assignmentProgress) {
        this.assignmentProgress = assignmentProgress;
        return this;
    }

    public Integer getAssignmentProgressPercentage() {
        return assignmentProgressPercentage;
    }

    public GradePassbackAssignment setAssignmentProgressPercentage(Integer assignmentProgressPercentage) {
        this.assignmentProgressPercentage = assignmentProgressPercentage;
        return this;
    }

    public Long getAssignmentProgressDateTime() {
        return assignmentProgressDateTime;
    }

    public GradePassbackAssignment setAssignmentProgressDateTime(Long assignmentProgressDateTime) {
        this.assignmentProgressDateTime = assignmentProgressDateTime;
        return this;
    }

    public List<GradePassbackItem> getItemScore() {
        return itemScore;
    }

    public GradePassbackAssignment setItemScore(List<GradePassbackItem> itemScore) {
        this.itemScore = itemScore;
        return this;
    }

    public String getLetterGrading() {
        return letterGrading;
    }

    public GradePassbackAssignment setLetterGrading(String letterGrading) {
        this.letterGrading = letterGrading;
        return this;
    }

    public List<Map<String, String>> getExtendedGradeProperties() {
        return extendedGradeProperties;
    }

    public GradePassbackAssignment setExtendedGradeProperties(final List<Map<String, String>> extendedGradeProperties) {
        this.extendedGradeProperties = extendedGradeProperties;
        return this;
    }

    public String getAssignmentCallerCode() {
        return assignmentCallerCode;
    }

    public GradePassbackAssignment setAssignmentCallerCode(String assignmentCallerCode) {
        this.assignmentCallerCode = assignmentCallerCode;
        return this;
    }

    @JsonIgnore
    public UUID getRootElementId() {
        return rootElementId;
    }

    @JsonIgnore
    public GradePassbackAssignment setRootElementId(UUID rootElementId) {
        this.rootElementId = rootElementId;
        return this;
    }

    public String getCallerCode() {
        return callerCode;
    }

    public GradePassbackAssignment setCallerCode(final String callerCode) {
        this.callerCode = callerCode;
        return this;
    }

    public String getUserIdType() {
        return userIdType;
    }

    public GradePassbackAssignment setUserIdType(final String userIdType) {
        this.userIdType = userIdType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GradePassbackAssignment that = (GradePassbackAssignment) o;
        return Objects.equals(correlationId, that.correlationId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(assignmentId, that.assignmentId) &&
                Objects.equals(courseId, that.courseId) &&
                Objects.equals(attemptNo, that.attemptNo) &&
                Objects.equals(assignmentScore, that.assignmentScore) &&
                Objects.equals(assignmentProgress, that.assignmentProgress) &&
                Objects.equals(assignmentProgressPercentage, that.assignmentProgressPercentage) &&
                Objects.equals(assignmentProgressDateTime, that.assignmentProgressDateTime) &&
                Objects.equals(itemScore, that.itemScore) &&
                Objects.equals(letterGrading, that.letterGrading) &&
                Objects.equals(extendedGradeProperties, that.extendedGradeProperties) &&
                Objects.equals(assignmentCallerCode, that.assignmentCallerCode) &&
                Objects.equals(rootElementId, that.rootElementId) &&
                Objects.equals(callerCode, that.callerCode) &&
                Objects.equals(userIdType, that.userIdType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId, userId, assignmentId, courseId, attemptNo, assignmentScore,
                            assignmentProgress, assignmentProgressPercentage, assignmentProgressDateTime,
                            itemScore,
                            letterGrading, extendedGradeProperties, assignmentCallerCode, rootElementId, callerCode, userIdType);
    }

    @Override
    public String toString() {
        return Json.stringify(this);
    }
}
