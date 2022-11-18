package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class LearnerCoursewareByAssetUrnMutator extends SimpleTableMutator<CoursewareElement> {

    public Statement upsert(final CoursewareElement element, final Deployment deployment, final String assetUrn) {
        // @formatter:off
        String QUERY = "INSERT INTO learner.courseware_by_asset_urn ("
                + "  asset_urn"
                + ", deployment_id"
                + ", change_id"
                + ", element_id"
                + ", element_type"
                + ") VALUES (?, ?, ?, ?, ?)";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(
                assetUrn,
                deployment.getId(),
                deployment.getChangeId(),
                element.getElementId(),
                element.getElementType().name()
        );
        stmt.setIdempotent(true);
        return stmt;
    }
}
