package com.smartsparrow.rtm.subscription.cohort.revoked;

import java.util.UUID;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the cohort revoked RTM event
 */
public class CohortRevokedRTMConsumer implements RTMConsumer<CohortRevokedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new CohortRevokedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the cohortId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param cohortRevokedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, CohortRevokedRTMConsumable cohortRevokedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = cohortRevokedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            CohortBroadcastMessage message = cohortRevokedRTMConsumable.getContent();

            final String broadcastType = cohortRevokedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = cohortRevokedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(),
                                    new BasicResponseMessage(broadcastType, subscriptionId.toString())
                                            .addField("cohortId", message.getCohortId())
                                            // TODO remove next line when FE supported
                                            .addField("action", getRTMEvent().getLegacyName())
                                            .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
