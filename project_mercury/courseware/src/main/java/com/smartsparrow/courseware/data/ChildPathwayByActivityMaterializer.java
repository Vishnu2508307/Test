package com.smartsparrow.courseware.data;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ChildPathwayByActivityMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ChildPathwayByActivityMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findByActivity(UUID activityId) {
        final String QUERY = "SELECT " +
                "pathway_ids " +
                "FROM courseware.child_pathway_by_activity " +
                "WHERE activity_id = ? ";

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(activityId);
        return stmt;
    }

    public List<UUID> fromRow(Row row) {
        return row.getList("pathway_ids", UUID.class);
    }

}
