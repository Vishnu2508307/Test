package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ActivityByProjectMutator extends SimpleTableMutator<ProjectActivity> {

    @Override
    public String getUpsertQuery(final ProjectActivity mutation) {
        return "INSERT INTO workspace.activity_by_project (" +
                "project_id" +
                ", activity_id" +
                ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final ProjectActivity mutation) {
        stmt.bind(mutation.getProjectId(), mutation.getActivityId());
    }

    @Override
    public String getDeleteQuery(final ProjectActivity mutation) {
        return "DELETE FROM workspace.activity_by_project" +
                " WHERE project_id = ?" +
                " AND activity_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final ProjectActivity mutation) {
        stmt.bind(mutation.getProjectId(), mutation.getActivityId());
    }
}
