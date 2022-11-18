package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class LearnerScenarioByParentMaterializer implements TableMaterializer {

    private static final String SELECT = "SELECT " +
            "parent_id, " +
            "deployment_id, " +
            "change_id, " +
            "lifecycle, " +
            "parent_type, " +
            "scenario_ids " +
            "FROM learner.scenario_by_parent " +
            "WHERE parent_id = ? " +
            "AND deployment_id = ?";

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerScenarioByParentMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findLatestDeployed(UUID parentId, UUID deploymentId) {
        final String LATEST = SELECT + " LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(LATEST);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(parentId, deploymentId);
        return stmt;
    }

    public Statement findByLifecycle(UUID parentId, UUID deploymentId, UUID changeId, ScenarioLifecycle lifecycle) {
        final String BY_LIFECYCLE = SELECT + " AND change_id = ? " +
                "AND lifecycle = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_LIFECYCLE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(parentId, deploymentId, changeId, lifecycle.name());
        return stmt;
    }

    public LearnerScenarioByParent fromRow(Row row) {
        return new LearnerScenarioByParent()
                .setParentId(row.getUUID("parent_id"))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"))
                .setLifecycle(Enums.of(ScenarioLifecycle.class, row.getString("lifecycle")))
                .setParentType(Enums.of(CoursewareElementType.class, row.getString("parent_type")))
                .setScenarioIds(row.getList("scenario_ids", UUID.class));
    }
}
