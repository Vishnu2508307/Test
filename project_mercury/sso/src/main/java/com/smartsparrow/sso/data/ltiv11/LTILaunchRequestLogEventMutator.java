package com.smartsparrow.sso.data.ltiv11;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.sso.service.LTILaunchRequestLogEvent;
import com.smartsparrow.util.Enums;

class LTILaunchRequestLogEventMutator extends SimpleTableMutator<LTILaunchRequestLogEvent> {

    @Override
    public String getUpsertQuery(final LTILaunchRequestLogEvent mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.fidm_ltiv11_launch_request_log ("
                + "  launch_request_id"
                + ", id"
                + ", status"
                + ", message"
                + ") VALUES (?, ?, ?, ?);";
        // @formatter:on
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final LTILaunchRequestLogEvent mutation) {
        stmt.setUUID(0, mutation.getLaunchRequestId());
        stmt.setUUID(1, mutation.getId());
        stmt.setString(2, Enums.asString(mutation.getAction()));
        optionalBind(stmt, 3, mutation.getMessage(), String.class);
    }

}
