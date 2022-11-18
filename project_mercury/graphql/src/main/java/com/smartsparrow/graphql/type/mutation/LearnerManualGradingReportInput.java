package com.smartsparrow.graphql.type.mutation;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.leangen.graphql.annotations.GraphQLInputField;
import io.leangen.graphql.annotations.types.GraphQLType;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD")
@GraphQLType(name = "LearnerManualGradingReport", description = "Arguments for fetching a manual grading report")
public class LearnerManualGradingReportInput {

    private UUID deploymentId;
    private UUID parentId;
    private UUID componentId;
    private CoursewareElementType parentType;

    @GraphQLInputField(description = "The deployment the component belongs to")
    public UUID getDeploymentId() {
        return deploymentId;
    }

    @GraphQLInputField(description = "The parent element id. (required to fetch the latest attempt on the component)")
    public UUID getParentId() {
        return parentId;
    }

    @GraphQLInputField(description = "The component id to find the manual grading report for")
    public UUID getComponentId() {
        return componentId;
    }

    public CoursewareElementType getParentType() {
        return parentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerManualGradingReportInput that = (LearnerManualGradingReportInput) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(parentId, that.parentId) &&
                Objects.equals(componentId, that.componentId) &&
                parentType == that.parentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, parentId, componentId, parentType);
    }

    @Override
    public String toString() {
        return "LearnerManualGradingReportInput{" +
                "deploymentId=" + deploymentId +
                ", parentId=" + parentId +
                ", componentId=" + componentId +
                ", parentType=" + parentType +
                '}';
    }
}
