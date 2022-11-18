package com.smartsparrow.user_content.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class SharedResourceMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;
    @Inject
    public SharedResourceMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchAllByAccount(UUID accountId) {
        // @formatter:off
        final String QUERY = "SELECT "
                + "  id"
                + ", account_id"
                + ", shared_account_id"
                + ", resource_id"
                + ", resource_type"
                + ", shared_at"
                + " FROM user_content.shared_resource "
                + " WHERE account_id=?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public SharedResource fromRow(Row row) {
        return new SharedResource()
                .setId(row.getUUID("id"))
                .setAccountId(row.getUUID("account_id"))
                .setSharedAccountId(row.getUUID("shared_account_id"))
                .setResourceId(row.getUUID("resource_id"))
                .setResourceType(Enums.of(ResourceType.class, row.getString("resource_type")))
                .setSharedAt(row.getUUID("shared_at"));
    }
}
