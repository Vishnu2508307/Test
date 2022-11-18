package com.smartsparrow.competency.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ItemAssociationByDestinationMutator extends SimpleTableMutator<ItemAssociation> {

    @Override
    public String getUpsertQuery(ItemAssociation mutation) {
        return "INSERT INTO competency.item_association_by_destination (" +
                "destination_item_id, " +
                "association_type, " +
                "association_id, " +
                "origin_item_id) VALUES (?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ItemAssociation mutation) {
        stmt.bind(mutation.getDestinationItemId(),
                mutation.getAssociationType().name(),
                mutation.getId(),
                mutation.getOriginItemId());
    }

    @Override
    public String getDeleteQuery(ItemAssociation mutation) {
        return "DELETE FROM competency.item_association_by_destination " +
                "WHERE destination_item_id = ? " +
                "AND association_type = ? " +
                "AND association_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, ItemAssociation mutation) {
        stmt.bind(mutation.getDestinationItemId(), mutation.getAssociationType().name(), mutation.getId());
    }
}
