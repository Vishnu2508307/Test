package com.smartsparrow.sso.data.ltiv11;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.sso.service.LTILaunchRequestEntry;

class LTILaunchRequestStaticFieldsMutator extends SimpleTableMutator<LTILaunchRequestEntry> {

    @Override
    public String getUpsertQuery(final LTILaunchRequestEntry mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.fidm_ltiv11_launch_request ("
                + "  id"
                + ", ltiv11_credential_id"
                + ", request_url"
                + ") VALUES (?, ?, ?);";
        // @formatter:on
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final LTILaunchRequestEntry mutation) {
        stmt.bind(mutation.getLaunchRequestId(), mutation.getLtiv11CredentialId(), mutation.getRequestUrl());
    }

}
