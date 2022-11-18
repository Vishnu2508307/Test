package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

public class InteractiveConfigId {

    private UUID configId;
    private UUID interactiveId;

    public InteractiveConfigId() {
    }
    public UUID getConfigId() {
        return configId;
    }

    public InteractiveConfigId setConfigId(UUID configId) {
        this.configId = configId;
        return this;
    }

    public UUID getInteractiveId() {
        return interactiveId;
    }

    public InteractiveConfigId setInteractiveId(UUID interactiveId) {
        this.interactiveId = interactiveId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InteractiveConfigId that = (InteractiveConfigId) o;
        return Objects.equals(configId, that.configId) && Objects.equals(interactiveId, that.interactiveId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configId, interactiveId);
    }

    @Override
    public String toString() {
        return "InteractiveConfigId{" +
                "configid=" + configId +
                ", interactiveId=" + interactiveId +
                '}';
    }
}
