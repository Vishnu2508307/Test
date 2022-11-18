package com.smartsparrow.rtm.subscription.competency.deleted;

import java.util.HashMap;
import java.util.UUID;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

public class DocumentDeletedRTMConsumer implements RTMConsumer<DocumentDeletedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new DocumentDeletedRTMEvent();
    }

    @Override
    public void accept(RTMClient rtmClient, DocumentDeletedRTMConsumable documentDeletedRTMConsumable) {
        CompetencyDocumentBroadcastMessage message = documentDeletedRTMConsumable.getContent();

        final String broadcastType = documentDeletedRTMConsumable.getBroadcastType();
        final UUID subscriptionId = documentDeletedRTMConsumable.getSubscriptionId();
        final HashMap<String, Object> data = new HashMap<>(){
            {
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
