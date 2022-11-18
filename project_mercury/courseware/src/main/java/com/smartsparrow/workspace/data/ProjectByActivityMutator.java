package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ProjectByActivityMutator extends SimpleTableMutator<ProjectActivity> {

    @Override
    public String getUpsertQuery(final ProjectActivity mutation) {
        return "INSERT INTO workspace.project_by_activity (" +
                " activity_id" +
                ", project_id" +
                ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final ProjectActivity mutation) {
        stmt.bind(mutation.getActivityId(), mutation.getProjectId());
    }

    @Override
    public String getDeleteQuery(final ProjectActivity mutation) {
        return "DELETE FROM workspace.project_by_activity" +
                " WHERE activity_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final ProjectActivity mutation) {
        stmt.bind(mutation.getActivityId());
    }
}
