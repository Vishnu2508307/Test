package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerFeedbackMutator extends SimpleTableMutator<LearnerFeedback> {

    @Override
    public String getUpsertQuery(LearnerFeedback mutation) {
        return "INSERT INTO learner.feedback (" +
                "id, " +
                "deployment_id, " +
                "change_id, " +
                "plugin_id, " +
                "plugin_version_expr, " +
                "config) VALUES(?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerFeedback mutation) {
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
