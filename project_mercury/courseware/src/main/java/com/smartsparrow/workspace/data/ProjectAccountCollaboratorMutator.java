package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class ProjectAccountCollaboratorMutator extends SimpleTableMutator<ProjectAccountCollaborator> {

    @Override
    public String getUpsertQuery(final ProjectAccountCollaborator mutation) {
        return "INSERT INTO workspace.account_by_project (" +
                "project_id" +
                ", account_id" +
                ", permission_level" +
                ") VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final ProjectAccountCollaborator mutation) {
        stmt.bind(
                mutation.getProjectId(),
                mutation.getAccountId(),
                Enums.asString(mutation.getPermissionLevel())
        );
    }

    @Override
    public String getDeleteQuery(final ProjectAccountCollaborator mutation) {
        return "DELETE FROM workspace.account_by_project" +
                " WHERE project_id = ?" +
                " AND account_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final ProjectAccountCollaborator mutation) {
        stmt.bind(mutation.getProjectId(), mutation.getAccountId());
    }
}
