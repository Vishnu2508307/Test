package com.smartsparrow.rtm.subscription.courseware.moved;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ElementMovedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a moved activity
 */
public class ActivityMovedRTMProducer extends AbstractProducer<ActivityMovedRTMConsumable> {

    private ActivityMovedRTMConsumable activityMovedRTMConsumable;

    @Inject
    public ActivityMovedRTMProducer() {
    }

    public ActivityMovedRTMProducer buildActivityMovedRTMConsumable(RTMClientContext rtmClientContext,
                                                                    UUID rootElementId,
                                                                    UUID activityId,
                                                                    UUID fromPathwayId,
                                                                    UUID toPathwayId) {
        this.activityMovedRTMConsumable = new ActivityMovedRTMConsumable(rtmClientContext,
                                                                         new ElementMovedBroadcastMessage(rootElementId,
                                                                                                          activityId,
                                                                                                          ACTIVITY,
                                                                                                          fromPathwayId,
                                                                                                          toPathwayId));
        return this;
    }

    @Override
    public ActivityMovedRTMConsumable getEventConsumable() {
        return activityMovedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityMovedRTMProducer that = (ActivityMovedRTMProducer) o;
        return Objects.equals(activityMovedRTMConsumable, that.activityMovedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityMovedRTMConsumable);
    }

    @Override
    public String toString() {
        return "ActivityMovedRTMProducer{" +
                "activityMovedRTMConsumable=" + activityMovedRTMConsumable +
                '}';
    }
}
