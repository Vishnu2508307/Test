package com.smartsparrow.iam.data.permission.competency;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.Enums;

public class DocumentAccountPermissionMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DocumentAccountPermissionMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchPermission(UUID accountId, UUID documentId) {
        final String BY_COHORT = "SELECT account_id, " +
                "document_id, " +
                "permission_level " +
                "FROM iam_global.document_permission_by_account " +
                "WHERE account_id = ? " +
                "AND document_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_COHORT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId, documentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchPermission(UUID accountId) {
        final String BY_ACCOUNT = "SELECT account_id, " +
                "document_id, " +
                "permission_level " +
                "FROM iam_global.document_permission_by_account " +
                "WHERE account_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ACCOUNT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Convert a cassandra row to an account document permission
     *
     * @param row the {@link Row} to convert
     * @return a {@link AccountDocumentPermission} object
     */
    public AccountDocumentPermission fromRow(Row row) {
        return new AccountDocumentPermission()
                .setAccountId(row.getUUID("account_id"))
                .setDocumentId(row.getUUID("document_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
