package com.smartsparrow.rtm.subscription.courseware.message;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;

import java.util.Objects;
import java.util.UUID;

public class ComponentManualGradingBroadcastMessage extends ActivityBroadcastMessage {

    private static final long serialVersionUID = -3550999488937527405L;

    private final ManualGradingConfig manualGradingConfig;

    public ComponentManualGradingBroadcastMessage(UUID activityId,
                                                  UUID componentId,
                                                  ManualGradingConfig manualGradingConfig) {
        super(activityId, componentId, COMPONENT);
        this.manualGradingConfig = manualGradingConfig;
    }

    public ManualGradingConfig getManualGradingConfig() {
        return manualGradingConfig;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ComponentManualGradingBroadcastMessage that = (ComponentManualGradingBroadcastMessage) o;
        return Objects.equals(manualGradingConfig, that.manualGradingConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), manualGradingConfig);
    }

    @Override
    public String toString() {
        return "ComponentManualGradingBroadcastMessage{" +
                "manualGradingConfig=" + manualGradingConfig +
                "} " + super.toString();
    }
}
