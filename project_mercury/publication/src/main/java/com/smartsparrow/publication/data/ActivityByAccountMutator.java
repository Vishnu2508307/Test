package com.smartsparrow.publication.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ActivityByAccountMutator extends SimpleTableMutator<ActivityByAccount> {

    @Override
    public String getUpsertQuery(ActivityByAccount mutation) {

        return "INSERT INTO publication.activity_by_account (" +
                "  account_id" +
                ", activity_id"
                + ") VALUES (?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ActivityByAccount mutation) {
        stmt.setUUID(0, mutation.getAccountId());
        stmt.setUUID(1, mutation.getActivityId());
    }
}
