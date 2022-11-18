package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class DeletedActivityByIdMutator extends SimpleTableMutator<DeletedActivity> {

    @Override
    public String getUpsertQuery(DeletedActivity mutation) {
        // @formatter:off
        return "INSERT INTO courseware.deleted_activity_by_id ("
                + "  activity_id"
                + ", account_id"
                + ", deleted_at"
                + ") VALUES ( ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, DeletedActivity mutation) {
        stmt.bind(mutation.getActivityId(), mutation.getAccountId(), mutation.getDeletedAt());
    }
}
