package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class LearnerConfigurationFieldMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerConfigurationFieldMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchField(UUID deploymentId, UUID changeId, UUID elementId, String fieldName) {
        final String SELECT = "SELECT" +
                " field_name" +
                ", field_value" +
                " FROM learner.configuration_field_by_element" +
                " WHERE deployment_id = ?" +
                " AND change_id = ?" +
                " AND element_id = ?" +
                " AND field_name = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(deploymentId, changeId, elementId, fieldName);
        return stmt;
    }

    public ConfigurationField fromRow(Row row) {
        return new ConfigurationField()
                .setFieldName(row.getString("field_name"))
                .setFieldValue(row.getString("field_value"));
    }
}
