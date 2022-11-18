package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.learner.data.Element;

public class Component implements Element {

    private UUID id;

    // Plugin information.
    private UUID pluginId;
    @JsonProperty("pluginVersion")
    private String pluginVersionExpr;

    public Component() {
    }

    public UUID getId() {
        return id;
    }

    @Override
    public CoursewareElementType getElementType() {
        return CoursewareElementType.COMPONENT;
    }

    public Component setId(UUID id) {
        this.id = id;
        return this;
    }

    @Override
    public UUID getPluginId() {
        return pluginId;
    }

    public Component setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    @Override
    public String getPluginVersionExpr() {
        return pluginVersionExpr;
    }

    public Component setPluginVersionExpr(String pluginVersionExpr) {
        this.pluginVersionExpr = pluginVersionExpr;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Component component = (Component) o;
        return Objects.equals(id, component.id) &&
                Objects.equals(pluginId, component.pluginId) &&
                Objects.equals(pluginVersionExpr, component.pluginVersionExpr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pluginId, pluginVersionExpr);
    }

    @Override
    public String toString() {
        return "Component{" +
                "id=" + id +
                ", pluginId=" + pluginId +
                ", pluginVersionExpr='" + pluginVersionExpr + '\'' +
                '}';
    }
}
