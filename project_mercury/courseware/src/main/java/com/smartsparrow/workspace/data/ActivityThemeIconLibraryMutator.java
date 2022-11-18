package com.smartsparrow.workspace.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class ActivityThemeIconLibraryMutator extends SimpleTableMutator<ActivityThemeIconLibrary> {

    @Override
    public String getUpsertQuery(ActivityThemeIconLibrary mutation) {
        // @formatter:off
        return "INSERT INTO courseware.icon_library_by_activity ("
                + "activity_id"
                + ", icon_library"
                + ", status"
                + ") VALUES ( ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, ActivityThemeIconLibrary mutation) {
        stmt.bind(mutation.getActivityId(), mutation.getIconLibrary(), Enums.asString(mutation.getStatus()));
    }

}
