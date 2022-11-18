package com.smartsparrow.rtm.subscription.competency.created;

import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

public class DocumentItemCreatedRTMConsumer implements RTMConsumer<DocumentItemCreatedRTMConsumable> {

    @Inject
    private DocumentItemService documentItemService;

    @Override
    public RTMEvent getRTMEvent() {
        return new DocumentItemCreatedRTMEvent();
    }

    @Override
    public void accept(final RTMClient rtmClient,
                       final DocumentItemCreatedRTMConsumable documentItemCreatedRTMConsumable) {
        CompetencyDocumentBroadcastMessage message = documentItemCreatedRTMConsumable.getContent();

        final String broadcastType = documentItemCreatedRTMConsumable.getBroadcastType();
        final UUID subscriptionId = documentItemCreatedRTMConsumable.getSubscriptionId();
        final DocumentItemPayload documentItemPayload = documentItemService.getDocumentItemPayload(message.getDocumentItemId()).block();

        Responses.writeReactive(rtmClient.getSession(),
                                new BasicResponseMessage(broadcastType, subscriptionId.toString())
                                        .addField("data", documentItemPayload)
                                        // TODO remove next line when FE supported
                                        .addField("action", getRTMEvent().getLegacyName())
                                        .addField("rtmEvent", getRTMEvent().getName()));
    }
}


