package com.smartsparrow.rtm.subscription.learner.studentprogress;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressBroadcastMessage;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressRTMConsumable;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressRTMEvent;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

public class StudentProgressRTMConsumer implements RTMConsumer<StudentProgressRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new StudentProgressRTMEvent();
    }

    @Override
    public void accept(final RTMClient rtmClient,
                       final StudentProgressRTMConsumable studentProgressRTMConsumable) {

        StudentProgressBroadcastMessage content = studentProgressRTMConsumable.getContent();
        final String broadcastType = studentProgressRTMConsumable.getBroadcastType();
        final UUID subscriptionId = studentProgressRTMConsumable.getSubscriptionId();

        Responses.writeReactive(rtmClient.getSession(),
                                new BasicResponseMessage(broadcastType, subscriptionId.toString())
                                        .addField("progress", content.getProgress())
                                        .addField("rtmEvent", getRTMEvent().getName()));
    }
}


