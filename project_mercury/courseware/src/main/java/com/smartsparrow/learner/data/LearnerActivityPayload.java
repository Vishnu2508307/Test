package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;

import io.leangen.graphql.annotations.GraphQLIgnore;

public class LearnerActivityPayload extends LearnerActivity implements LearnerWalkablePayload {

    private static final long serialVersionUID = 1421497322505119567L;

    public LearnerActivityPayload(final LearnerActivity activity) {
        this.setId(activity.getId());
        this.setChangeId(activity.getChangeId());
        this.setDeploymentId(activity.getDeploymentId());
        this.setConfig(activity.getConfig());
        this.setEvaluationMode(activity.getEvaluationMode());
        this.setPluginId(activity.getPluginId());
        this.setPluginVersionExpr(activity.getPluginVersionExpr());
        this.setStudentScopeURN(activity.getStudentScopeURN());
        this.setTheme(activity.getTheme());
        this.setCreatorId(activity.getCreatorId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public String toString() {
        return "LearnerActivityPayload{" +
                super.toString() +
                "}";
    }
}
