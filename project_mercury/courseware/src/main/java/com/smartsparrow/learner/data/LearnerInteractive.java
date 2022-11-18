package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.courseware.data.Interactive;

import io.leangen.graphql.annotations.GraphQLIgnore;

public class LearnerInteractive extends Interactive implements LearnerWalkable {

    private static final long serialVersionUID = 4795572953135802302L;

    private UUID deploymentId;
    private UUID changeId;
    private String config;
    private EvaluationMode evaluationMode;

    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.INTERACTIVE;
    }

    @GraphQLIgnore
    @Override
    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerInteractive setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @GraphQLIgnore
    @Override
    public UUID getChangeId() {
        return changeId;
    }

    public LearnerInteractive setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public String getConfig() {
        return config;
    }

    public LearnerInteractive setConfig(String config) {
        this.config = config;
        return this;
    }

    @Override
    public LearnerInteractive setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public LearnerInteractive setPluginId(UUID pluginId) {
        super.setPluginId(pluginId);
        return this;
    }

    @Override
    public LearnerInteractive setPluginVersionExpr(String pluginVersionExpr) {
        super.setPluginVersionExpr(pluginVersionExpr);
        return this;
    }

    @Override
    public LearnerInteractive setStudentScopeURN(UUID studentScopeURN) {
        super.setStudentScopeURN(studentScopeURN);
        return this;
    }

    @Override
    public EvaluationMode getEvaluationMode() {
        return evaluationMode;
    }

    @Override
    public LearnerInteractive setEvaluationMode(EvaluationMode evaluationMode) {
        this.evaluationMode = evaluationMode;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LearnerInteractive that = (LearnerInteractive) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(evaluationMode, that.evaluationMode) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deploymentId, changeId, config, evaluationMode);
    }

    @Override
    public String toString() {
        return "LearnerInteractive{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", config='" + config + '\'' +
                ", evaluationMode='" + evaluationMode + '\'' +
                "} " + super.toString();
    }
}
