package com.smartsparrow.rtm.subscription.courseware.message;

import static com.smartsparrow.courseware.data.CoursewareElementType.FEEDBACK;

import java.util.UUID;

public class FeedbackCreatedBroadcastMessage extends ActivityBroadcastMessage {


    private static final long serialVersionUID = -5085744450789638362L;

    public FeedbackCreatedBroadcastMessage(UUID activityId, UUID feedbackId) {
        super(activityId, feedbackId, FEEDBACK);
    }

    @Override
    public String toString() {
        return "FeedbackCreatedBroadcastMessage{} " + super.toString();
    }
}
