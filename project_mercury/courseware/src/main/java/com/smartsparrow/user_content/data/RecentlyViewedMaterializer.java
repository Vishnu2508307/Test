package com.smartsparrow.user_content.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class RecentlyViewedMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public RecentlyViewedMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchAllByAccount(UUID accountId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", account_id"
                + ", root_element_id"
                + ", workspace_id"
                + ", project_id"
                + ", activity_id"
                + ", document_id"
                + ", resource_type"
                + ", last_viewed_at"
                + " FROM user_content.recently_viewed "
                + " WHERE account_id=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public RecentlyViewed fromRow(Row row) {
        return new RecentlyViewed()
                .setId(row.getUUID("id"))
                .setAccountId(row.getUUID("account_id"))
                .setActivityId(row.getUUID("activity_id"))
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setProjectId(row.getUUID("project_id"))
                .setLastViewedAt(row.getUUID("last_viewed_at"))
                .setRootElementId(row.getUUID("root_element_id"))
                .setResourceType(Enums.of(ResourceType.class, row.getString("resource_type")))
                .setDocumentId(row.getUUID("document_id"));

    }
}
