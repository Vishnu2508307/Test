package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class LearnerRedirectByIdMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    LearnerRedirectByIdMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetch(final UUID id) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", version"
                + ", redirect_type"
                + ", redirect_key"
                + ", destination_path"
                + " FROM learner_redirect.by_id"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(id);
        return stmt;
    }
}
