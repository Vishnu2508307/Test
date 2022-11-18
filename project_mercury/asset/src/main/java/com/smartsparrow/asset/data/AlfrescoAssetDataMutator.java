package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class AlfrescoAssetDataMutator extends SimpleTableMutator<AlfrescoAssetData> {

    @Override
    public String getUpsertQuery(AlfrescoAssetData mutation) {
        return "INSERT INTO asset.alfresco_data_by_asset (" +
                "asset_id, " +
                "alfresco_id, " +
                "name, " +
                "version, " +
                "last_modified_date, " +
                "last_sync_date) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AlfrescoAssetData mutation) {
        stmt.bind(
                mutation.getAssetId(),
                mutation.getAlfrescoId(),
                mutation.getName(),
                mutation.getVersion(),
                mutation.getLastModifiedDate(),
                mutation.getLastSyncDate()
        );
    }

}
