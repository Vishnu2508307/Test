package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class ActivityThemeMutator extends SimpleTableMutator<ActivityTheme> {

    @Override
    public String getUpsertQuery(ActivityTheme mutation) {
        // @formatter:off
        return "INSERT INTO courseware.activity_theme ("
                + "  id"
                + ", activity_id"
                + ", config"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ActivityTheme mutation) {
        stmt.bind(mutation.getId(), mutation.getActivityId(), mutation.getConfig());
    }
}
