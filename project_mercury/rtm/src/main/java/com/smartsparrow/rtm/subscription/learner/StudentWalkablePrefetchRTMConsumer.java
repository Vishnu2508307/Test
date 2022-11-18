package com.smartsparrow.rtm.subscription.learner;

import java.util.UUID;

import com.smartsparrow.pubsub.subscriptions.learner.StudentWalkablePrefetchBroadcastMessage;
import com.smartsparrow.pubsub.subscriptions.learner.StudentWalkablePrefetchConsumable;
import com.smartsparrow.pubsub.subscriptions.learner.StudentWalkablePrefetchRTMEvent;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the project created RTM event
 */
public class StudentWalkablePrefetchRTMConsumer implements RTMConsumer<StudentWalkablePrefetchConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new StudentWalkablePrefetchRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the studentId and config for the fetched walkable
     * and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param studentWalkablePrefetchRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, StudentWalkablePrefetchConsumable studentWalkablePrefetchRTMConsumable) {
        StudentWalkablePrefetchBroadcastMessage message = studentWalkablePrefetchRTMConsumable.getContent();

        final String broadcastType = studentWalkablePrefetchRTMConsumable.getBroadcastType();
        final UUID subscriptionId = studentWalkablePrefetchRTMConsumable.getSubscriptionId();
        // TODO: The consumable provides the current walkable. Need to add a service call here to find next walkable
        // TODO: and return that in the broadcast message.

        Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                .addField("studentId", message.getStudentId())
                .addField("walkable", message.getWalkable())
                // TODO remove next line when FE supported
                .addField("action", getRTMEvent().getLegacyName())
                .addField("rtmEvent", getRTMEvent().getName()));
    }

}
