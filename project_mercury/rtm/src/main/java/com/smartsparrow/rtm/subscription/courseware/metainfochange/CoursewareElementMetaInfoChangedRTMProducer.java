package com.smartsparrow.rtm.subscription.courseware.metainfochange;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a courseware element meta info change activity
 */
public class CoursewareElementMetaInfoChangedRTMProducer extends AbstractProducer<CoursewareElementMetaInfoChangedRTMConsumable> {

    private CoursewareElementMetaInfoChangedRTMConsumable coursewareElementMetaInfoChangedRTMConsumable;

    @Inject
    public CoursewareElementMetaInfoChangedRTMProducer() {
    }

    public CoursewareElementMetaInfoChangedRTMProducer buildCoursewareElementMetaInfoChangedRTMConsumable(
            RTMClientContext rtmClientContext,
            UUID activityId,
            UUID elementId,
            CoursewareElementType type) {
        this.coursewareElementMetaInfoChangedRTMConsumable = new CoursewareElementMetaInfoChangedRTMConsumable(
                rtmClientContext,
                new ActivityBroadcastMessage(activityId, elementId, type));
        return this;
    }

    @Override
    public CoursewareElementMetaInfoChangedRTMConsumable getEventConsumable() {
        return coursewareElementMetaInfoChangedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursewareElementMetaInfoChangedRTMProducer that = (CoursewareElementMetaInfoChangedRTMProducer) o;
        return Objects.equals(coursewareElementMetaInfoChangedRTMConsumable, that.coursewareElementMetaInfoChangedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coursewareElementMetaInfoChangedRTMConsumable);
    }

    @Override
    public String toString() {
        return "CoursewareElementMetaInfoChangedRTMProducer{" +
                "coursewareElementMetaInfoChangedRTMConsumable=" + coursewareElementMetaInfoChangedRTMConsumable +
                '}';
    }
}
