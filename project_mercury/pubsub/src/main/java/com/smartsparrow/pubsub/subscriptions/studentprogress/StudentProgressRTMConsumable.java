package com.smartsparrow.pubsub.subscriptions.studentprogress;

import com.smartsparrow.pubsub.data.AbstractConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

/**
 * This consumable describes a student progress event
 */
public class StudentProgressRTMConsumable extends AbstractConsumable<StudentProgressBroadcastMessage> {

    private static final long serialVersionUID = 2785454469594096593L;

    public StudentProgressRTMConsumable(final StudentProgressBroadcastMessage content) {
        super(content);
    }

    @Override
    public String getName() {
        return String.format("learner.progress/%s/%s/%s/%s",
                             content.getStudentId(),
                             content.getDeploymentId(),
                             content.getCoursewareElementId(),
                             getRTMEvent().getName());
    }

    @Override
    public String getSubscriptionName() {
        return String.format("learner.progress/%s/%s/%s",
                             content.getStudentId(),
                             content.getDeploymentId(),
                             content.getCoursewareElementId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new StudentProgressRTMEvent();
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
