package com.smartsparrow.rtm.subscription.courseware.configchange;

import static com.smartsparrow.courseware.data.CoursewareElementType.FEEDBACK;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.ConfigChangeBroadcastMessage;

/**
 * This RTM producer produces an RTM event for a config changed activity
 */
public class FeedbackConfigChangeRTMProducer extends AbstractProducer<FeedbackConfigChangeRTMConsumable> {

    private FeedbackConfigChangeRTMConsumable feedbackConfigChangeRTMConsumable;

    @Inject
    public FeedbackConfigChangeRTMProducer() {
    }

    public FeedbackConfigChangeRTMProducer buildFeedbackConfigChangeRTMConsumable(RTMClientContext rtmClientContext,
                                                                                  UUID activityId,
                                                                                  UUID feedbackId,
                                                                                  String config) {
        this.feedbackConfigChangeRTMConsumable = new FeedbackConfigChangeRTMConsumable(rtmClientContext,
                                                                                       new ConfigChangeBroadcastMessage(
                                                                                               activityId,
                                                                                               feedbackId,
                                                                                               FEEDBACK,
                                                                                               config));
        return this;
    }

    @Override
    public FeedbackConfigChangeRTMConsumable getEventConsumable() {
        return feedbackConfigChangeRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeedbackConfigChangeRTMProducer that = (FeedbackConfigChangeRTMProducer) o;
        return Objects.equals(feedbackConfigChangeRTMConsumable, that.feedbackConfigChangeRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackConfigChangeRTMConsumable);
    }

    @Override
    public String toString() {
        return "FeedbackConfigChangeRTMProducer{" +
                "feedbackConfigChangeRTMConsumable=" + feedbackConfigChangeRTMConsumable +
                '}';
    }
}
