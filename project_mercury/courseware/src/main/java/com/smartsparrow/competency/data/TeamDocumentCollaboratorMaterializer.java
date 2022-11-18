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

public class TeamDocumentCollaboratorMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public TeamDocumentCollaboratorMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findTeams(UUID documentId) {
        final String BY_DOCUMENT = "SELECT document_id, " +
                "team_id, " +
                "permission_level " +
                "FROM competency.team_by_document " +
                "WHERE document_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_DOCUMENT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(documentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Convert a cassandra row to a  team document collaborator object
     *
     * @param row the {@link Row} to convert
     * @return a {@link TeamDocumentCollaborator}
     */
    public TeamDocumentCollaborator fromRow(Row row) {
        return new TeamDocumentCollaborator()
                .setTeamId(row.getUUID("team_id"))
                .setDocumentId(row.getUUID("document_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
