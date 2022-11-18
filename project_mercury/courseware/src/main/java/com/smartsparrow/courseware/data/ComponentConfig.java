package com.smartsparrow.courseware.data;

import java.util.UUID;

import com.google.common.base.Objects;

public class ComponentConfig {
    private UUID id;
    private UUID componentId;
    private String config;

    public ComponentConfig() {
    }

    public UUID getId() {
        return id;
    }

    public ComponentConfig setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getComponentId() {
        return componentId;
    }

    public ComponentConfig setComponentId(UUID componentId) {
        this.componentId = componentId;
        return this;
    }

    public String getConfig() {
        return config;
    }

    public ComponentConfig setConfig(String config) {
        this.config = config;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ComponentConfig that = (ComponentConfig) o;
        return Objects.equal(id, that.id) && Objects.equal(componentId, that.componentId) && Objects.equal(config,
                that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, componentId, config);
    }

    @Override
    public String toString() {
        return "ComponentConfig{" +
                "id=" + id +
                ", componentId=" + componentId +
                ", config='" + config + '\'' +
                '}';
    }
}
