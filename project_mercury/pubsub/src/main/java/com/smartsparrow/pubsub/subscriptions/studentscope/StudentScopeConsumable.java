package com.smartsparrow.pubsub.subscriptions.studentscope;

import com.smartsparrow.pubsub.data.AbstractConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

public class StudentScopeConsumable extends AbstractConsumable<StudentScopeBroadcastMessage> {

    private static final long serialVersionUID = 3734418037192964047L;

    public StudentScopeConsumable(final StudentScopeBroadcastMessage content) {
        super(content);
    }

    @Override
    public String getName() {
        return String.format("learner.student.scope/%s/%s", content.getStudentId(), content.getDeploymentId());
    }

    @Override
    public String getSubscriptionName() {
        return String.format("learner.student.scope/%s/%s", content.getStudentId(), content.getDeploymentId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new StudentScopeRTMEvent();
    }
}
