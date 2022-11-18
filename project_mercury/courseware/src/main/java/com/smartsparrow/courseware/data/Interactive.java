package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.learner.data.Element;

public class Interactive implements Element, Walkable {

    private static final long serialVersionUID = 3894147771597209519L;
    private UUID id;

    // Plugin information.
    private UUID pluginId;
    private String pluginVersionExpr;

    private UUID studentScopeURN;
    private EvaluationMode evaluationMode;

    public Interactive() {
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.INTERACTIVE;
    }

    public Interactive setStudentScopeURN(UUID studentScopeURN) {
        this.studentScopeURN = studentScopeURN;
        return this;
    }

    @Override
    public UUID getStudentScopeURN() {
        return studentScopeURN;
    }

    public Interactive setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public UUID getPluginId() {
        return pluginId;
    }

    public Interactive setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    @Override
    public String getPluginVersionExpr() {
        return pluginVersionExpr;
    }

    public Interactive setPluginVersionExpr(String pluginVersionExpr) {
        this.pluginVersionExpr = pluginVersionExpr;
        return this;
    }

    @Override
    public EvaluationMode getEvaluationMode() {
        return evaluationMode;
    }

    public Interactive setEvaluationMode(EvaluationMode evaluationMode) {
        this.evaluationMode = evaluationMode;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interactive that = (Interactive) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(pluginVersionExpr, that.pluginVersionExpr) &&
                Objects.equals(evaluationMode, that.evaluationMode) &&
                Objects.equals(studentScopeURN, that.studentScopeURN);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pluginId, pluginVersionExpr, studentScopeURN, evaluationMode);
    }

    @Override
    public String toString() {
        return "Interactive{" +
                "id=" + id +
                ", pluginId=" + pluginId +
                ", pluginVersionExpr='" + pluginVersionExpr + '\'' +
                ", studentScopeURN=" + studentScopeURN +
                ", evaluationMode=" + evaluationMode +
                '}';
    }
}
