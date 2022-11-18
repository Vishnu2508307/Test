package com.smartsparrow.rtm.subscription.competency.association.created;

import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.competency.service.ItemAssociationService;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

public class CompetencyItemAssociationCreatedRTMConsumer implements RTMConsumer<CompetencyItemAssociationCreatedRTMConsumable> {

    @Inject
    private ItemAssociationService itemAssociationService;

    @Override
    public RTMEvent getRTMEvent() {
        return new CompetencyItemAssociationCreatedRTMEvent();
    }

    @Override
    public void accept(final RTMClient rtmClient,
                       final CompetencyItemAssociationCreatedRTMConsumable associationCreatedRTMConsumable) {
        CompetencyDocumentBroadcastMessage message = associationCreatedRTMConsumable.getContent();

        final String broadcastType = associationCreatedRTMConsumable.getBroadcastType();
        final UUID subscriptionId = associationCreatedRTMConsumable.getSubscriptionId();
        final ItemAssociation itemAssociation = itemAssociationService.findById(message.getAssociationId()).block();

        Responses.writeReactive(rtmClient.getSession(),
                                new BasicResponseMessage(broadcastType, subscriptionId.toString())
                                        .addField("data", itemAssociation)
                                        // TODO remove next line when FE supported
                                        .addField("action", getRTMEvent().getLegacyName())
                                        .addField("rtmEvent", getRTMEvent().getName()));
    }
}


