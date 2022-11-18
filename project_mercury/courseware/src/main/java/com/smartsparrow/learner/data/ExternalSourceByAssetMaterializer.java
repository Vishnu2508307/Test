package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.asset.data.ExternalSource;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class ExternalSourceByAssetMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ExternalSourceByAssetMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findBy(UUID assetId) {
        final String SELECT = "SELECT " +
                "asset_id, " +
                "url " +
                "FROM learner.external_source_by_asset " +
                "WHERE asset_id = ? ";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public ExternalSource fromRow(final Row row) {
        return new ExternalSource()
                .setAssetId(row.getUUID("asset_id"))
                .setUrl(row.getString("url"));
    }
}
