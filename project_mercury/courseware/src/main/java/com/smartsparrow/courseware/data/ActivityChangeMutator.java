package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ActivityChangeMutator extends SimpleTableMutator<ActivityChange> {

    @Override
    public String getUpsertQuery(ActivityChange mutation) {
        return "INSERT INTO courseware.activity_change (" +
                "activity_id, " +
                "change_id) " +
                "VALUES (?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ActivityChange mutation) {
        stmt.bind(mutation.getActivityId(), mutation.getChangeId());
    }
}
