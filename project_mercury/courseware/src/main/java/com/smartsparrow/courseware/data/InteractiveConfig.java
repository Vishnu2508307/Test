package com.smartsparrow.courseware.data;

import java.util.UUID;

import com.google.common.base.Objects;

public class InteractiveConfig {

    private UUID id;
    private UUID interactiveId;
    private String config;

    public InteractiveConfig() {
    }

    public UUID getId() {
        return id;
    }

    public InteractiveConfig setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getInteractiveId() {
        return interactiveId;
    }

    public InteractiveConfig setInteractiveId(UUID interactiveId) {
        this.interactiveId = interactiveId;
        return this;
    }

    public String getConfig() {
        return config;
    }

    public InteractiveConfig setConfig(String config) {
        this.config = config;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InteractiveConfig that = (InteractiveConfig) o;
        return Objects.equal(id, that.id) && Objects.equal(interactiveId, that.interactiveId) && Objects.equal(config,
                that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, interactiveId, config);
    }

    @Override
    public String toString() {
        return "InteractiveConfig{" +
                "id=" + id +
                ", interactiveId=" + interactiveId +
                ", config='" + config + '\'' +
                '}';
    }
}
