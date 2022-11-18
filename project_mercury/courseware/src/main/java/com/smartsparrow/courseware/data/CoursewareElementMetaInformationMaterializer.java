package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class CoursewareElementMetaInformationMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CoursewareElementMetaInformationMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findMetaInformation(final UUID elementId, final String key) {
        // @formatter:off
        final String SELECT = "SELECT" +
                " element_id" +
                ", key" +
                ", value" +
                " FROM courseware.courseware_element_meta_information" +
                " WHERE element_id = ?" +
                " AND key = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(elementId, key);
        return stmt;
    }

    public Statement findAllMetaInformation(final UUID elementId) {
        // @formatter:off
        final String SELECT = "SELECT" +
                " element_id" +
                ", key" +
                ", value" +
                " FROM courseware.courseware_element_meta_information" +
                " WHERE element_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(elementId);
        return stmt;
    }

    public CoursewareElementMetaInformation fromRow(Row row) {
        return new CoursewareElementMetaInformation()
                .setElementId(row.getUUID("element_id"))
                .setKey(row.getString("key"))
                .setValue(row.getString("value"));

    }
}
