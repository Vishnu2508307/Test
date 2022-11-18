package com.smartsparrow.competency.data;

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

public class AccountDocumentCollaboratorMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public AccountDocumentCollaboratorMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchAccounts(UUID documentId) {
        final String BY_DOCUMENT = "SELECT document_id, " +
                "account_id, " +
                "permission_level " +
                "FROM competency.account_by_document " +
                "WHERE document_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_DOCUMENT);
        stmt.bind(documentId);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        return stmt;

    }

    public Statement fetchAccount(UUID documentId, UUID accountId) {
        final String BY_ACCOUNT = "SELECT document_id, " +
                "account_id, " +
                "permission_level " +
                "FROM competency.account_by_document " +
                "WHERE document_id = ? " +
                "AND account_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ACCOUNT);
        stmt.bind(documentId, accountId);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Convert a cassandra row to a account document collaborator object
     *
     * @param row the {@link Row} to convert
     * @return a {@link AccountDocumentCollaborator}
     */
    public AccountDocumentCollaborator fromRow(Row row) {
        return new AccountDocumentCollaborator()
                .setAccountId(row.getUUID("account_id"))
                .setDocumentId(row.getUUID("document_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }

}
