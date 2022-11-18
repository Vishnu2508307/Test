package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

@Deprecated
public class LearnerActivityMutator extends SimpleTableMutator<LearnerActivity> {

    @Override
    public String getUpsertQuery(LearnerActivity mutation) {
        return "INSERT INTO learner.activity (" +
                "id, " +
                "deployment_id, " +
                "change_id, " +
                "plugin_id, " +
                "plugin_version_expr, " +
                "config, " +
                "theme, " +
                "creator_id, " +
                "student_scope_urn, " +
                "evaluation_mode) VALUES (?,?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerActivity mutation) {
        stmt.bind(
                mutation.getId(),
                mutation.getDeploymentId(),
                mutation.getChangeId(),
                mutation.getPluginId(),
                mutation.getPluginVersionExpr(),
                mutation.getConfig(),
                mutation.getTheme(),
                mutation.getCreatorId(),
                mutation.getStudentScopeURN(),
                Enums.asString(mutation.getEvaluationMode())
        );
    }
}
