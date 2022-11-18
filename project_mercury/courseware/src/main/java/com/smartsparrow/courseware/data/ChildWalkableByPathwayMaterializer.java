package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ChildWalkableByPathwayMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ChildWalkableByPathwayMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchBy(UUID pathwayId) {
        final String SELECT = "SELECT " +
                "pathway_id, " +
                "walkable_ids, " +
                "walkable_types " +
                "FROM courseware.child_walkable_by_pathway " +
                "WHERE pathway_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(pathwayId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public WalkablePathwayChildren fromRow(Row row) {
        return new WalkablePathwayChildren()
                .setPathwayId(row.getUUID("pathway_id"))
                .setWalkableIds(row.getList("walkable_ids", UUID.class))
                .setWalkableTypes(row.getMap("walkable_types", UUID.class, String.class));
    }
}
