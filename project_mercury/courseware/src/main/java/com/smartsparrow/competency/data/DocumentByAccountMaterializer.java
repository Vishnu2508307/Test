package com.smartsparrow.competency.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class DocumentByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DocumentByAccountMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchDocuments(UUID accountId) {
        final String BY_ACCOUNT = "SELECT account_id, " +
                "document_id " +
                "FROM competency.document_by_account " +
                "WHERE account_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ACCOUNT);
        stmt.bind(accountId);
        stmt.setIdempotent(true);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        return stmt;
    }

    /**
     * Convert a cassandra row to a document account object
     *
     * @param row the {@link Row} to convert
     * @return a {@link DocumentAccount}
     */
    public DocumentAccount fromRow(Row row) {
        return new DocumentAccount()
                .setAccountId(row.getUUID("account_id"))
                .setDocumentId(row.getUUID("document_id"));
    }
}
