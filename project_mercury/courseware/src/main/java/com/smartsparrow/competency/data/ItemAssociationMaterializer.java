package com.smartsparrow.competency.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class ItemAssociationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ItemAssociationMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchById(UUID id) {
        String BY_ID = "SELECT id, " +
                "document_id, " +
                "origin_item_id, " +
                "destination_item_id, " +
                "association_type, " +
                "created_at," +
                "created_by FROM competency.item_association " +
                "WHERE id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ID);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public ItemAssociation fromRow(Row row) {
        return new ItemAssociation()
                .setId(row.getUUID("id"))
                .setDocumentId(row.getUUID("document_id"))
                .setOriginItemId(row.getUUID("origin_item_id"))
                .setDestinationItemId(row.getUUID("destination_item_id"))
                .setAssociationType(Enums.of(AssociationType.class, row.getString("association_type")))
                .setCreatedAt(row.getUUID("created_at"))
                .setCreatedById(row.getUUID("created_by"));
    }
}
