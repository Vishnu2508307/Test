package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class LearnerScenarioMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerScenarioMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    private static final String SELECT = "SELECT " +
            "id, " +
            "deployment_id, " +
            "change_id, " +
            "actions, " +
            "condition, " +
            "correctness, " +
            "lifecycle, " +
            "name," +
            "description " +
            "FROM learner.scenario ";

    public Statement findById(UUID id, UUID deploymentId, UUID changeId) {

        final String SELECT_BY_ID = SELECT +
                "WHERE id = ? " +
                "AND deployment_id = ? " +
                "AND changeId = ?";
        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_BY_ID);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(id, deploymentId, changeId);
        return stmt;
    }

    public Statement findAllById(UUID deploymentId, UUID changeId) {

        final String SELECT_BY_ID = SELECT +
                "WHERE deployment_id = ? " +
                "AND change_id = ? ALLOW FILTERING";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_BY_ID);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(deploymentId, changeId);
        return stmt;
    }

    public Statement findLatestDeployed(UUID id, UUID deploymentId) {

        final String SELECT_LATEST = SELECT +
                "WHERE id = ? " +
                "AND deployment_id = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_LATEST);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id, deploymentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LearnerScenario fromRow(Row row) {
        return new LearnerScenario()
                .setId(row.getUUID("id"))
                .setDeploymentId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"))
                .setActions(row.getString("actions"))
                .setCondition(row.getString("condition"))
                .setCorrectness(row.getString("correctness") != null ? Enums.of(ScenarioCorrectness.class, row.getString("correctness")) : null)
                .setLifecycle(Enums.of(ScenarioLifecycle.class, row.getString("lifecycle")))
                .setName(row.getString("name"))
                .setDescription(row.getString("description"));

    }
}
