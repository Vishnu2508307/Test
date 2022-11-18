package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.learner.data.Element;

public class Feedback implements Element {

    private UUID id;
    private UUID pluginId;
    @JsonProperty("pluginVersion")
    private String pluginVersionExpr;

    public Feedback() {
    }

    public UUID getId() {
        return id;
    }

    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.FEEDBACK;
    }

    public Feedback setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public UUID getPluginId() {
        return pluginId;
    }


    public Feedback setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    @Override
    public String getPluginVersionExpr() {
        return pluginVersionExpr;
    }

    public Feedback setPluginVersionExpr(String pluginVersionExpr) {
        this.pluginVersionExpr = pluginVersionExpr;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feedback feedback = (Feedback) o;
        return Objects.equals(id, feedback.id) &&
                Objects.equals(pluginId, feedback.pluginId) &&
                Objects.equals(pluginVersionExpr, feedback.pluginVersionExpr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pluginId, pluginVersionExpr);
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "id=" + id +
                ", pluginId=" + pluginId +
                ", pluginVersionExpr='" + pluginVersionExpr + '\'' +
                '}';
    }
}
