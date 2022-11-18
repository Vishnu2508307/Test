package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class InteractiveConfigLatestMutator extends SimpleTableMutator<InteractiveConfig> {

    @Override
    public String getUpsertQuery(InteractiveConfig mutation) {
        // @formatter:off
        return "INSERT INTO courseware.interactive_config_latest ("
                + "  id"
                + ", interactive_id"
                + ", config"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, InteractiveConfig mutation) {
        stmt.bind(mutation.getId(), mutation.getInteractiveId(), mutation.getConfig());
    }
}
