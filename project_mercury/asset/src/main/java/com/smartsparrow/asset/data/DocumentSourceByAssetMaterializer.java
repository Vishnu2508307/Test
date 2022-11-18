package com.smartsparrow.asset.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class DocumentSourceByAssetMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DocumentSourceByAssetMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findBy(UUID assetId) {
        final String SELECT = "SELECT " +
                "asset_id, " +
                "url " +
                "FROM asset.document_source_by_asset " +
                "WHERE asset_id = ? ";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Map a row to a document source object.
     */
    public DocumentSource fromRow(Row row) {
        return new DocumentSource()
                .setAssetId(row.getUUID("asset_id"))
                .setUrl(row.getString("url"));
    }
}
