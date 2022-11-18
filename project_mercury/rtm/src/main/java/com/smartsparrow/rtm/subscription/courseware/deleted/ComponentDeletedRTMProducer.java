package com.smartsparrow.rtm.subscription.courseware.deleted;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a deleted activity component
 */
public class ComponentDeletedRTMProducer extends AbstractProducer<ComponentDeletedRTMConsumable> {

    private ComponentDeletedRTMConsumable componentDeletedRTMConsumable;

    @Inject
    public ComponentDeletedRTMProducer() {
    }

    public ComponentDeletedRTMProducer buildComponentDeletedRTMConsumable(RTMClientContext rtmClientContext,
                                                                          UUID activityId,
                                                                          UUID componentId) {
        this.componentDeletedRTMConsumable = new ComponentDeletedRTMConsumable(rtmClientContext,
                                                                               new ActivityBroadcastMessage(activityId,
                                                                                                            componentId,
                                                                                                            CoursewareElementType.COMPONENT));
        return this;
    }

    @Override
    public ComponentDeletedRTMConsumable getEventConsumable() {
        return componentDeletedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentDeletedRTMProducer that = (ComponentDeletedRTMProducer) o;
        return Objects.equals(componentDeletedRTMConsumable, that.componentDeletedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentDeletedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ComponentDeletedRTMProducer{" +
                "componentDeletedRTMConsumable=" + componentDeletedRTMConsumable +
                '}';
    }
}
