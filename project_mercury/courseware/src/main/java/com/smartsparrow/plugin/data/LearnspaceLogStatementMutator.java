package com.smartsparrow.plugin.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnspaceLogStatementMutator extends SimpleTableMutator<LearnspaceLogStatement> {

    @Override
    public String getUpsertQuery(LearnspaceLogStatement mutation) {
        return "INSERT INTO plugin.learnspace_log_statement_by_plugin ("
                + "plugin_id"
                + ", version"
                + ", bucket_id"
                + ", level"
                + ", id"
                + ", message"
                + ", args"
                + ", element_id"
                + ", element_type"
                + ", deployment_id"
                + ", cohort_id"
                + ", plugin_context"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
    }

    @Override
    public String getDeleteQuery(LearnspaceLogStatement mutation) {
        return "DELETE FROM plugin.learnspace_log_statement_by_plugin " +
                "WHERE plugin_id = ? AND " +
                "version = ? AND " +
                "bucket_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, LearnspaceLogStatement mutation) {
        stmt.bind(mutation.getPluginId(), mutation.getVersion(), mutation.getBucketId());
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnspaceLogStatement mutation) {
        stmt.bind(mutation.getPluginId(),
                  mutation.getVersion(),
                  mutation.getBucketId(),
                  mutation.getLevel().name(),
                  mutation.getId(),
                  mutation.getMessage(),
                  mutation.getArgs(),
                  mutation.getElementId(),
                  mutation.getElementType().name(),
                  mutation.getDeploymentId(),
                  mutation.getCohortId(),
                  mutation.getPluginContext());
    }
}
