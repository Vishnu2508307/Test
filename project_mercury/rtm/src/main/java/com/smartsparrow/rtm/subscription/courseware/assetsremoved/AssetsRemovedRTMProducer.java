package com.smartsparrow.rtm.subscription.courseware.assetsremoved;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;

/**
 * This RTM producer produces an RTM event for removed assets
 */
public class AssetsRemovedRTMProducer extends AbstractProducer<AssetsRemovedRTMConsumable> {

    private AssetsRemovedRTMConsumable assetsRemovedRTMConsumable;

    @Inject
    public AssetsRemovedRTMProducer() {
    }

    public AssetsRemovedRTMProducer buildAssetsRemovedRTMConsumable(RTMClientContext rtmClientContext,
                                                                    UUID activityId,
                                                                    UUID elementId,
                                                                    CoursewareElementType elementType) {
        this.assetsRemovedRTMConsumable = new AssetsRemovedRTMConsumable(rtmClientContext,
                                                                         new ActivityBroadcastMessage(
                                                                                 activityId,
                                                                                 elementId,
                                                                                 elementType));
        return this;
    }

    @Override
    public AssetsRemovedRTMConsumable getEventConsumable() {
        return assetsRemovedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetsRemovedRTMProducer that = (AssetsRemovedRTMProducer) o;
        return Objects.equals(assetsRemovedRTMConsumable, that.assetsRemovedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetsRemovedRTMConsumable);
    }

    @Override
    public String toString() {
        return "AssetsRemovedRTMProducer{" +
                "assetsRemovedRTMConsumable=" + assetsRemovedRTMConsumable +
                '}';
    }
}
