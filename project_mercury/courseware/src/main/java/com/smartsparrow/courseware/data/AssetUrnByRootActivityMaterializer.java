package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

class AssetUrnByRootActivityMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    AssetUrnByRootActivityMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAssetUrnFor(final UUID rootActivityId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + " root_activity_id"
                + ", asset_urn"
                + ", element_id"
                + ", element_type"
                + " FROM courseware.asset_urn_by_root_activity"
                + " WHERE root_activity_id = ?";
        // @formatter:on
        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(rootActivityId);
        return stmt;
    }

    public AssetUrnByRootActivity fromRow(final Row row) {
        return new AssetUrnByRootActivity()
                .setRootActivityId(row.getUUID("root_activity_id"))
                .setAssetUrn(row.getString("asset_urn"))
                .setCoursewareElement(
                        CoursewareElement.from(
                                row.getUUID("element_id"),
                                Enums.of(CoursewareElementType.class, row.getString("element_type"))
                        )
                );
    }
}
