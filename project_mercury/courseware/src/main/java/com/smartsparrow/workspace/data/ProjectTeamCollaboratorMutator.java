package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class ProjectTeamCollaboratorMutator extends SimpleTableMutator<ProjectTeamCollaborator> {

    @Override
    public String getUpsertQuery(final ProjectTeamCollaborator mutation) {
        return "INSERT INTO workspace.team_by_project (" +
                "project_id" +
                ", team_id" +
                ", permission_level" +
                ") VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final ProjectTeamCollaborator mutation) {
        stmt.bind(
                mutation.getProjectId(),
                mutation.getTeamId(),
                Enums.asString(mutation.getPermissionLevel())
        );
    }

    @Override
    public String getDeleteQuery(final ProjectTeamCollaborator mutation) {
        return "DELETE FROM workspace.team_by_project" +
                " WHERE project_id = ?" +
                " AND team_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final ProjectTeamCollaborator mutation) {
        stmt.bind(mutation.getProjectId(), mutation.getTeamId());
    }
}
