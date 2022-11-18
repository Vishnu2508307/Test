package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ScenarioMutator extends SimpleTableMutator<Scenario> {

    @Override
    public String getUpsertQuery(Scenario mutation) {
        // @formatter:off
        return "INSERT INTO courseware.scenario ("
                + "  id"
                + ", condition"
                + ", actions"
                + ", lifecycle"
                + ", name"
                + ", description"
                + ", correctness"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Scenario mutation) {
        stmt.bind(mutation.getId(),
                mutation.getCondition(),
                mutation.getActions(),
                mutation.getLifecycle().name(),
                mutation.getName(),
                mutation.getDescription(),
                (mutation.getCorrectness() != null ? mutation.getCorrectness().name() : null));
    }

    Statement updateScenario(Scenario scenario) {
        // @formatter:off
        final String UPDATE_SCENARIO_QUERY = "UPDATE courseware.scenario set"
                + " condition = ?,"
                + " actions = ?,"
                + " name = ?,"
                + " description = ?,"
                + " correctness = ?"
                + " where id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(UPDATE_SCENARIO_QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(scenario.getCondition(),
                scenario.getActions(),
                scenario.getName(),
                scenario.getDescription(),
                (scenario.getCorrectness() != null ? scenario.getCorrectness().name() : null),
                scenario.getId());
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement updateScenarioCondition(Scenario scenario) {
        // @formatter:off
        final String UPDATE_CONDITION_QUERY = "UPDATE courseware.scenario set"
                + " condition = ?"
                + " where id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(UPDATE_CONDITION_QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(scenario.getCondition(), scenario.getId());
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement updateScenarioAction(Scenario scenario) {
        // @formatter:off
        final String UPDATE_ACTION_QUERY = "UPDATE courseware.scenario set"
                + " actions = ?"
                + " where id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(UPDATE_ACTION_QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(scenario.getActions(), scenario.getId());
        stmt.setIdempotent(true);
        return stmt;
    }

    @Override
    public String getDeleteQuery(Scenario mutation) {
        return "DELETE FROM cohort.scenario" +
                "WHERE scenario_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, Scenario mutation) {
        stmt.bind(mutation.getId());
    }

}
