package com.smartsparrow.rtm.subscription.ingestion;

import java.util.UUID;

import com.smartsparrow.ingestion.subscription.IngestionBroadcastMessage;
import com.smartsparrow.ingestion.subscription.IngestionConsumable;
import com.smartsparrow.ingestion.subscription.IngestionRTMEvent;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the project ingestion RTM event
 */
public class IngestionRTMConsumer implements RTMConsumer<IngestionConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new IngestionRTMEvent();
    }

    /**
     * Writes a message to the websocket connection
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param ingestionConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, IngestionConsumable ingestionConsumable) {

        IngestionBroadcastMessage message = ingestionConsumable.getContent();

        final String broadcastType = ingestionConsumable.getBroadcastType();
        final UUID subscriptionId = ingestionConsumable.getSubscriptionId();

        Responses.writeReactive(rtmClient.getSession(),
                                new BasicResponseMessage(broadcastType, subscriptionId.toString())
                                        .addField("ingestionId", message.getIngestionId())
                                        .addField("ingestionStatus", message.getIngestionStatus())
                                        // TODO remove next line when FE supported
                                        .addField("action", getRTMEvent().getLegacyName())
                                        .addField("rtmEvent", getRTMEvent().getName()));

    }

}
