package com.smartsparrow.asset.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

class AssetByIconLibraryMaterializer implements TableMaterializer {

    private PreparedStatementCache statementCache;

    @Inject
    AssetByIconLibraryMaterializer(PreparedStatementCache statementCache) {
        this.statementCache = statementCache;
    }

    public Statement findByIconLibrary(String iconLibrary) {
        final String SELECT = "SELECT " +
                "icon_library, " +
                "asset_urn " +
                "FROM asset.icons_by_library " +
                "WHERE icon_library = ?";

        BoundStatement stmt = statementCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(iconLibrary);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Map a row to an asset by icon library object
     */
    public IconsByLibrary fromRow(Row row) {
        return new IconsByLibrary()
                .setIconLibrary(row.getString("icon_library"))
                .setAssetUrn(row.getString("asset_urn"));
    }
}
