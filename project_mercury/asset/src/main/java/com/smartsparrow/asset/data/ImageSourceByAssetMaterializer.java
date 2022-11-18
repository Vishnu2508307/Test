package com.smartsparrow.asset.data;

import static com.smartsparrow.dse.api.ResultSets.getNullableDouble;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class ImageSourceByAssetMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ImageSourceByAssetMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    private static final String SELECT_BY_ID = "SELECT " +
            "asset_id, " +
            "name, " +
            "url, " +
            "width, " +
            "height " +
            "FROM asset.image_source_by_asset " +
            "WHERE asset_id = ?";

    @SuppressWarnings("Duplicates")
    public Statement findAllBy(UUID assetId) {
        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_BY_ID);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findByName(UUID assetId, ImageSourceName imageSourceName) {
        final String SELECT_BY_NAME = SELECT_BY_ID + " AND name = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_BY_NAME);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(assetId, imageSourceName.name());
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Map a row to an image source object. Fields <b>width</b> and <b>height</b> should never be <code>null</code>
     * in the row, however they will be set to <b>null</b> otherwise {@link Row#getDouble(String)} will return
     * <b>0</b> when the cell is <code>null</code>
     * @param row
     * @return
     */
    public ImageSource fromRow(Row row) {
        return new ImageSource()
                .setAssetId(row.getUUID("asset_id"))
                .setName(Enums.of(ImageSourceName.class, row.getString("name")))
                .setUrl(row.getString("url"))
                .setWidth(getNullableDouble(row, "width"))
                .setHeight(getNullableDouble(row, "height"));
    }
}
