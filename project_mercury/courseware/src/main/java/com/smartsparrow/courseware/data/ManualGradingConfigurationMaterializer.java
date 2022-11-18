package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ManualGradingConfigurationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ManualGradingConfigurationMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement find(UUID componentId) {
        final String SELECT = "SELECT" +
                " component_id" +
                ", max_score" +
                " FROM courseware.manual_grading_configuration_by_component" +
                " WHERE component_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(componentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public ManualGradingConfiguration fromRow(Row row) {
        return new ManualGradingConfiguration()
                .setComponentId(row.getUUID("component_id"))
                .setMaxScore(row.getDouble("max_score"));
    }
}
