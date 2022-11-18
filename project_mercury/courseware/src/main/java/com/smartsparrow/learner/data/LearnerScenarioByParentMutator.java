package com.smartsparrow.learner.data;

import java.util.List;
import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class LearnerScenarioByParentMutator extends SimpleTableMutator<LearnerScenarioByParent> {

    public Statement addScenario(UUID scenarioId, UUID deploymentId, UUID changeId, UUID parentId,
                                 ScenarioLifecycle lifecycle, CoursewareElementType type) {
        final String ADD_SCENARIO = "UPDATE learner.scenario_by_parent " +
                "SET scenario_ids = scenario_ids + ?" +
                ", parent_type = ? " +
                "WHERE parent_id = ? " +
                "AND deployment_id = ? " +
                "AND change_id = ? " +
                "AND lifecycle = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(ADD_SCENARIO);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(
                Lists.newArrayList(scenarioId),
                type.name(),
                parentId,
                deploymentId,
                changeId,
                lifecycle.name()
        );
        stmt.setIdempotent(false);
        return stmt;
    }

    public Statement insertScenarioByParent(List<UUID> scenarioIds, UUID deploymentId, UUID changeId, UUID parentId,
                                            ScenarioLifecycle lifecycle, CoursewareElementType type) {
        final String INSERT = "INSERT INTO learner.scenario_by_parent (" +
                " parent_id" +
                ", deployment_id" +
                ", change_id" +
                ", lifecycle" +
                ", parent_type" +
                ", scenario_ids" +
                ") VALUES (?, ?, ?, ?, ?, ?)";

        BoundStatement stmt = stmtCache.asBoundStatement(INSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(parentId, deploymentId, changeId, Enums.asString(lifecycle), Enums.asString(type), scenarioIds);
        return stmt;
    }
}
