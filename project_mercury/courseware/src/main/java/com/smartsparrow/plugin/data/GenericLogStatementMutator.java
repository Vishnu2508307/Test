package com.smartsparrow.plugin.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class GenericLogStatementMutator extends SimpleTableMutator<GenericLogStatement> {

    @Override
    public String getUpsertQuery(GenericLogStatement mutation) {
        return "INSERT INTO plugin.generic_log_statement_by_plugin ("
                + "plugin_id"
                + ", version"
                + ", bucket_id"
                + ", level"
                + ", id"
                + ", message"
                + ", args"
                + ", plugin_context"
                + ", logging_context"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
    }

    @Override
    public String getDeleteQuery(GenericLogStatement mutation) {
        return "DELETE FROM plugin.generic_log_statement_by_plugin " +
                "WHERE plugin_id = ? AND " +
                "version = ? AND " +
                "bucket_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, GenericLogStatement mutation) {
        stmt.bind(mutation.getPluginId(), mutation.getVersion(), mutation.getBucketId());
    }

    @Override
    public void bindUpsert(BoundStatement stmt, GenericLogStatement mutation) {
        stmt.bind(mutation.getPluginId(),
                  mutation.getVersion(),
                  mutation.getBucketId(),
                  mutation.getLevel().name(),
                  mutation.getId(),
                  mutation.getMessage(),
                  mutation.getArgs(),
                  mutation.getPluginContext(),
                  mutation.getLoggingContext().name());
    }
}
