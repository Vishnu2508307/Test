package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class ComponentConfigMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    ComponentConfigMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchLatestConfig(final UUID componentId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", component_id"
                + ", config"
                + " FROM courseware.component_config"
                + " WHERE component_id = ?"
                + " LIMIT 1";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(componentId);
        return stmt;
    }
}
