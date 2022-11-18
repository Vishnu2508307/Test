package com.smartsparrow.asset.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class AlfrescoAssetDataMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AlfrescoAssetDataMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchBy(UUID assetId) {
        final String SELECT = "SELECT " +
                "asset_id, " +
                "alfresco_id, " +
                "name, " +
                "version, " +
                "last_modified_date, " +
                "last_sync_date " +
                "FROM asset.alfresco_data_by_asset " +
                "WHERE asset_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Map a row to an Alfresco asset object
     */
    public AlfrescoAssetData fromRow(Row row) {
        return new AlfrescoAssetData()
                .setAssetId(row.getUUID("asset_id"))
                .setAlfrescoId(row.getUUID("alfresco_id"))
                .setName(row.getString("name"))
                .setVersion(row.getString("version"))
                .setLastModifiedDate(row.getLong("last_modified_date"))
                .setLastSyncDate(row.getLong("last_sync_date"));
    }

}
