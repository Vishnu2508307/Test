package com.smartsparrow.rtm.subscription.competency.association.deleted;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

public class CompetencyItemAssociationDeletedRTMProducer extends AbstractProducer<CompetencyItemAssociationDeletedRTMConsumable> {

    private CompetencyItemAssociationDeletedRTMConsumable associationDeletedRTMConsumable;

    @Inject
    public CompetencyItemAssociationDeletedRTMProducer() {
    }

    public CompetencyItemAssociationDeletedRTMProducer buildAssociationDeletedRTMConsumable(RTMClientContext rtmClientContext, UUID associationId, UUID documentId) {
        this.associationDeletedRTMConsumable = new CompetencyItemAssociationDeletedRTMConsumable(rtmClientContext, new CompetencyDocumentBroadcastMessage().setAssociationId(associationId).setDocumentId(documentId));
        return this;
    }

    @Override
    public CompetencyItemAssociationDeletedRTMConsumable getEventConsumable() {
        return associationDeletedRTMConsumable;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencyItemAssociationDeletedRTMProducer that = (CompetencyItemAssociationDeletedRTMProducer) o;
        return Objects.equals(associationDeletedRTMConsumable, that.associationDeletedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(associationDeletedRTMConsumable);
    }

    @Override
    public String toString() {
        return "CompetencyItemAssociationDeletedRTMProducer{" +
                "associationDeletedRTMConsumable=" + associationDeletedRTMConsumable +
                '}';
    }
}
