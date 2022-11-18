package com.smartsparrow.rtm.subscription.competency.updated;

import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.competency.payload.DocumentPayload;
import com.smartsparrow.competency.service.DocumentService;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

public class DocumentUpdatedRTMConsumer implements RTMConsumer<DocumentUpdatedRTMConsumable> {

    @Inject
    private DocumentService documentService;

    @Override
    public RTMEvent getRTMEvent() {
        return new DocumentUpdatedRTMEvent();
    }

    @Override
    public void accept(final RTMClient rtmClient,
                       final DocumentUpdatedRTMConsumable documentUpdatedRTMConsumable) {

        CompetencyDocumentBroadcastMessage message = documentUpdatedRTMConsumable.getContent();

        final String broadcastType = documentUpdatedRTMConsumable.getBroadcastType();
        final UUID subscriptionId = documentUpdatedRTMConsumable.getSubscriptionId();
        final DocumentPayload documentPayload = documentService.fetchDocument(message.getDocumentId())
                                                                .map(DocumentPayload::from)
                                                                .block();
        Responses.writeReactive(rtmClient.getSession(),
                new BasicResponseMessage(broadcastType, subscriptionId.toString())
                        .addField("data", documentPayload)
                        // TODO remove next line when FE supported
                        .addField("action", getRTMEvent().getLegacyName())
                        .addField("rtmEvent", getRTMEvent().getName()));
    }

}
