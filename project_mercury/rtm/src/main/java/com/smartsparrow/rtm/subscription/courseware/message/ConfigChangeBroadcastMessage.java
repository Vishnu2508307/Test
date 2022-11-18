package com.smartsparrow.rtm.subscription.courseware.message;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElementType;

public class ConfigChangeBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = 1499085534810285168L;
    private final String config;

    public ConfigChangeBroadcastMessage(final UUID activityId,
                                        final UUID elementId,
                                        final CoursewareElementType type,
                                        final String config) {
        super(activityId, elementId, type);
        this.config = config;
    }

    public String getConfig() {
        return config;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConfigChangeBroadcastMessage that = (ConfigChangeBroadcastMessage) o;
        return Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), config);
    }

    @Override
    public String toString() {
        return "ConfigChangedBroadcastMessage{" +
                "config=" + config +
                "} " + super.toString();
    }
}
