package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.courseware.service.ThemeState;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

class ThemeVariantByStateMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ThemeVariantByStateMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findVariantByStateVariantId(final UUID themeId, final ThemeState state, final UUID variantId) {

        // @formatter:off
        final String QUERY = "SELECT " +
                "  theme_id" +
                ", variant_id" +
                ", variant_name" +
                ", config" +
                ", state" +
                " FROM workspace.theme_variant_by_state " +
                " WHERE theme_id = ? AND state = ? AND variant_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(themeId, Enums.asString(state), variantId);
        return stmt;
    }

    public Statement findThemeVariantByState(final UUID themeId, final ThemeState state) {

        // @formatter:off
        final String QUERY = "SELECT " +
                "  theme_id" +
                ", variant_id" +
                ", variant_name" +
                ", config" +
                ", state" +
                " FROM workspace.theme_variant_by_state " +
                " WHERE theme_id = ? AND state = ?" +
                " ORDER BY state DESC, variant_id DESC " +
                " LIMIT 1";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(themeId, Enums.asString(state));
        return stmt;
    }

    public ThemeVariant fromRow(Row row) {
        return new ThemeVariant()
                .setThemeId(row.getUUID("theme_id"))
                .setVariantId(row.getUUID("variant_id"))
                .setVariantName(row.getString("variant_name"))
                .setConfig(row.getString("config"))
                .setState(Enums.of(ThemeState.class, row.getString("state")));

    }
}
