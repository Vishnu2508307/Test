package com.smartsparrow.rtm.subscription.competency.updated;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

public class DocumentItemUpdatedRTMProducer extends AbstractProducer<DocumentItemUpdatedRTMConsumable> {

    private DocumentItemUpdatedRTMConsumable documentItemUpdatedRTMConsumable;

    @Inject
    public DocumentItemUpdatedRTMProducer() {
    }

    public DocumentItemUpdatedRTMProducer buildDocumentItemUpdatedRTMConsumable(RTMClientContext rtmClientContext, UUID documentId, UUID documentItemId) {
        this.documentItemUpdatedRTMConsumable = new DocumentItemUpdatedRTMConsumable(rtmClientContext, new CompetencyDocumentBroadcastMessage().setDocumentId(documentId).setDocumentItemId(documentItemId));
        return this;
    }

    @Override
    public DocumentItemUpdatedRTMConsumable getEventConsumable() {
        return documentItemUpdatedRTMConsumable;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentItemUpdatedRTMProducer that = (DocumentItemUpdatedRTMProducer) o;
        return Objects.equals(documentItemUpdatedRTMConsumable, that.documentItemUpdatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentItemUpdatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "DocumentItemUpdatedRTMProducer{" +
                "documentItemUpdatedRTMConsumable=" + documentItemUpdatedRTMConsumable +
                '}';
    }
}
