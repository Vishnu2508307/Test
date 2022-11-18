package com.smartsparrow.workspace.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ThemeVariantMutator extends SimpleTableMutator<ThemeVariant> {

    @Override
    public String getUpsertQuery(ThemeVariant mutation) {
        // @formatter:off
        return "INSERT INTO workspace.theme_variant (" +
                "  theme_id" +
                ", variant_id" +
                ", variant_name" +
                ", config" +
                ") VALUES (?, ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ThemeVariant mutation) {
        stmt.setUUID(0, mutation.getThemeId());
        stmt.setUUID(1, mutation.getVariantId());
        stmt.setString(2, mutation.getVariantName());
        stmt.setString(3, mutation.getConfig());
    }

    public Statement deleteByThemeId(ThemeVariant mutation) {
        String REMOVE = "DELETE FROM workspace.theme_variant " +
                         "WHERE theme_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(REMOVE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(mutation.getThemeId());
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement deleteByThemeIdAndVariant(UUID themeId, UUID variantId) {
        String REMOVE = "DELETE FROM workspace.theme_variant " +
                "WHERE theme_id = ? AND variant_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(REMOVE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(themeId, variantId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
