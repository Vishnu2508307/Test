package com.smartsparrow.sso.data.oidc;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.sso.service.SessionAccount;

class SessionByAccountMutator extends SimpleTableMutator<SessionAccount> {

    @Override
    public String getUpsertQuery(SessionAccount mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.fidm_oidc_account_by_session ("
                + "  session_id"
                + ", account_id"
                + ") VALUES ( ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, SessionAccount mutation) {
        stmt.bind(mutation.getSessionId(), //
                  mutation.getAccountId());
    }

}
