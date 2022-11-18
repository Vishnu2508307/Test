package com.smartsparrow.rtm.subscription.competency.association.created;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

public class CompetencyItemAssociationCreatedRTMProducer extends AbstractProducer<CompetencyItemAssociationCreatedRTMConsumable> {

    private CompetencyItemAssociationCreatedRTMConsumable associationCreatedRTMConsumable;

    @Inject
    public CompetencyItemAssociationCreatedRTMProducer() {
    }

    public CompetencyItemAssociationCreatedRTMProducer buildAssociationCreatedRTMConsumable(RTMClientContext rtmClientContext, UUID associationId, UUID documentId) {
        this.associationCreatedRTMConsumable = new CompetencyItemAssociationCreatedRTMConsumable(rtmClientContext, new CompetencyDocumentBroadcastMessage().setAssociationId(associationId).setDocumentId(documentId));
        return this;
    }

    @Override
    public CompetencyItemAssociationCreatedRTMConsumable getEventConsumable() {
        return associationCreatedRTMConsumable;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompetencyItemAssociationCreatedRTMProducer that = (CompetencyItemAssociationCreatedRTMProducer) o;
        return Objects.equals(associationCreatedRTMConsumable, that.associationCreatedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(associationCreatedRTMConsumable);
    }

    @Override
    public String toString() {
        return "CompetencyItemAssociationCreatedRTMProducer{" +
                "associationCreatedRTMConsumable=" + associationCreatedRTMConsumable +
                '}';
    }
}
