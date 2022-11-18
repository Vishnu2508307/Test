package com.smartsparrow.rtm.subscription.supplier.competency;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;

import reactor.core.publisher.Mono;

public class DocumentItemAssociationDeletedContentSupplier extends CompetencyDocumentBroadcastContentSupplier<Map<String, Object>> {


    @Override
    public Mono<Map<String, Object>> supplyFrom(CompetencyDocumentBroadcastMessage content) {
        final UUID associationId = content.getAssociationId();
        final UUID documentId = content.getDocumentId();

        return Mono.just(new HashMap<String, Object>() {
            {
                put("id", associationId);
                put("documentId", documentId);
            }
        });
    }
}
