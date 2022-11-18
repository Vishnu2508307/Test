package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LearnerStudentScopeRegistryMutator extends SimpleTableMutator<LearnerScopeReference> {

    @Override
    public String getUpsertQuery(LearnerScopeReference mutation) {
        return "INSERT INTO learner.student_scope_registry (" +
                "student_scope_urn" +
                ", deployment_id" +
                ", element_id" +
                ", change_id" +
                ", element_type" +
                ", plugin_id" +
                ", plugin_version) VALUES ( ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LearnerScopeReference mutation) {
        stmt.bind(
                mutation.getScopeURN(),
                mutation.getDeploymentId(),
                mutation.getElementId(),
                mutation.getChangeId(),
                mutation.getElementType().name(),
                mutation.getPluginId(),
                mutation.getPluginVersion()
        );
    }
}
