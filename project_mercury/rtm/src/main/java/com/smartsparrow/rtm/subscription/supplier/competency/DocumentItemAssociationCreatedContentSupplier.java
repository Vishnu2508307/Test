package com.smartsparrow.rtm.subscription.supplier.competency;

import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.competency.service.ItemAssociationService;

import reactor.core.publisher.Mono;

public class DocumentItemAssociationCreatedContentSupplier extends CompetencyDocumentBroadcastContentSupplier<ItemAssociation> {

    private final ItemAssociationService itemAssociationService;

    @Inject
    public DocumentItemAssociationCreatedContentSupplier(ItemAssociationService itemAssociationService) {
        this.itemAssociationService = itemAssociationService;
    }

    @Override
    public Mono<ItemAssociation> supplyFrom(CompetencyDocumentBroadcastMessage content) {
        final UUID associationId = content.getAssociationId();

        return itemAssociationService.findById(associationId);
    }
}
