package com.smartsparrow.rtm.subscription.competency.created;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

public class DocumentItemCreatedRTMProducer extends AbstractProducer<DocumentItemCreatedRTMConsumable> {

    private DocumentItemCreatedRTMConsumable documentItemCreatedRTMConsumable;

    @Inject
    public DocumentItemCreatedRTMProducer() {
    }

    public DocumentItemCreatedRTMProducer buildDocumentItemCreatedRTMConsumable(RTMClientContext rtmClientContext, UUID documentId, UUID documentItemId) {
        this.documentItemCreatedRTMConsumable = new DocumentItemCreatedRTMConsumable(rtmClientContext, new CompetencyDocumentBroadcastMessage().setDocumentId(documentId).setDocumentItemId(documentItemId));
        return this;
    }

    @Override
    public DocumentItemCreatedRTMConsumable getEventConsumable() {
        return documentItemCreatedRTMConsumable;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentItemCreatedRTMProducer that = (DocumentItemCreatedRTMProducer) o;
        return Objects.equals(documentItemCreatedRTMConsumable, that.documentItemCreatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentItemCreatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "DocumentItemCreatedRTMProducer{" +
                "documentItemCreatedRTMConsumable=" + documentItemCreatedRTMConsumable +
                '}';
    }
}
