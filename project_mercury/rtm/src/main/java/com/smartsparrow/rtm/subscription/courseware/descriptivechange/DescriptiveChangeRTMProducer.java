package com.smartsparrow.rtm.subscription.courseware.descriptivechange;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.DescriptiveChangeBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a descriptive change activity
 */
public class DescriptiveChangeRTMProducer extends AbstractProducer<DescriptiveChangeRTMConsumable> {

    private DescriptiveChangeRTMConsumable descriptiveChangeRTMConsumable;

    @Inject
    public DescriptiveChangeRTMProducer() {
    }

    public DescriptiveChangeRTMProducer buildDescriptiveChangeRTMConsumable(RTMClientContext rtmClientContext,
                                                                            UUID activityId,
                                                                            UUID elementId,
                                                                            CoursewareElementType elementType,
                                                                            String value) {
        this.descriptiveChangeRTMConsumable = new DescriptiveChangeRTMConsumable(rtmClientContext,
                                                                                 new DescriptiveChangeBroadcastMessage(
                                                                                         activityId,
                                                                                         elementId,
                                                                                         elementType,
                                                                                         value));
        return this;
    }

    @Override
    public DescriptiveChangeRTMConsumable getEventConsumable() {
        return descriptiveChangeRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DescriptiveChangeRTMProducer that = (DescriptiveChangeRTMProducer) o;
        return Objects.equals(descriptiveChangeRTMConsumable, that.descriptiveChangeRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(descriptiveChangeRTMConsumable);
    }

    @Override
    public String toString() {
        return "DescriptiveChangeRTMProducer{" +
                "descriptiveChangeRTMConsumable=" + descriptiveChangeRTMConsumable +
                '}';
    }
}
