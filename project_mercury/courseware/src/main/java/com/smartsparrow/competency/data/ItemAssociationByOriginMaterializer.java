package com.smartsparrow.competency.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ItemAssociationByOriginMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ItemAssociationByOriginMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchDestinations(UUID itemId, AssociationType associationType) {
        String BY_ITEM_ID_AND_ASSOCIATION_TYPE = "SELECT origin_item_id, " +
                "association_type, " +
                "association_id, " +
                "destination_item_id FROM competency.item_association_by_origin " +
                "WHERE origin_item_id = ? and association_type = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ITEM_ID_AND_ASSOCIATION_TYPE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(itemId, associationType.name());
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchDestinations(UUID itemId) {
        String BY_ITEM_ID = "SELECT origin_item_id, " +
                "association_type, " +
                "association_id, " +
                "destination_item_id FROM competency.item_association_by_origin " +
                "WHERE origin_item_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ITEM_ID);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(itemId);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("association_id");
    }
}
