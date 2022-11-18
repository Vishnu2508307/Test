package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class LearnerInteractiveMutator extends SimpleTableMutator<LearnerInteractive> {

    @Override
    public String getUpsertQuery(LearnerInteractive mutation) {
        return "INSERT INTO learner.interactive (" +
                "id, " +
                "deployment_id, " +
                "change_id, " +
                "plugin_id, " +
                "plugin_version_expr, " +
                "config, " +
                "student_scope_urn, " +
                "evaluation_mode) VALUES(?,?,?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerInteractive mutation) {
        stmt.bind(
                mutation.getId(),
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getPluginId(),
                mutation.getPluginVersionExpr(),
                mutation.getConfig(),
                mutation.getStudentScopeURN(),
                Enums.asString(mutation.getEvaluationMode())
        );
    }
}
