package com.smartsparrow.plugin.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class WorkspaceLogStatementMutator extends SimpleTableMutator<WorkspaceLogStatement> {

    @Override
    public String getUpsertQuery(WorkspaceLogStatement mutation) {
        return "INSERT INTO plugin.workspace_log_statement_by_plugin ("
                + "plugin_id"
                + ", version"
                + ", bucket_id"
                + ", level"
                + ", id"
                + ", message"
                + ", args"
                + ", project_id"
                + ", element_id"
                + ", element_type"
                + ", plugin_context"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
    }

    @Override
    public String getDeleteQuery(WorkspaceLogStatement mutation) {
        return "DELETE FROM plugin.workspace_log_statement_by_plugin " +
                "WHERE plugin_id = ? AND " +
                "version = ? AND " +
                "bucket_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, WorkspaceLogStatement mutation) {
        stmt.bind(mutation.getPluginId(), mutation.getVersion(), mutation.getBucketId());
    }

    @Override
    public void bindUpsert(BoundStatement stmt, WorkspaceLogStatement mutation) {
        stmt.bind(mutation.getPluginId(),
                  mutation.getVersion(),
                  mutation.getBucketId(),
                  mutation.getLevel().name(),
                  mutation.getId(),
                  mutation.getMessage(),
                  mutation.getArgs(),
                  mutation.getProjectId(),
                  mutation.getElementId(),
                  mutation.getElementType().name(),
                  mutation.getPluginContext());
    }
}
