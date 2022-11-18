package com.smartsparrow.rtm.subscription.supplier.competency;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentAction;
import com.smartsparrow.rtm.subscription.supplier.BroadcastContentProvider;

public class CompetencyDocumentBroadcastProvider implements BroadcastContentProvider<CompetencyDocumentAction, CompetencyDocumentBroadcastContentSupplier> {

    private final Map<CompetencyDocumentAction, CompetencyDocumentBroadcastContentSupplier> suppliers;

    @Inject
    public CompetencyDocumentBroadcastProvider(DocumentItemMutatedContentSupplier documentItemMutatedContentSupplier,
                                               DocumentItemDeletedContentSupplier documentItemDeletedContentSupplier,
                                               DocumentItemAssociationCreatedContentSupplier documentItemAssociationCreatedContentSupplier,
                                               DocumentItemAssociationDeletedContentSupplier documentItemAssociationDeletedContentSupplier) {
        this.suppliers = new HashMap<CompetencyDocumentAction, CompetencyDocumentBroadcastContentSupplier>() {
            {
                put(CompetencyDocumentAction.DOCUMENT_ITEM_CREATED, documentItemMutatedContentSupplier);
                put(CompetencyDocumentAction.DOCUMENT_ITEM_UPDATED, documentItemMutatedContentSupplier);
                put(CompetencyDocumentAction.DOCUMENT_ITEM_DELETED, documentItemDeletedContentSupplier);
                put(CompetencyDocumentAction.ASSOCIATION_CREATED, documentItemAssociationCreatedContentSupplier);
                put(CompetencyDocumentAction.ASSOCIATION_DELETED, documentItemAssociationDeletedContentSupplier);
            }
        };
    }

    @Override
    public CompetencyDocumentBroadcastContentSupplier get(CompetencyDocumentAction action) {
        if (!suppliers.containsKey(action)) {
            throw new UnsupportedOperationException(String.format("no supplier provided for %s", action));
        }

        return suppliers.get(action);
    }
}
