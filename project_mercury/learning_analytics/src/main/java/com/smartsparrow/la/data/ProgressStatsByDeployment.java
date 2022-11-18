package com.smartsparrow.la.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class ProgressStatsByDeployment {

    private UUID deploymentId;
    private UUID coursewareElementId;
    private CoursewareElementType coursewareElementType;
    private StatType statType;
    private Double statValue;

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public ProgressStatsByDeployment setDeploymentId(final UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public UUID getCoursewareElementId() {
        return coursewareElementId;
    }

    public ProgressStatsByDeployment setCoursewareElementId(final UUID coursewareElementId) {
        this.coursewareElementId = coursewareElementId;
        return this;
    }

    public CoursewareElementType getCoursewareElementType() {
        return coursewareElementType;
    }

    public ProgressStatsByDeployment setCoursewareElementType(final CoursewareElementType coursewareElementType) {
        this.coursewareElementType = coursewareElementType;
        return this;
    }

    public StatType getStatType() {
        return statType;
    }

    public ProgressStatsByDeployment setStatType(final StatType statType) {
        this.statType = statType;
        return this;
    }

    public Double getStatValue() {
        return statValue;
    }

    public ProgressStatsByDeployment setStatValue(final Double statValue) {
        this.statValue = statValue;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgressStatsByDeployment that = (ProgressStatsByDeployment) o;
        return Objects.equals(deploymentId, that.deploymentId) && Objects.equals(coursewareElementId,
                                                                                 that.coursewareElementId) && coursewareElementType == that.coursewareElementType && statType == that.statType && Objects.equals(
                statValue,
                that.statValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, coursewareElementId, coursewareElementType, statType, statValue);
    }

    @Override
    public String toString() {
        return "ProgressStatsByDeployment{" +
                "deploymentId=" + deploymentId +
                ", coursewareElementId=" + coursewareElementId +
                ", coursewareElementType=" + coursewareElementType +
                ", statType=" + statType +
                ", statValue=" + statValue +
                '}';
    }
}
