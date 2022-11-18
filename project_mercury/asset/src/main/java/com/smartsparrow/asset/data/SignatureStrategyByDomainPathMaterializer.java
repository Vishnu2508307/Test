package com.smartsparrow.asset.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class SignatureStrategyByDomainPathMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public SignatureStrategyByDomainPathMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findByHostPath(final String domain, final String path) {
        final String SELECT = "SELECT" +
                " host" +
                ", path" +
                ", signature_type" +
                ", id" +
                ", config" +
                " FROM asset.signature_strategy_by_host" +
                " WHERE host = ?" +
                " AND path = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(domain, path);
        stmt.setIdempotent(true);
        return stmt;
    }

    public AssetSignature fromRow(final Row row) {
        return new AssetSignature()
                .setId(row.getUUID("id"))
                .setHost(row.getString("host"))
                .setPath(row.getString("path"))
                .setAssetSignatureStrategyType(Enums.of(AssetSignatureStrategyType.class, row.getString("signature_type")))
                .setConfig(row.getString("config"));
    }
}
