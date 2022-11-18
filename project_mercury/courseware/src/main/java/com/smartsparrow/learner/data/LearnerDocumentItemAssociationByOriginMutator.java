package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerDocumentItemAssociationByOriginMutator extends SimpleTableMutator<ItemAssociation> {

    @Override
    public String getUpsertQuery(ItemAssociation mutation) {
        return "INSERT INTO learner.document_item_association_by_origin (" +
                "origin_item_id, " +
                "association_type, " +
                "association_id, " +
                "destination_item_id) VALUES (?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ItemAssociation mutation) {
        stmt.bind(mutation.getOriginItemId(),
                mutation.getAssociationType().name(),
                mutation.getId(),
                mutation.getDestinationItemId());
    }
}
