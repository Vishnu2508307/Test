package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class ParentByLearnerScenarioMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ParentByLearnerScenarioMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchLatestDeployed(UUID scenarioId, UUID deploymentId) {
        final String SELECT = "SELECT " +
                "scenario_id, " +
                "deployment_id, " +
                "change_id, " +
                "parent_id, " +
                "parent_type FROM learner.parent_by_scenario " +
                "WHERE scenario_id = ? " +
                "AND deployment_id = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(scenarioId, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public ParentByLearnerScenario fromRow(Row row) {
        return new ParentByLearnerScenario()
                .setScenarioId(row.getUUID("scenario_id"))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"))
                .setParentId(row.getUUID("parent_id"))
                .setParentType(Enums.of(CoursewareElementType.class, row.getString("parent_type")));
    }
}
