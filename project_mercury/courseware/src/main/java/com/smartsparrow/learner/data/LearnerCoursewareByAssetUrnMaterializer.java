package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

class LearnerCoursewareByAssetUrnMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LearnerCoursewareByAssetUrnMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findElements(final String assetUrn, final UUID deploymentId, final UUID changeId) {
        final String SELECT = "SELECT " +
                "element_id, " +
                "element_type " +
                "FROM learner.courseware_by_asset_urn " +
                "WHERE asset_urn = ? " +
                "AND deployment_id = ? " +
                "AND change_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetUrn, deploymentId, changeId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public CoursewareElement fromRow(Row row) {
        return new CoursewareElement()
                .setElementId(row.getUUID("element_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")));
    }
}
