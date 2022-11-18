package com.smartsparrow.cohort.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CohortByProductMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(String productId, UUID cohortId) {
        String UPSERT = "INSERT INTO cohort.cohort_by_product (" +
                "product_id, " +
                "cohort_id) " +
                "VALUES (?,?)";

        BoundStatement stmt = stmtCache.asBoundStatement(UPSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(productId, cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement remove(String productId, UUID cohortId) {
        String REMOVE = "DELETE FROM cohort.cohort_by_product " +
                "WHERE product_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(REMOVE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(productId, cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
