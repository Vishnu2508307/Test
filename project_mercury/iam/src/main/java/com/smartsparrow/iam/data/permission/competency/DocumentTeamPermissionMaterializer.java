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

public class DocumentTeamPermissionMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public DocumentTeamPermissionMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchPermission(UUID teamId, UUID documentId) {
        final String BY_COHORT = "SELECT team_id, " +
                "document_id, " +
                "permission_level " +
                "FROM iam_global.document_permission_by_team " +
                "WHERE team_id = ? " +
                "AND document_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_COHORT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(teamId, documentId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchPermission(UUID teamId) {
        final String BY_ACCOUNT = "SELECT team_id, " +
                "document_id, " +
                "permission_level " +
                "FROM iam_global.document_permission_by_team " +
                "WHERE team_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ACCOUNT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(teamId);
        stmt.setIdempotent(true);
        return stmt;
    }

    /**
     * Convert a cassandra row to an team document permission
     *
     * @param row the {@link Row} to convert
     * @return a {@link TeamDocumentPermission} object
     */
    public TeamDocumentPermission fromRow(Row row) {
        return new TeamDocumentPermission()
                .setTeamId(row.getUUID("team_id"))
                .setDocumentId(row.getUUID("document_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }

}
