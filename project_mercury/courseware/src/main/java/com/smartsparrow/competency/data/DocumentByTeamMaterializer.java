package com.smartsparrow.competency.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class DocumentByTeamMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DocumentByTeamMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findDocuments(UUID teamId) {
        final String BY_GROUP = "SELECT document_id" +
                ", team_id " +
                "FROM competency.document_by_team " +
                "WHERE team_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_GROUP);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(teamId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Convert a cassandra row to a document team object
     *
     * @param row the {@link Row} to convert
     * @return a {@link DocumentTeam}
     */
    public DocumentTeam fromRow(Row row) {
        return new DocumentTeam()
                .setTeamId(row.getUUID("team_id"))
                .setDocumentId(row.getUUID("document_id"));
    }
}
