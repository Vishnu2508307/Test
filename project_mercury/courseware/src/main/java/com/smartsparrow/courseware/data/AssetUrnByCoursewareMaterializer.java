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

class AssetUrnByCoursewareMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AssetUrnByCoursewareMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findAssetUrnFor(final UUID elementId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + " element_id,"
                + " asset_urn,"
                + " element_type"
                + " FROM courseware.asset_urn_by_courseware"
                + " WHERE element_id = ?";
        // @formatter:on
        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(elementId);
        return stmt;
    }

    public CoursewareElementByAssetUrn fromRow(final Row row) {
        return new CoursewareElementByAssetUrn()
                .setAssetUrn(row.getString("asset_urn"))
                .setCoursewareElement(
                        CoursewareElement.from(
                                row.getUUID("element_id"),
                                Enums.of(CoursewareElementType.class, row.getString("element_type"))
                        )
                );
    }
}
