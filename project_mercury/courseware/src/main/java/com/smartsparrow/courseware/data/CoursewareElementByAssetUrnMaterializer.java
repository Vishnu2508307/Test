package com.smartsparrow.courseware.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

class CoursewareElementByAssetUrnMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CoursewareElementByAssetUrnMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findElementsFor(final String assetUrn) {
        // @formatter:off
        final String QUERY = "SELECT "
                + " asset_urn,"
                + " element_id,"
                + " element_type"
                + " FROM courseware.courseware_by_asset_urn"
                + " WHERE asset_urn = ?";
        // @formatter:on
        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(assetUrn);
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
