package com.smartsparrow.rtm.subscription.cohort.disenrolled;

import java.util.UUID;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;

import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the cohort dis enrolled RTM event
 */
public class CohortDisEnrolledRTMConsumer implements RTMConsumer<CohortDisEnrolledRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new CohortDisEnrolledRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the cohortId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param cohortDisEnrolledRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, CohortDisEnrolledRTMConsumable cohortDisEnrolledRTMConsumable) {
        final RTMClientContext producingRTMClientContext = cohortDisEnrolledRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            CohortBroadcastMessage message = cohortDisEnrolledRTMConsumable.getContent();

            final String broadcastType = cohortDisEnrolledRTMConsumable.getBroadcastType();
            final UUID subscriptionId = cohortDisEnrolledRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("cohortId", message.getCohortId())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
