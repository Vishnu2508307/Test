package com.smartsparrow.rtm.subscription.supplier.competency;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;

import reactor.core.publisher.Mono;

public class DocumentItemDeletedContentSupplier extends CompetencyDocumentBroadcastContentSupplier<Map<String, Object>> {

    @Override
    public Mono<Map<String, Object>> supplyFrom(CompetencyDocumentBroadcastMessage content) {

        final UUID deletedItemId = content.getDocumentItemId();
        final UUID documentId = content.getDocumentId();

        return Mono.just(new HashMap<String, Object>(){
            {
                put("id", deletedItemId);
                put("documentId", documentId);
            }
        });
    }
}
