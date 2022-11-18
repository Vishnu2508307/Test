package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

/**
 * Being replaced by asset urn tracking (part of immutable asset urn refactoring)
 */
@Deprecated
public class AssetByCoursewareElementMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    public AssetByCoursewareElementMaterializer(PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchAll(final UUID elementId) {
        // @formatter:off
        final String QUERY = "SELECT "
                           + " asset_id"
                           + " FROM courseware.asset_by_courseware"
                           + " WHERE element_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(elementId);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("asset_id");
    }
}
