package com.smartsparrow.courseware.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.google.common.collect.Lists;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ScenarioByParentMutator extends SimpleTableMutator<ScenarioByParent> {

    @Override
    public String getUpsertQuery(ScenarioByParent mutation) {
        return "INSERT INTO courseware.scenario_by_parent (" +
                "parent_id, " +
                "lifecycle, " +
                "scenario_ids, " +
                "parent_type) " +
                "VALUES (?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ScenarioByParent mutation) {
        stmt.bind(mutation.getParentId(), mutation.getLifecycle().name(), mutation.getScenarioIds(), mutation.getParentType().name());
    }

    Statement addScenario(UUID parentId, ScenarioLifecycle lifecycle, UUID scenarioId, CoursewareElementType parentType) {
        final String ADD_SCENARIO = "UPDATE courseware.scenario_by_parent " +
                "SET scenario_ids = scenario_ids + ?" +
                ", parent_type = ? " +
                "WHERE parent_id = ? " +
                "AND lifecycle = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(ADD_SCENARIO);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(Lists.newArrayList(scenarioId), parentType.name(), parentId, lifecycle.name());
        stmt.setIdempotent(false);
        return stmt;
    }

    Statement removeScenario(UUID parentId, ScenarioLifecycle lifecycle, UUID scenarioId, CoursewareElementType parentType) {
        final String REMOVE_SCENARIO = "UPDATE courseware.scenario_by_parent " +
                "SET scenario_ids = scenario_ids - ?" +
                ", parent_type = ? " +
                "WHERE parent_id = ? " +
                "AND lifecycle = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(REMOVE_SCENARIO);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(Lists.newArrayList(scenarioId), parentType.name(), parentId, lifecycle.name());
        stmt.setIdempotent(false);
        return stmt;
    }
}
