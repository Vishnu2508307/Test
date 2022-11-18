package com.smartsparrow.iam.data;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.Subscription;

class SubscriptionMutator extends SimpleTableMutator<Subscription> {

    private final PreparedStatementCache stmtCache;

    @Inject
    public SubscriptionMutator(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    @Override
    public String getUpsertQuery(Subscription mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.subscription ("
                + "  id"
                + ", name"
                + ", iam_region"
                + ") VALUES (?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Subscription mutation) {
        stmt.bind(mutation.getId(), mutation.getName(), mutation.getIamRegion().name());
    }

}
