package com.smartsparrow.rtm.subscription.courseware.themechange;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ConfigChangeBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a theme changed activity
 */
public class ActivityThemeChangeRTMProducer extends AbstractProducer<ActivityThemeChangeRTMConsumable> {

    private ActivityThemeChangeRTMConsumable activityThemeChangeRTMConsumable;

    @Inject
    public ActivityThemeChangeRTMProducer() {
    }

    public ActivityThemeChangeRTMProducer buildActivityThemeChangeRTMConsumable(RTMClientContext rtmClientContext,
                                                                                UUID rootElementId,
                                                                                UUID activityId,
                                                                                String titleConfig) {
        this.activityThemeChangeRTMConsumable = new ActivityThemeChangeRTMConsumable(rtmClientContext,
                                                                                     new ConfigChangeBroadcastMessage(
                                                                                             rootElementId,
                                                                                             activityId,
                                                                                             ACTIVITY,
                                                                                             titleConfig));
        return this;
    }

    @Override
    public ActivityThemeChangeRTMConsumable getEventConsumable() {
        return activityThemeChangeRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityThemeChangeRTMProducer that = (ActivityThemeChangeRTMProducer) o;
        return Objects.equals(activityThemeChangeRTMConsumable, that.activityThemeChangeRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityThemeChangeRTMConsumable);
    }

    @Override
    public String toString() {
        return "ActivityThemeChangeRTMProducer{" +
                "activityThemeChangeRTMConsumable=" + activityThemeChangeRTMConsumable +
                '}';
    }
}
