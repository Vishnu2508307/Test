package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class InteractiveConfigIdMutator extends SimpleTableMutator<InteractiveConfig> {

    @Override
    public String getUpsertQuery(InteractiveConfig mutation) {
        // @formatter:off
        return "INSERT INTO courseware.interactive_config_id ("
                + "  config_id"
                + ", interactive_id"
                + ") VALUES ( ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, InteractiveConfig mutation) {
        stmt.bind(mutation.getId(), mutation.getInteractiveId());
    }
}
