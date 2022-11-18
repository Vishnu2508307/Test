package com.smartsparrow.rtm.subscription.courseware.created;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.FeedbackCreatedBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a newly create feedback
 */
public class FeedbackCreatedRTMProducer extends AbstractProducer<FeedbackCreatedRTMConsumable> {

    private FeedbackCreatedRTMConsumable feedbackCreatedRTMConsumable;

    @Inject
    public FeedbackCreatedRTMProducer() {
    }

    public FeedbackCreatedRTMProducer buildFeedbackCreatedRTMConsumable(RTMClientContext rtmClientContext,
                                                                        UUID activityId,
                                                                        UUID feedbackId) {
        this.feedbackCreatedRTMConsumable = new FeedbackCreatedRTMConsumable(rtmClientContext,
                                                                             new FeedbackCreatedBroadcastMessage(
                                                                                     activityId,
                                                                                     feedbackId));
        return this;
    }

    @Override
    public FeedbackCreatedRTMConsumable getEventConsumable() {
        return feedbackCreatedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedbackCreatedRTMProducer that = (FeedbackCreatedRTMProducer) o;
        return Objects.equals(feedbackCreatedRTMConsumable, that.feedbackCreatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackCreatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "FeedbackCreatedRTMProducer{" +
                "feedbackCreatedRTMConsumable=" + feedbackCreatedRTMConsumable +
                '}';
    }
}
