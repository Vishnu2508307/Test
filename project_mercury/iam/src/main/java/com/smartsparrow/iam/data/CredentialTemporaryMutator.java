package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.CredentialTemporary;

class CredentialTemporaryMutator extends SimpleTableMutator<CredentialTemporary> {

    @Override
    public String getUpsertQuery(CredentialTemporary mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.credential_temporary ("
                + "  authorization_code"
                + ", type"
                + ", account_id"
                + ") VALUES ( ?, ?, ? )"
                + " USING TTL ?";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CredentialTemporary mutation) {
        stmt.bind(mutation.getAuthorizationCode(), mutation.getType().name(), mutation.getAccountId(),
                  (int) mutation.getType().TTL());
    }

    @Override
    public String getDeleteQuery(CredentialTemporary mutation) {
        // @formatter:off
        return "DELETE FROM iam_global.credential_temporary"
                + "  WHERE authorization_code = ?";
        // @formatter:on
    }

    @Override
    public void bindDelete(BoundStatement stmt, CredentialTemporary mutation) {
        stmt.bind(mutation.getAuthorizationCode());
    }

}
