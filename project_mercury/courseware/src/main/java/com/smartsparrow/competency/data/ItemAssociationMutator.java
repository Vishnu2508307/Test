package com.smartsparrow.competency.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ItemAssociationMutator extends SimpleTableMutator<ItemAssociation> {

    @Override
    public String getUpsertQuery(ItemAssociation mutation) {
        return "INSERT INTO competency.item_association (" +
                "id, " +
                "document_id, " +
                "origin_item_id, " +
                "destination_item_id, " +
                "association_type, " +
                "created_at, " +
                "created_by) VALUES (?,?,?,?,?,?,?)";
    }

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

    @Override
    public String getDeleteQuery(ItemAssociation mutation) {
        return "DELETE FROM competency.item_association WHERE id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, ItemAssociation mutation) {
        stmt.bind(mutation.getId());
    }
}
