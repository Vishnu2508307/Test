package com.smartsparrow.iam.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class CredentialByHashMutator extends SimpleTableMutator<CredentialsType> {

    @Override
    public String getUpsertQuery(CredentialsType mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.credential_type_by_hash ("
                + "  hash"
                + ", credential_type"
                + ", account_id"
                + ") VALUES ( ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CredentialsType mutation) {
        stmt.setString(0, mutation.getHash());
        stmt.setString(1, Enums.asString(mutation.getAuthenticationType()));
        stmt.setUUID(2, mutation.getAccountId());
    }

}
