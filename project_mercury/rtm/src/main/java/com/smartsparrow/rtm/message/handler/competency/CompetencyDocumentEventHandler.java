package com.smartsparrow.rtm.message.handler.competency;

import static com.smartsparrow.rtm.route.RTMRoutes.COMPETENCY_DOCUMENT_EVENT_MESSAGE;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.rtm.subscription.competency.association.created.CompetencyItemAssociationCreatedRTMProducer;
import com.smartsparrow.rtm.subscription.competency.association.deleted.CompetencyItemAssociationDeletedRTMProducer;
import com.smartsparrow.rtm.subscription.competency.created.DocumentItemCreatedRTMProducer;
import com.smartsparrow.rtm.subscription.competency.deleted.DocumentDeletedRTMProducer;
import com.smartsparrow.rtm.subscription.competency.deleted.DocumentItemDeletedRTMProducer;
import com.smartsparrow.rtm.subscription.competency.updated.DocumentItemUpdatedRTMProducer;
import com.smartsparrow.rtm.subscription.competency.updated.DocumentUpdatedRTMProducer;

public class CompetencyDocumentEventHandler {

    private final DocumentItemCreatedRTMProducer documentItemCreatedRTMProducer;
    private final DocumentItemUpdatedRTMProducer documentItemUpdatedRTMProducer;
    private final DocumentItemDeletedRTMProducer documentItemDeletedRTMProducer;
    private final CompetencyItemAssociationCreatedRTMProducer associationCreatedRTMProducer;
    private final CompetencyItemAssociationDeletedRTMProducer associationDeletedRTMProducer;
    private final DocumentUpdatedRTMProducer documentUpdatedRTMProducer;
    private final DocumentDeletedRTMProducer documentDeletedRTMProducer;

    @Inject
    public CompetencyDocumentEventHandler(final DocumentItemCreatedRTMProducer documentItemCreatedRTMProducer,
                                          final DocumentItemUpdatedRTMProducer documentItemUpdatedRTMProducer,
                                          final DocumentItemDeletedRTMProducer documentItemDeletedRTMProducer,
                                          final CompetencyItemAssociationCreatedRTMProducer associationCreatedRTMProducer,
                                          final CompetencyItemAssociationDeletedRTMProducer associationDeletedRTMProducer,
                                          final DocumentUpdatedRTMProducer documentUpdatedRTMProducer,
                                          final DocumentDeletedRTMProducer documentDeletedRTMProducer) {
        this.documentItemCreatedRTMProducer = documentItemCreatedRTMProducer;
        this.documentItemUpdatedRTMProducer = documentItemUpdatedRTMProducer;
        this.documentItemDeletedRTMProducer = documentItemDeletedRTMProducer;
        this.associationCreatedRTMProducer = associationCreatedRTMProducer;
        this.associationDeletedRTMProducer = associationDeletedRTMProducer;
        this.documentUpdatedRTMProducer = documentUpdatedRTMProducer;
        this.documentDeletedRTMProducer = documentDeletedRTMProducer;
    }

    /**
     * Handle the competency document update event. This method calls corresponding producer.produce() based on the
     * competency document action set in the broadcast message
     *
     * @param exchange the camel exchange
     */
    @Handler
    public void handle(Exchange exchange) {
        // get the broadcast message from the exchange
        CompetencyDocumentBroadcastMessage message = exchange.getProperty(COMPETENCY_DOCUMENT_EVENT_MESSAGE, CompetencyDocumentBroadcastMessage.class);

        switch(message.getAction()) {
            case DOCUMENT_ITEM_CREATED:
                documentItemCreatedRTMProducer.buildDocumentItemCreatedRTMConsumable(null, message.getDocumentId(), message.getDocumentItemId())
                        .produce();
                break;
            case DOCUMENT_ITEM_UPDATED:
                documentItemUpdatedRTMProducer.buildDocumentItemUpdatedRTMConsumable(null, message.getDocumentId(), message.getDocumentItemId())
                        .produce();
                break;
            case DOCUMENT_ITEM_DELETED:
                documentItemDeletedRTMProducer.buildDocumentItemDeletedRTMConsumable(null, message.getDocumentId(), message.getDocumentItemId())
                        .produce();
                break;
            case ASSOCIATION_CREATED:
                associationCreatedRTMProducer.buildAssociationCreatedRTMConsumable(null, message.getAssociationId(), message.getDocumentId())
                        .produce();
                break;
            case ASSOCIATION_DELETED:
                associationDeletedRTMProducer.buildAssociationDeletedRTMConsumable(null, message.getAssociationId(), message.getDocumentId())
                        .produce();
                break;
            case DOCUMENT_UPDATED:
                documentUpdatedRTMProducer.buildDocumentUpdatedRTMConsumable(null, message.getDocumentId())
                        .produce();
                break;
            case DOCUMENT_DELETED:
                documentDeletedRTMProducer.buildDocumentDeletedRTMConsumable(null, message.getDocumentId())
                        .produce();
                break;
            default:
                throw new UnsupportedOperationException();
        }

    }

}
