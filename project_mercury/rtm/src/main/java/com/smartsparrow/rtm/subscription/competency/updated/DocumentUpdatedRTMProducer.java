package com.smartsparrow.rtm.subscription.competency.updated;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

public class DocumentUpdatedRTMProducer extends AbstractProducer<DocumentUpdatedRTMConsumable> {

    private DocumentUpdatedRTMConsumable documentUpdatedRTMConsumable;

    @Inject
    public DocumentUpdatedRTMProducer() {
    }

    public DocumentUpdatedRTMProducer buildDocumentUpdatedRTMConsumable(RTMClientContext rtmClientContext, UUID documentId){
        this.documentUpdatedRTMConsumable = new DocumentUpdatedRTMConsumable(rtmClientContext,
                                                                            new CompetencyDocumentBroadcastMessage()
                                                                                    .setDocumentId(documentId));
        return this;
    }

    @Override
    public DocumentUpdatedRTMConsumable getEventConsumable() {
        return documentUpdatedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentUpdatedRTMProducer that = (DocumentUpdatedRTMProducer) o;
        return Objects.equals(documentUpdatedRTMConsumable, that.documentUpdatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentUpdatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "DocumentUpdatedRTMProducer{" +
                "documentUpdatedRTMConsumable=" + documentUpdatedRTMConsumable +
                '}';
    }
}
