package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.dse.api.SimpleTableMutator;

/**
 * Being replaced by asset urn tracking (part of immutable asset urn refactoring)
 */
@Deprecated
public class LearnerCoursewareByAssetMutator extends SimpleTableMutator<CoursewareElement> {

    public Statement upsert(CoursewareElement element, Deployment deployment, UUID assetId) {
        // @formatter:off
        String QUERY = "INSERT INTO learner.courseware_by_asset ("
                + "  asset_id"
                + ", deployment_id"
                + ", change_id"
                + ", element_id"
                + ", element_type"
                + ") VALUES (?,?,?,?,?)";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(
                assetId,
                deployment.getId(),
                deployment.getChangeId(),
                element.getElementId(),
                element.getElementType().name()
        );
        stmt.setIdempotent(true);
        return stmt;
    }
}
