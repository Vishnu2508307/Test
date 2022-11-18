package com.smartsparrow.rtm.subscription.learner.studentscope;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.pubsub.subscriptions.studentscope.StudentScopeBroadcastMessage;
import com.smartsparrow.pubsub.subscriptions.studentscope.StudentScopeConsumable;
import com.smartsparrow.pubsub.subscriptions.studentscope.StudentScopeRTMEvent;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

public class StudentScopeRTMConsumer implements RTMConsumer<StudentScopeConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new StudentScopeRTMEvent();
    }

    @Override
    public void accept(final RTMClient rtmClient, final StudentScopeConsumable studentScopeConsumable) {

            StudentScopeBroadcastMessage message = studentScopeConsumable.getContent();
            final String broadcastType = studentScopeConsumable.getBroadcastType();
            final UUID subscriptionId = studentScopeConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(),
                                    new BasicResponseMessage(broadcastType, subscriptionId.toString())
                                            .addField("deploymentId", message.getDeploymentId())
                                            .addField("studentScopeURN", message.getStudentScopeUrn())
                                            .addField("studentScope", message.getStudentScopeEntry())
                                            .addField("rtmEvent", getRTMEvent().getName()));
    }
}
