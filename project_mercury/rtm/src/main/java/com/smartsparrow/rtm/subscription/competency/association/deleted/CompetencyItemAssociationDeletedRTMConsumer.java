package com.smartsparrow.rtm.subscription.competency.association.deleted;

import java.util.HashMap;
import java.util.UUID;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

public class CompetencyItemAssociationDeletedRTMConsumer implements RTMConsumer<CompetencyItemAssociationDeletedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new CompetencyItemAssociationDeletedRTMEvent();
    }

    @Override
    public void accept(final RTMClient rtmClient,
                       final CompetencyItemAssociationDeletedRTMConsumable associationDeletedRTMConsumable) {
        CompetencyDocumentBroadcastMessage message = associationDeletedRTMConsumable.getContent();

        final String broadcastType = associationDeletedRTMConsumable.getBroadcastType();
        final UUID subscriptionId = associationDeletedRTMConsumable.getSubscriptionId();
        final HashMap<String, Object> data = new HashMap<>() {
            {
                put("id", message.getAssociationId());
                put("documentId", message.getDocumentId());
            }
        };

        Responses.writeReactive(rtmClient.getSession(),
                                new BasicResponseMessage(broadcastType, subscriptionId.toString())
                                        .addField("data", data)
                                        // TODO remove next line when FE supported
                                        .addField("action", getRTMEvent().getLegacyName())
                                        .addField("rtmEvent", getRTMEvent().getName()));
    }
}


