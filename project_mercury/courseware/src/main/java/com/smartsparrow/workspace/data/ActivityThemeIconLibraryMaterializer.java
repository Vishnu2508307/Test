package com.smartsparrow.workspace.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

class ActivityThemeIconLibraryMaterializer implements TableMaterializer {

    private final PreparedStatementCache preparedStatementCache;

    @Inject
    public ActivityThemeIconLibraryMaterializer(final PreparedStatementCache preparedStatementCache) {
        this.preparedStatementCache = preparedStatementCache;
    }

    public Statement fetchById(final UUID activityId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + " activity_id"
                + " , icon_library"
                + " , status"
                + " FROM courseware.icon_library_by_activity"
                + " WHERE activity_id = ?";
        // @formatter:on

        BoundStatement stmt = preparedStatementCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(activityId);
        return stmt;
    }

    public IconLibrary fromRow(Row row) {
        return new IconLibrary()
                .setName(row.getString("icon_library"))
                .setStatus(Enums.of(IconLibraryState.class, row.getString("status")));
    }
}
