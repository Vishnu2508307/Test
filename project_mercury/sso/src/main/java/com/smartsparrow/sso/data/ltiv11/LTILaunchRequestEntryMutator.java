package com.smartsparrow.sso.data.ltiv11;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.sso.service.LTILaunchRequestEntry;
import com.smartsparrow.util.Enums;

class LTILaunchRequestEntryMutator extends SimpleTableMutator<LTILaunchRequestEntry> {

    // This class intentionally does not manage the static request_url field in this table.

    @Override
    public String getUpsertQuery(LTILaunchRequestEntry mutation) {
        // @formatter:off
        String query = "INSERT INTO iam_global.fidm_ltiv11_launch_request ("
                + "  id"
                + ", part"
                + ", name"
                + ", value"
                + ") VALUES (?, ?, ?, ?)";
        // @formatter:on

        if (mutation.getTtl() != null) {
            query += " USING TTL ?";
        }

        return query;
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final LTILaunchRequestEntry mutation) {
        // bind with a TTL?
        if (mutation.getTtl() != null) {
            // yes, a TTL.
            stmt.bind(mutation.getLaunchRequestId(), //
                    Enums.asString(mutation.getPart()), //
                    mutation.getName(), //
                    mutation.getValue(), //
                    mutation.getTtl());
        } else {
            // no TTL.
            stmt.bind(mutation.getLaunchRequestId(), //
                    Enums.asString(mutation.getPart()), //
                    mutation.getName(), //
                    mutation.getValue());
        }
    }

}
