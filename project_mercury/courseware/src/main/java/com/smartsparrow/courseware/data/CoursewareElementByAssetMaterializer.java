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

/**
 * Being replaced by asset urn tracking (part of immutable asset urn refactoring)
 */
@Deprecated
public class CoursewareElementByAssetMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    public CoursewareElementByAssetMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchAll(final UUID assetId) {
        // @formatter:off
        final String QUERY = "SELECT "
                           + " element_id,"
                           + " element_type"
                           + " FROM courseware.courseware_by_asset"
                           + " WHERE asset_id = ?";
        // @formatter:on
        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(assetId);
        return stmt;
    }

    public CoursewareElement fromRow(Row row) {
        return new CoursewareElement()
                .setElementId(row.getUUID("element_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")));
    }
}
