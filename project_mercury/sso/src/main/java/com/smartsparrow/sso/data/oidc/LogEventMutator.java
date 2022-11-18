package com.smartsparrow.sso.data.oidc;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.sso.service.OpenIDConnectLogEvent;

class LogEventMutator extends SimpleTableMutator<OpenIDConnectLogEvent> {

    @Override
    public String getUpsertQuery(OpenIDConnectLogEvent mutation) {
        // @formatter:off
        String query = "INSERT INTO iam_global.fidm_oidc_session_log ("
                + "  session_id"
                + ", id"
                + ", action"
                + ", message"
                + ") VALUES ( ?, ?, ?, ? )";
        // @formatter:on

        if (mutation.getTtl() != null) {
            query += " USING TTL ?";
        }

        return query;
    }

    @Override
    public void bindUpsert(BoundStatement stmt, OpenIDConnectLogEvent mutation) {
        // bind with a TTL?
        if (mutation.getTtl() != null) {
            // yes, a TTL.
            stmt.bind(mutation.getSessionId(), //
                      mutation.getId(), //
                      mutation.getAction().name(), //
                      mutation.getMessage(), //
                      mutation.getTtl());
        } else {
            // no TTL.
            stmt.bind(mutation.getSessionId(), //
                      mutation.getId(), //
                      mutation.getAction().name(), //
                      mutation.getMessage());
        }
    }

}
