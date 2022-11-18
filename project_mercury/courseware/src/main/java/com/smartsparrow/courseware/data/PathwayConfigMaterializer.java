package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class PathwayConfigMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public PathwayConfigMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchLatestConfig(final UUID pathwayId) {
        final String SELECT = "SELECT" +
                " pathway_id" +
                ", id" +
                ", config" +
                " FROM courseware.pathway_config" +
                " WHERE pathway_id = ?" +
                " LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(pathwayId);
        return stmt;
    }

    public PathwayConfig fromRow(Row row) {
        return new PathwayConfig()
                .setId(row.getUUID("id"))
                .setPathwayId(row.getUUID("pathway_id"))
                .setConfig(row.getString("config"));
    }
}
