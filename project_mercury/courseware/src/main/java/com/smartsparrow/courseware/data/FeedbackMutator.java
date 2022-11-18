package com.smartsparrow.courseware.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class FeedbackMutator extends SimpleTableMutator<Feedback> {

    @Override
    public String getUpsertQuery(Feedback mutation) {
        // @formatter:off
        return "INSERT INTO courseware.feedback ("
                + "  id"
                + ", plugin_id"
                + ", plugin_version_expr"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Feedback mutation) {
        stmt.bind(mutation.getId(), mutation.getPluginId(), mutation.getPluginVersionExpr());
    }
}
