package com.smartsparrow.learner.data;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.EvaluationMode;

import io.leangen.graphql.annotations.GraphQLIgnore;

public class LearnerActivity extends Activity implements LearnerWalkable {

    private static final long serialVersionUID = 8410540381846161684L;

    private UUID deploymentId;
    private UUID changeId;
    private String config;
    private String theme;
    private EvaluationMode evaluationMode;

    @JsonIgnore
    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.ACTIVITY;
    }

    @Override
    public LearnerActivity setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public LearnerActivity setPluginId(UUID pluginId) {
        super.setPluginId(pluginId);
        return this;
    }

    @Override
    public LearnerActivity setPluginVersionExpr(String pluginVersionExpr) {
        super.setPluginVersionExpr(pluginVersionExpr);
        return this;
    }

    @Override
    public LearnerActivity setCreatorId(UUID creatorId) {
        super.setCreatorId(creatorId);
        return this;
    }

    @GraphQLIgnore
    @Override
    public UUID getDeploymentId() {
        return deploymentId;
    }

    public LearnerActivity setDeploymentId(UUID deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    @GraphQLIgnore
    @Override
    public UUID getChangeId() {
        return changeId;
    }

    public LearnerActivity setChangeId(UUID changeId) {
        this.changeId = changeId;
        return this;
    }

    @Override
    public String getConfig() {
        return config;
    }

    public LearnerActivity setConfig(String config) {
        this.config = config;
        return this;
    }

    public String getTheme() {
        return theme;
    }

    public LearnerActivity setTheme(String theme) {
        this.theme = theme;
        return this;
    }

    @Override
    public LearnerActivity setStudentScopeURN(UUID studentScopeURN) {
        super.setStudentScopeURN(studentScopeURN);
        return this;
    }

    @Override
    public EvaluationMode getEvaluationMode() {
        return evaluationMode;
    }

    @Override
    public LearnerActivity setEvaluationMode(EvaluationMode evaluationMode) {
        this.evaluationMode = evaluationMode;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LearnerActivity that = (LearnerActivity) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(changeId, that.changeId) &&
                Objects.equals(config, that.config) &&
                Objects.equals(evaluationMode, that.evaluationMode) &&
                Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), deploymentId, changeId, config, theme, evaluationMode);
    }

    @Override
    public String toString() {
        return "LearnerActivity{" +
                "deploymentId=" + deploymentId +
                ", changeId=" + changeId +
                ", config='" + config + '\'' +
                ", theme='" + theme + '\'' +
                ", evaluationMode='" + evaluationMode + '\'' +
                "} " + super.toString();
    }
}
