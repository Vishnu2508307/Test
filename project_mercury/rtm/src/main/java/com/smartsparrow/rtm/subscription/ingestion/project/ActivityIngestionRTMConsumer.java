package com.smartsparrow.rtm.subscription.ingestion.project;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.pubsub.subscriptions.activityIngestion.ActivityIngestionBroadcastMessage;
import com.smartsparrow.pubsub.subscriptions.activityIngestion.ActivityIngestionConsumable;
import com.smartsparrow.pubsub.subscriptions.activityIngestion.ActivityIngestionRTMEvent;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles ingestion status RTM event
 */
public class ActivityIngestionRTMConsumer implements RTMConsumer<ActivityIngestionConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ActivityIngestionRTMEvent();
    }
    /**
     * Writes a message to the websocket connection including the ingestion status
     *
     * @implNote As the broadcast is not restricted to update creator hence client id check is removed
     * @param rtmClient the rtm client that is listening to this consumer
     * @param activityIngestionConsumable the produced consumable
     */

    @Override
    public void accept(final RTMClient rtmClient, final ActivityIngestionConsumable activityIngestionConsumable) {
        ActivityIngestionBroadcastMessage message = activityIngestionConsumable.getContent();

        final String broadcastType = activityIngestionConsumable.getBroadcastType();
        final UUID subscriptionId = activityIngestionConsumable.getSubscriptionId();

        Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                .addField("rootElementId", message.getRootElementId())
                .addField("ingestionId", message.getIngestionId())
                .addField("projectId", message.getProjectId())
                .addField("ingestionStatus", message.getIngestionStatus())
                // TODO remove next line when FE supported
                .addField("action", getRTMEvent().getLegacyName())
                .addField("rtmEvent", getRTMEvent().getName()));
    }
}
