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

class ThemeVariantMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public ThemeVariantMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findVariantByThemeId(final UUID themeId) {

        // @formatter:off
        final String QUERY = "SELECT " +
                "  theme_id" +
                ", variant_id" +
                ", variant_name" +
                ", config" +
                " FROM workspace.theme_variant " +
                " WHERE theme_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(themeId);
        return stmt;
    }

    public Statement findVariantByThemeIdAndVariantId(final UUID themeId, final UUID variantId) {

        // @formatter:off
        final String QUERY = "SELECT " +
                "  theme_id" +
                ", variant_id" +
                ", variant_name" +
                ", config" +
                " FROM workspace.theme_variant " +
                " WHERE theme_id = ? AND variant_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(themeId, variantId);
        return stmt;
    }

    public ThemeVariant fromRow(Row row) {
        return new ThemeVariant()
                .setThemeId(row.getUUID("theme_id"))
                .setVariantId(row.getUUID("variant_id"))
                .setVariantName(row.getString("variant_name"))
                .setConfig(row.getString("config"));

    }
}
