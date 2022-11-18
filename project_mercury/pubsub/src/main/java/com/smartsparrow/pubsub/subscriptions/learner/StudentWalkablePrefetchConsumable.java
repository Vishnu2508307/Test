package com.smartsparrow.pubsub.subscriptions.learner;

import com.smartsparrow.pubsub.data.AbstractConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

import javax.inject.Singleton;

/**
 * This RTM consumable describes a student walkable prefetch event
 */
@Singleton
public class StudentWalkablePrefetchConsumable extends AbstractConsumable<StudentWalkablePrefetchBroadcastMessage> {

    private static final long serialVersionUID = 323829735248229968L;

    public StudentWalkablePrefetchConsumable(StudentWalkablePrefetchBroadcastMessage content) {
        super(content);
    }

    @Override
    public String getName() {
        return String.format("student/%s/%s", content.studentId, getRTMEvent().getName());
    }

    @Override
    public String getSubscriptionName() {
        return String.format("student/%s", content.studentId);
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new StudentWalkablePrefetchRTMEvent();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
