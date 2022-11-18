package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioCorrectness;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class EvaluationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public EvaluationMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement find(UUID id) {
        // formatter:off
        final String SELECT = "SELECT" +
                "  id" +
                ", element_id" +
                ", element_type" +
                ", deployment_id" +
                ", change_id" +
                ", student_id" +
                ", attempt_id" +
                ", element_scope_data_map" +
                ", student_scope_urn" +
                ", parent_id" +
                ", parent_type" +
                ", parent_attempt_id" +
                ", completed" +
                ", triggered_scenario_ids" +
                ", scenario_correctness" +
                ", cohort_id" +
                ", triggered_actions" +
                "  FROM learner.evaluation" +
                " WHERE id = ?";
        // formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findHistoricScope(UUID evaluationId) {
        // formatter:off
        final String SELECT = "SELECT" +
                " element_scope_data_map" +
                ", student_scope_urn" +
                "  FROM learner.evaluation" +
                " WHERE id = ?";
        // formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(evaluationId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public EvaluationScopeData fromRowSummary(Row row) {
        return new EvaluationScopeData()
                .setStudentScopeURN(row.getUUID("student_scope_urn"))
                .setStudentScopeDataMap(row.getMap("element_scope_data_map", UUID.class, String.class));
    }

    public Evaluation fromRow(Row row) {

        String scenarioCorrectness = row.getString("scenario_correctness");

        final Deployment deployment = new Deployment()
                .setId(row.getUUID("deployment_id"))
                .setChangeId(row.getUUID("change_id"))
                .setCohortId(row.getUUID("cohort_id"));

        return new Evaluation()
                .setId(row.getUUID("id"))
                .setElementId(row.getUUID("element_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")))
                .setDeployment(deployment)
                .setStudentId(row.getUUID("student_id"))
                .setAttemptId(row.getUUID("attempt_id"))
                .setElementScopeDataMap(row.getMap("element_scope_data_map", UUID.class, String.class))
                .setStudentScopeURN(row.getUUID("student_scope_urn"))
                .setParentId(row.getUUID("parent_id"))
                .setParentType(Enums.of(CoursewareElementType.class, row.getString("parent_type")))
                .setCompleted(row.getBool("completed"))
                .setParentAttemptId(row.getUUID("parent_attempt_id"))
                .setTriggeredScenarioIds(row.getList("triggered_scenario_ids", UUID.class))
                .setScenarioCorrectness(scenarioCorrectness != null ? Enums.of(ScenarioCorrectness.class, scenarioCorrectness) : null)
                .setTriggeredActions(row.getString("triggered_actions"));
    }
}
