package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class PathwayConfig {

    private UUID id;
    private UUID pathwayId;
    private String config;

    public UUID getId() {
        return id;
    }

    public PathwayConfig setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getPathwayId() {
        return pathwayId;
    }

    public PathwayConfig setPathwayId(UUID pathwayId) {
        this.pathwayId = pathwayId;
        return this;
    }

    public String getConfig() {
        return config;
    }

    public PathwayConfig setConfig(String config) {
        this.config = config;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathwayConfig that = (PathwayConfig) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(pathwayId, that.pathwayId) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, pathwayId, config);
    }

    @Override
    public String toString() {
        return "PathwayConfig{" +
                "id=" + id +
                ", pathwayId=" + pathwayId +
                ", config='" + config + '\'' +
                '}';
    }
}
