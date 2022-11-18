package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class LearnerDocumentItemAssociationByOriginMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerDocumentItemAssociationByOriginMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchDestinations(UUID documentItemId, AssociationType associationType) {
        String SELECT = "SELECT origin_item_id, " +
                "association_type, " +
                "association_id, " +
                "destination_item_id FROM learner.document_item_association_by_origin " +
                "WHERE origin_item_id = ? and association_type = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(documentItemId, associationType.name());
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchDestinations(UUID documentItemId) {
        String BY_ITEM_ID = "SELECT origin_item_id, " +
                "association_type, " +
                "association_id, " +
                "destination_item_id FROM learner.document_item_association_by_origin " +
                "WHERE origin_item_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ITEM_ID);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(documentItemId);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("association_id");
    }
}
