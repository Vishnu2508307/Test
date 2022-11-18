package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class StudentScopeMutator extends SimpleTableMutator<StudentScope> {

    @Override
    public String getUpsertQuery(StudentScope mutation) {
        return "INSERT INTO learner.student_scope (" +
                "deployment_id, " +
                "account_id, " +
                "scope_urn, " +
                "id) VALUES(?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, StudentScope mutation) {
        stmt.bind(mutation.getDeploymentId(),
                mutation.getAccountId(),
                mutation.getScopeUrn(),
                mutation.getId());
    }
}
