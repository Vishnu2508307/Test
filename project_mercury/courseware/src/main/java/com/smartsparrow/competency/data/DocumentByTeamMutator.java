package com.smartsparrow.competency.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DocumentByTeamMutator extends SimpleTableMutator<DocumentTeam> {

    @Override
    public String getUpsertQuery(DocumentTeam mutation) {
        return "INSERT INTO competency.document_by_team (" +
                "team_id, " +
                "document_id) VALUES (?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DocumentTeam mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getDocumentId());
    }

    @Override
    public String getDeleteQuery(DocumentTeam mutation) {
        return "DELETE FROM competency.document_by_team " +
                "WHERE team_id = ? " +
                "AND document_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, DocumentTeam mutation) {
        stmt.bind(mutation.getTeamId(), mutation.getDocumentId());
    }
}
