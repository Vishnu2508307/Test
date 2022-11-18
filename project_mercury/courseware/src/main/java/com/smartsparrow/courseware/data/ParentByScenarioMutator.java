package com.smartsparrow.courseware.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ParentByScenarioMutator extends SimpleTableMutator<ParentByScenario> {

    @Override
    public String getUpsertQuery(ParentByScenario mutation) {
        return "INSERT INTO courseware.parent_by_scenario (" +
                "scenario_id, " +
                "parent_id, " +
                "parent_type) " +
                "VALUES (?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ParentByScenario mutation) {
        stmt.bind(mutation.getScenarioId(), mutation.getParentId(), mutation.getParentType().name());
    }

    public Statement deleteRelationship(UUID scenarioId) {
        final String DELETE = "DELETE FROM courseware.parent_by_scenario " +
                "WHERE scenario_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(DELETE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(scenarioId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
