package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class StudentScopeEntryBySourceMutator extends SimpleTableMutator<StudentScopeEntry> {

    @Override
    public String getUpsertQuery(StudentScopeEntry mutation) {
        return "INSERT INTO learner.student_scope_entry_by_source (" +
                "id, " +
                "scope_id, " +
                "source_id, " +
                "data) VALUES(?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, StudentScopeEntry mutation) {
        stmt.bind(mutation.getId(),
                mutation.getScopeId(),
                mutation.getSourceId(),
                mutation.getData());
    }
}
