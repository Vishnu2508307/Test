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

class IconLibraryByThemeMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    public IconLibraryByThemeMaterializer(final PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchById(final UUID themeId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + " theme_id"
                + " , icon_library"
                + " , status"
                + " FROM workspace.icon_library_by_theme"
                + " WHERE theme_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(themeId);
        return stmt;
    }

    public IconLibrary fromRow(Row row) {
        return new IconLibrary()
                .setName(row.getString("icon_library"))
                .setStatus(Enums.of(IconLibraryState.class, row.getString("status")));
    }
}
