package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerDocumentItemAssociationMutator extends SimpleTableMutator<ItemAssociation> {

    @Override
    public String getUpsertQuery(ItemAssociation mutation) {
        return "INSERT INTO learner.document_item_association (" +
                "id, " +
                "document_id, " +
                "origin_item_id, " +
                "destination_item_id, " +
                "association_type, " +
                "created_at, " +
                "created_by) VALUES (?,?,?,?,?,?,?)";    }

    @Override
    public void bindUpsert(BoundStatement stmt, ItemAssociation mutation) {
        stmt.bind(mutation.getId(),
                mutation.getDocumentId(),
                mutation.getOriginItemId(),
                mutation.getDestinationItemId(),
                mutation.getAssociationType().name(),
                mutation.getCreatedAt(),
                mutation.getCreatedById());
    }
}
