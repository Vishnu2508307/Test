package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class AssetSummaryMutator extends SimpleTableMutator<AssetSummary> {

    @Override
    public String getUpsertQuery(AssetSummary mutation) {
        return "INSERT INTO asset.summary (" +
                "id, " +
                "urn, " +
                "provider, " +
                "owner_id, " +
                "subscription_id, " +
                "media_type, " +
                "hash, " +
                "visibility) " +
                "VALUES (?,?,?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AssetSummary mutation) {
        stmt.bind(
                mutation.getId(),
                mutation.getUrn(),
                mutation.getProvider().name(),
                mutation.getOwnerId(),
                mutation.getSubscriptionId(),
                mutation.getMediaType().name(),
                mutation.getHash(),
                mutation.getVisibility().name()
        );
    }
}
