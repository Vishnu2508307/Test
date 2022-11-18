package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.learner.data.Element;

/**
 * This object represents an Activity within the CDP. This is an entity which can represent high level container items
 * such as a Course, Unit or Lesson.
 *
 * It contains:
 *  - Plugin Reference
 *  - Plugin Configuration
 *
 */
public class Activity implements Element, Walkable {

    private static final long serialVersionUID = -8383232483622866594L;

    private UUID id;

    // Plugin information.
    private UUID pluginId;
    private String pluginVersionExpr;// 1.0.* 1.0.2

    private UUID creatorId;
    private UUID studentScopeURN;
    private EvaluationMode evaluationMode;

    /**
     * Default empty constructor
     */
    @SuppressWarnings("unused")
    public Activity() {
    }

    public UUID getId() {
        return id;
    }

    @Override
    public UUID getStudentScopeURN() {
        return studentScopeURN;
    }

    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.ACTIVITY;
    }

    public Activity setStudentScopeURN(UUID studentScopeURN) {
        this.studentScopeURN = studentScopeURN;
        return this;
    }

    public Activity setId(UUID id) {
        this.id = id;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getPluginId() {
        return pluginId;
    }

    public Activity setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPluginVersionExpr() {
        return pluginVersionExpr;
    }

    public Activity setPluginVersionExpr(String pluginVersionExpr) {
        this.pluginVersionExpr = pluginVersionExpr;
        return this;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public Activity setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    @Override
    public EvaluationMode getEvaluationMode() {
        return evaluationMode;
    }

    public Activity setEvaluationMode(EvaluationMode evaluationMode) {
        this.evaluationMode = evaluationMode;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Activity activity = (Activity) o;
        return Objects.equals(id, activity.id) &&
                Objects.equals(pluginId, activity.pluginId) &&
                Objects.equals(pluginVersionExpr, activity.pluginVersionExpr) &&
                Objects.equals(creatorId, activity.creatorId) &&
                Objects.equals(studentScopeURN, activity.studentScopeURN) &&
                evaluationMode == activity.evaluationMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pluginId, pluginVersionExpr, creatorId, studentScopeURN, evaluationMode);
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id=" + id +
                ", pluginId=" + pluginId +
                ", pluginVersionExpr='" + pluginVersionExpr + '\'' +
                ", creatorId=" + creatorId +
                ", studentScopeURN=" + studentScopeURN +
                ", evaluationMode=" + evaluationMode +
                '}';
    }
}
