package com.smartsparrow.learner.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LatestStudentScopeEntryMutator extends SimpleTableMutator<StudentScopeEntry> {

    @Override
    public String getUpsertQuery(StudentScopeEntry mutation) {
        // @formatter:off
        return "INSERT INTO learner.latest_student_scope_entry_by_source ("
                + "  scope_id"
                + ", source_id"
                + ", data"
                + ") VALUES (?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, StudentScopeEntry mutation) {
        stmt.bind(
                mutation.getScopeId(),
                mutation.getSourceId(),
                mutation.getData()
        );
    }
}
