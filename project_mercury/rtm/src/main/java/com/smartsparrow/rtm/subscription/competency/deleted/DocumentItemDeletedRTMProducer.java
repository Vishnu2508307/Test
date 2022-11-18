package com.smartsparrow.rtm.subscription.competency.deleted;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

public class DocumentItemDeletedRTMProducer extends AbstractProducer<DocumentItemDeletedRTMConsumable> {

    private DocumentItemDeletedRTMConsumable documentItemDeletedRTMConsumable;

    @Inject
    public DocumentItemDeletedRTMProducer() {
    }

    public DocumentItemDeletedRTMProducer buildDocumentItemDeletedRTMConsumable(RTMClientContext rtmClientContext, UUID documentId, UUID documentItemId) {
        this.documentItemDeletedRTMConsumable = new DocumentItemDeletedRTMConsumable(rtmClientContext, new CompetencyDocumentBroadcastMessage().setDocumentId(documentId).setDocumentItemId(documentItemId));
        return this;
    }

    @Override
    public DocumentItemDeletedRTMConsumable getEventConsumable() {
        return documentItemDeletedRTMConsumable;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentItemDeletedRTMProducer that = (DocumentItemDeletedRTMProducer) o;
        return Objects.equals(documentItemDeletedRTMConsumable, that.documentItemDeletedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentItemDeletedRTMConsumable);
    }

    @Override
    public String toString() {
        return "DocumentItemDeletedRTMProducer{" +
                "documentItemDeletedRTMConsumable=" + documentItemDeletedRTMConsumable +
                '}';
    }
}
