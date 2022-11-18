package com.smartsparrow.workspace.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.service.ThemeState;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class ThemeVariantByStateMutator extends SimpleTableMutator<ThemeVariant> {

    @Override
    public String getUpsertQuery(ThemeVariant mutation) {
        // @formatter:off
        return "INSERT INTO workspace.theme_variant_by_state (" +
                "  theme_id" +
                ", variant_id" +
                ", variant_name" +
                ", config" +
                ", state" +
                ") VALUES (?, ?, ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ThemeVariant mutation) {
        stmt.setUUID(0, mutation.getThemeId());
        stmt.setUUID(1, mutation.getVariantId());
        stmt.setString(2, mutation.getVariantName());
        stmt.setString(3, mutation.getConfig());
        stmt.setString(4, Enums.asString(mutation.getState()));
    }

    /**
     * Delete all default theme variants associated by theme id
     * @param mutation the theme variant object
     * @return executable query
     */
    public Statement deleteByThemeId(ThemeVariant mutation) {
        String REMOVE = "DELETE FROM workspace.theme_variant_by_state " +
                "WHERE theme_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(REMOVE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(mutation.getThemeId());
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Delete specific theme variants by theme id, state and variant id
     * @param themeId the theme id
     * @param state the theme state
     * @param variantId the variant id
     * @return executable query
     */
    public Statement deleteByThemeIdAndVariant(UUID themeId, ThemeState state, UUID variantId) {
        String REMOVE = "DELETE FROM workspace.theme_variant_by_state " +
                "WHERE theme_id = ? AND state = ? AND variant_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(REMOVE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(themeId, Enums.asString(state), variantId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
