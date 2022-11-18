package com.smartsparrow.iam.data.permission.competency;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DocumentTeamPermissionMutator extends SimpleTableMutator<TeamDocumentPermission> {
    @Override
    public String getUpsertQuery(TeamDocumentPermission mutation) {
        return "INSERT INTO iam_global.document_permission_by_team (" +
                "team_id, " +
                "document_id, " +
                "permission_level) VALUES (?,?,?)";
    }

    @Override
    public String getDeleteQuery(TeamDocumentPermission mutation) {
        return "DELETE FROM iam_global.document_permission_by_team " +
                "WHERE team_id = ? " +
                "AND document_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, TeamDocumentPermission mutation) {
        stmt.bind(mutation.getTeamId(),
                mutation.getDocumentId(),
                mutation.getPermissionLevel().name());
    }

    @Override
    public void bindDelete(BoundStatement stmt, TeamDocumentPermission mutation) {
        stmt.bind(mutation.getTeamId(),
                mutation.getDocumentId());
    }
}
