package com.smartsparrow.asset.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class SignatureStrategyByDomainPathMutator extends SimpleTableMutator<AssetSignature> {

    @Override
    public String getUpsertQuery(AssetSignature mutation) {
        return "INSERT INTO asset.signature_strategy_by_host (" +
                " host" +
                ", path" +
                ", signature_type" +
                ", id" +
                ", config" +
                ") VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AssetSignature mutation) {
        stmt.bind(
                mutation.getHost(),
                mutation.getPath(),
                Enums.asString(mutation.getAssetSignatureStrategyType()),
                mutation.getId(),
                mutation.getConfig()
        );
    }

    @Override
    public String getDeleteQuery(AssetSignature mutation) {
        return "DELETE FROM asset.signature_strategy_by_host" +
                " WHERE host = ?" +
                " AND path = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, AssetSignature mutation) {
        stmt.bind(mutation.getHost(), mutation.getPath());
    }
}
