package com.smartsparrow.sso.data.oidc;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.sso.service.SessionAccount;

class AccountBySessionMutator extends SimpleTableMutator<SessionAccount> {

    @Override
    public String getUpsertQuery(SessionAccount mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.fidm_oidc_session_by_account ("
                + "  account_id"
                + ", id"
                + ", session_id"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, SessionAccount mutation) {
        stmt.bind(mutation.getAccountId(), //
                  mutation.getId(), //
                  mutation.getSessionId());
    }

}
