package com.smartsparrow.rtm.subscription.cohort.unarchived;

import java.util.UUID;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the cohort unarchived RTM event
 */
public class CohortUnArchivedRTMConsumer implements RTMConsumer<CohortUnArchivedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new CohortUnArchivedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the cohortId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param cohortUnArchivedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, CohortUnArchivedRTMConsumable cohortUnArchivedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = cohortUnArchivedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            CohortBroadcastMessage message = cohortUnArchivedRTMConsumable.getContent();

            final String broadcastType = cohortUnArchivedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = cohortUnArchivedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("cohortId", message.getCohortId())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
