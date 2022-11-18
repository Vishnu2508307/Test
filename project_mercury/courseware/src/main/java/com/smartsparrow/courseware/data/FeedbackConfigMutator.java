package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class FeedbackConfigMutator extends SimpleTableMutator<FeedbackConfig> {
    @Override
    public String getUpsertQuery(FeedbackConfig mutation) {
        // @formatter:off
        return "INSERT INTO courseware.feedback_config ("
                + "  id"
                + ", feedback_id"
                + ", config"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, FeedbackConfig mutation) {
        stmt.bind(mutation.getId(), mutation.getFeedbackId(), mutation.getConfig());
    }
}
