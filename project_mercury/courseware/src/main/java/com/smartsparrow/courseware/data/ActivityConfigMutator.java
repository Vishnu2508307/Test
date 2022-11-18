package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class ActivityConfigMutator extends SimpleTableMutator<ActivityConfig> {

    @Override
    public String getUpsertQuery(ActivityConfig mutation) {
        // @formatter:off
        return "INSERT INTO courseware.activity_config ("
                + "  id"
                + ", activity_id"
                + ", config"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ActivityConfig mutation) {
        stmt.bind(mutation.getId(), mutation.getActivityId(), mutation.getConfig());
    }
}
