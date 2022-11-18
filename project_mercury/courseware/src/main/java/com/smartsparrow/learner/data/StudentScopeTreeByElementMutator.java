package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class StudentScopeTreeByElementMutator extends SimpleTableMutator<StudentScopeTrace> {

    @Override
    public String getUpsertQuery(StudentScopeTrace mutation) {
        return "INSERT INTO learner.student_scope_tree_by_element (" +
                " deployment_id" +
                ", student_id" +
                ", root_id" +
                ", scope_urn" +
                ", scope_id" +
                ", element_id" +
                ", element_type)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, StudentScopeTrace mutation) {
        stmt.bind(
                mutation.getDeploymentId(),
                mutation.getStudentId(),
                mutation.getRootId(),
                mutation.getStudentScopeUrn(),
                mutation.getScopeId(),
                mutation.getElementId(),
                mutation.getElementType().name()
        );
    }
}
