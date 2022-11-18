package com.smartsparrow.competency.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class TeamDocumentCollaboratorMutator extends SimpleTableMutator<TeamDocumentCollaborator> {

    @Override
    public String getUpsertQuery(TeamDocumentCollaborator mutation) {
        return "INSERT INTO competency.team_by_document (" +
                "document_id, " +
                "team_id, " +
                "permission_level) VALUES (?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, TeamDocumentCollaborator mutation) {
        stmt.bind(mutation.getDocumentId(), mutation.getTeamId(), mutation.getPermissionLevel().name());
    }

    @Override
    public String getDeleteQuery(TeamDocumentCollaborator mutation) {
        return "DELETE FROM competency.team_by_document " +
                "WHERE document_id = ? " +
                "AND team_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, TeamDocumentCollaborator mutation) {
        stmt.bind(mutation.getDocumentId(), mutation.getTeamId());
    }
}
