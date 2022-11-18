package com.smartsparrow.sso.data.ltiv11;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

import reactor.util.function.Tuple2;

// Tuple of <launchRequestId, accountId>
public class AccountByLTILaunchRequestMutator extends SimpleTableMutator<Tuple2<UUID, UUID>> {

    @Override
    public String getUpsertQuery(final Tuple2<UUID, UUID> mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.fidm_ltiv11_account_by_launch_request ("
                + "  launch_request_id"
                + ", account_id"
                + ") VALUES (?, ?);";
        // @formatter:on
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final Tuple2<UUID, UUID> mutation) {
        stmt.bind(mutation.getT1(), mutation.getT2());
    }

}
