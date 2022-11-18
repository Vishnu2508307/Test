package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class CoursewareConfigurationFieldMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CoursewareConfigurationFieldMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchField(UUID elementId, String fieldName) {
        final String SELECT = "SELECT" +
                " field_name" +
                ", field_value" +
                " FROM courseware.configuration_field_by_element" +
                " WHERE element_id = ?" +
                " AND field_name = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(elementId, fieldName);
        return stmt;
    }

    public Statement fetchAll(UUID elementId) {
        final String SELECT = "SELECT" +
                " field_name" +
                ", field_value" +
                " FROM courseware.configuration_field_by_element" +
                " WHERE element_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(elementId);
        return stmt;
    }

    public ConfigurationField fromRow(Row row) {
        return new ConfigurationField()
                .setFieldName(row.getString("field_name"))
                .setFieldValue(row.getString("field_value"));
    }
}
