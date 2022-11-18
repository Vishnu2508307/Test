package com.smartsparrow.competency.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ItemAssociationByDestinationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ItemAssociationByDestinationMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchOrigins(UUID itemId, AssociationType associationType) {
        String BY_ITEM_ID_AND_ASSOCIATION_TYPE = "SELECT destination_item_id, " +
                "association_type, " +
                "association_id, " +
                "origin_item_id FROM competency.item_association_by_destination " +
                "WHERE destination_item_id = ? and association_type = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ITEM_ID_AND_ASSOCIATION_TYPE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(itemId, associationType.name());
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchOrigins(UUID itemId) {
        String BY_ITEM_ID = "SELECT destination_item_id, " +
                "association_type, " +
                "association_id, " +
                "origin_item_id FROM competency.item_association_by_destination " +
                "WHERE destination_item_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ITEM_ID);
        stmt.setIdempotent(true);
        stmt.bind(itemId);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("association_id");
    }
}
