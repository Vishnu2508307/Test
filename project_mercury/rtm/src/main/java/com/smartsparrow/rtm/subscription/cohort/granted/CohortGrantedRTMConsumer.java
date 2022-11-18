package com.smartsparrow.rtm.subscription.cohort.granted;

import java.util.UUID;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the cohort granted RTM event
 */
public class CohortGrantedRTMConsumer implements RTMConsumer<CohortGrantedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new CohortGrantedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the cohortId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param cohortGrantedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, CohortGrantedRTMConsumable cohortGrantedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = cohortGrantedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            CohortBroadcastMessage message = cohortGrantedRTMConsumable.getContent();

            final String broadcastType = cohortGrantedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = cohortGrantedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("cohortId", message.getCohortId())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
