package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

import javax.inject.Inject;

class InteractiveConfigLatestMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    InteractiveConfigLatestMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchLatestConfig(final InteractiveConfigId  interactiveConfigId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", interactive_id"
                + ", config"
                + " FROM courseware.interactive_config_latest"
                + " WHERE interactive_id = ?"
                + " AND id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(interactiveConfigId.getInteractiveId(), interactiveConfigId.getConfigId());
        return stmt;
    }
}
