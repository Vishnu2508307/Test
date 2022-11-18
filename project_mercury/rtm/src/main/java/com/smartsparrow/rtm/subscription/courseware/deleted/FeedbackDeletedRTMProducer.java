package com.smartsparrow.rtm.subscription.courseware.deleted;

import static com.smartsparrow.courseware.data.CoursewareElementType.FEEDBACK;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a deleted activity feedback
 */
public class FeedbackDeletedRTMProducer extends AbstractProducer<FeedbackDeletedRTMConsumable> {

    private FeedbackDeletedRTMConsumable feedbackDeletedRTMConsumable;

    @Inject
    public FeedbackDeletedRTMProducer() {
    }

    public FeedbackDeletedRTMProducer buildFeedbackDeletedRTMConsumable(RTMClientContext rtmClientContext,
                                                                        UUID activityId,
                                                                        UUID feedbackId) {
        this.feedbackDeletedRTMConsumable = new FeedbackDeletedRTMConsumable(rtmClientContext,
                                                                             new ActivityBroadcastMessage(activityId,
                                                                                                          feedbackId,
                                                                                                          FEEDBACK));
        return this;
    }

    @Override
    public FeedbackDeletedRTMConsumable getEventConsumable() {
        return feedbackDeletedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedbackDeletedRTMProducer that = (FeedbackDeletedRTMProducer) o;
        return Objects.equals(feedbackDeletedRTMConsumable, that.feedbackDeletedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackDeletedRTMConsumable);
    }

    @Override
    public String toString() {
        return "FeedbackDeletedRTMProducer{" +
                "feedbackDeletedRTMConsumable=" + feedbackDeletedRTMConsumable +
                '}';
    }
}
