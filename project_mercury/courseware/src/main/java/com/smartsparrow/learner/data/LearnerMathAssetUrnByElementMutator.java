package com.smartsparrow.learner.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class LearnerMathAssetUrnByElementMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(final UUID elementId, final UUID changeId, final String assetUrn, final CoursewareElementType elementType) {
        // @formatter:off
        String QUERY = "INSERT INTO learner.math_asset_urn_by_element ("
                + "  element_id"
                + ", change_id"
                + ", asset_urn"
                + ", element_type"
                + ") VALUES ( ?, ?, ?, ? )";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(elementId, changeId, assetUrn, Enums.asString(elementType));
        stmt.setIdempotent(true);
        return stmt;
    }
}
