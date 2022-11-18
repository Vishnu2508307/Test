package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerComponentMutator extends SimpleTableMutator<LearnerComponent> {

    @Override
    public String getUpsertQuery(LearnerComponent mutation) {
        return "INSERT INTO learner.component (" +
                "id, " +
                "deployment_id, " +
                "change_id, " +
                "plugin_id, " +
                "plugin_version_expr, " +
                "config) VALUES (?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerComponent mutation) {
        stmt.bind(
                mutation.getId(),
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getPluginId(),
                mutation.getPluginVersionExpr(),
                mutation.getConfig()
        );
    }
}
