package com.smartsparrow.courseware.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

/**
 * Being replaced by asset urn tracking (part of immutable asset urn refactoring)
 */
@Deprecated
class AssetByCoursewareRootActivityMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AssetByCoursewareRootActivityMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findById(final UUID rootElementId) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  root_element_id" +
                ", asset_provider" +
                ", element_id" +
                ", element_type" +
                ", asset_id" +
                " FROM courseware.asset_by_root_activity" +
                " WHERE root_element_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(rootElementId);
        return stmt;
    }

    public Statement findByIdAndProvider(final UUID rootElementId, String assetProvider) {

        // @formatter:off
        final String QUERY = "SELECT" +
                "  root_element_id" +
                ", asset_provider" +
                ", element_id" +
                ", element_type" +
                ", asset_id" +
                " FROM courseware.asset_by_root_activity" +
                " WHERE root_element_id = ? AND asset_provider = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(rootElementId, assetProvider);
        return stmt;
    }

    public AssetByRootActivity fromRow(Row row) {
        return new AssetByRootActivity()
                .setRootElementId(row.getUUID("root_element_id"))
                .setAssetProvider(Enums.of(AssetProvider.class, row.getString("asset_provider")))
                .setElementId(row.getUUID("element_id"))
                .setElementType(Enums.of(CoursewareElementType.class, row.getString("element_type")))
                .setAssetId(row.getUUID("asset_id"));
    }
}
