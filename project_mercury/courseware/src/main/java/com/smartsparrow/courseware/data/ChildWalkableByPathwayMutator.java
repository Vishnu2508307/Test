package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ChildWalkableByPathwayMutator extends SimpleTableMutator<WalkablePathwayChildren> {

    @Override
    public String getUpsertQuery(WalkablePathwayChildren mutation) {
        return "INSERT INTO courseware.child_walkable_by_pathway (" +
                "pathway_id, " +
                "walkable_ids, " +
                "walkable_types) " +
                "VALUES (?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, WalkablePathwayChildren mutation) {
        stmt.bind(mutation.getPathwayId(), mutation.getWalkableIds(), mutation.getWalkableTypes());
    }

    public Statement addWalkable(WalkablePathwayChildren child) {
        final String ADD_WALKABLE = "UPDATE courseware.child_walkable_by_pathway " +
                "SET walkable_ids = walkable_ids + ?, " +
                "walkable_types = walkable_types + ? " +
                "WHERE pathway_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(ADD_WALKABLE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(child.getWalkableIds(), child.getWalkableTypes(), child.getPathwayId());
        stmt.setIdempotent(false);
        return stmt;
    }

    public Statement removeWalkable(WalkablePathwayChildren child) {
        final String REMOVE_WALKABLE = "UPDATE courseware.child_walkable_by_pathway " +
                "SET walkable_ids = walkable_ids - ?, " +
                "walkable_types = walkable_types - ? " +
                "WHERE pathway_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(REMOVE_WALKABLE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(child.getWalkableIds(), child.getWalkableTypes().keySet(), child.getPathwayId());
        stmt.setIdempotent(true);
        return stmt;
    }
}
