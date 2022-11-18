package com.smartsparrow.math.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.google.common.collect.Lists;
import com.smartsparrow.dse.api.SimpleTableMutator;

class UsageByAssetUrnMutator extends SimpleTableMutator<UsageByAssetUrn> {

    @Override
    public String getUpsertQuery(UsageByAssetUrn mutation) {
        return "INSERT INTO math.usage_by_asset_urn ("
                + "  asset_urn"
                + ", asset_id"
                + ", element_id"
                + ") VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, UsageByAssetUrn mutation) {
        stmt.setString(0, mutation.getAssetUrn());
        stmt.setUUID(1, mutation.getAssetId());
        stmt.setList(2, mutation.getElementId());
    }

    @Override
    public String getDeleteQuery(final UsageByAssetUrn mutation) {
        return "DELETE FROM math.usage_by_asset_urn" +
                " WHERE asset_urn = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final UsageByAssetUrn mutation) {
        stmt.bind(mutation.getAssetUrn());
    }

    public Statement addElementId(final UUID elementId, final String assetUrn) {
        final String ADD_ELEMENT = "UPDATE math.usage_by_asset_urn " +
                "SET element_id = element_id + ? " +
                "WHERE asset_urn = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(ADD_ELEMENT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(Lists.newArrayList(elementId.toString()), assetUrn);
        stmt.setIdempotent(false);
        return stmt;
    }

    public Statement removeElementId(final UUID elementId, final String assetUrn) {
        final String REMOVE_ELEMENT = "UPDATE math.usage_by_asset_urn " +
                "SET element_id = element_id - ? " +
                "WHERE asset_urn = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(REMOVE_ELEMENT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(Lists.newArrayList(elementId.toString()), assetUrn);
        stmt.setIdempotent(true);
        return stmt;
    }
}
