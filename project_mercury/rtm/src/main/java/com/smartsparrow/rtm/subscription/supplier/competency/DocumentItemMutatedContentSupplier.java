package com.smartsparrow.rtm.subscription.supplier.competency;

import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.competency.service.DocumentItemService;

import reactor.core.publisher.Mono;

public class DocumentItemMutatedContentSupplier extends CompetencyDocumentBroadcastContentSupplier<DocumentItemPayload> {

    private final DocumentItemService documentItemService;

    @Inject
    public DocumentItemMutatedContentSupplier(DocumentItemService documentItemService) {
        this.documentItemService = documentItemService;
    }

    @Override
    public Mono<DocumentItemPayload> supplyFrom(CompetencyDocumentBroadcastMessage content) {

        UUID createdDocumentItemId = content.getDocumentItemId();

        return documentItemService.getDocumentItemPayload(createdDocumentItemId);
    }
}
