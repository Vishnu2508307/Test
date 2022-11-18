package com.smartsparrow.user_content.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class FavoriteMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public FavoriteMaterializer(final PreparedStatementCache stmtCache) {
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
                + ", created_at"
                + " FROM user_content.favorite "
                + " WHERE account_id=? ALLOW FILTERING";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchOlderFavorites(long createdAt) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  account_id"
                + ", root_element_id"
                + ", workspace_id"
                + ", project_id"
                + ", activity_id"
                + ", document_id"
                + ", resource_type"
                + ", created_at"
                + " FROM user_content.favorite "
                + " WHERE created_at >= ? ALLOW FILTERING";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.bind(createdAt);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Favorite fromRow(Row row) {
        return new Favorite()
                .setId(row.getUUID("id"))
                .setAccountId(row.getUUID("account_id"))
                .setActivityId(row.getUUID("activity_id"))
                .setWorkspaceId(row.getUUID("workspace_id"))
                .setProjectId(row.getUUID("project_id"))
                .setCreatedAt(row.getUUID("created_at"))
                .setRootElementId(row.getUUID("root_element_id"))
                .setResourceType(Enums.of(ResourceType.class, row.getString("resource_type")))
                .setDocumentId(row.getUUID("document_id"));
    }
}
