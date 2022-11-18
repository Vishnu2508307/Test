package com.smartsparrow.rtm.subscription.competency.deleted;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

public class DocumentDeletedRTMProducer extends AbstractProducer<DocumentDeletedRTMConsumable> {

    private DocumentDeletedRTMConsumable documentDeletedRTMConsumable;

    @Inject
    public DocumentDeletedRTMProducer() {
    }

    public DocumentDeletedRTMProducer buildDocumentDeletedRTMConsumable(RTMClientContext rtmClientContext, UUID documentId) {
        this.documentDeletedRTMConsumable = new DocumentDeletedRTMConsumable(rtmClientContext,
                                                                                new CompetencyDocumentBroadcastMessage()
                                                                                        .setDocumentId(documentId));
        return this;
    }

    @Override
    public DocumentDeletedRTMConsumable getEventConsumable() {
        return documentDeletedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentDeletedRTMProducer that = (DocumentDeletedRTMProducer) o;
        return Objects.equals(documentDeletedRTMConsumable, that.documentDeletedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentDeletedRTMConsumable);
    }

    @Override
    public String toString() {
        return "DocumentDeletedRTMProducer{" +
                "documentDeletedRTMConsumable=" + documentDeletedRTMConsumable +
                '}';
    }
}
