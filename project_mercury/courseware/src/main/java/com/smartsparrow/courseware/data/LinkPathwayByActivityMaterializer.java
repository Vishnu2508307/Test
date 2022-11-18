package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class LinkPathwayByActivityMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LinkPathwayByActivityMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchAll(UUID activityId) {
        final String SELECT = "SELECT " +
                "pathway_id " +
                "FROM courseware.link_pathway_by_activity " +
                "WHERE activity_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(activityId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("pathway_id");
    }
}
