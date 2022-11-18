package com.smartsparrow.rtm.subscription.courseware.configchange;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ConfigChangeBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a config changed activity
 */
public class ActivityConfigChangeRTMProducer extends AbstractProducer<ActivityConfigChangeRTMConsumable> {

    private ActivityConfigChangeRTMConsumable activityConfigChangeRTMConsumable;

    @Inject
    public ActivityConfigChangeRTMProducer() {
    }

    public ActivityConfigChangeRTMProducer buildActivityConfigChangeRTMConsumable(RTMClientContext rtmClientContext,
                                                                                  UUID rootElementId,
                                                                                  UUID activityId,
                                                                                  String config) {
        this.activityConfigChangeRTMConsumable = new ActivityConfigChangeRTMConsumable(rtmClientContext,
                                                                                       new ConfigChangeBroadcastMessage(
                                                                                               rootElementId,
                                                                                               activityId,
                                                                                               ACTIVITY,
                                                                                               config));
        return this;
    }

    @Override
    public ActivityConfigChangeRTMConsumable getEventConsumable() {
        return activityConfigChangeRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityConfigChangeRTMProducer that = (ActivityConfigChangeRTMProducer) o;
        return Objects.equals(activityConfigChangeRTMConsumable, that.activityConfigChangeRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityConfigChangeRTMConsumable);
    }

    @Override
    public String toString() {
        return "ActivityConfigChangeRTMProducer{" +
                "activityConfigChangeRTMConsumable=" + activityConfigChangeRTMConsumable +
                '}';
    }
}
