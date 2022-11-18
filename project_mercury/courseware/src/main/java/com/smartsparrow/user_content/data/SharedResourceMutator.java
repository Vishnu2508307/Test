package com.smartsparrow.user_content.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class SharedResourceMutator extends SimpleTableMutator<SharedResource> {

    @Override
    public String getUpsertQuery(SharedResource mutation) {
        // @formatter:off
        return "INSERT INTO user_content.shared_resource ("
                + "  id"
                + ", account_id"
                + ", shared_account_id"
                + ", resource_id"
                + ", resource_type"
                + ", shared_at"
                + ") VALUES ( ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, SharedResource mutation) {
        stmt.bind(mutation.getId(),
                  mutation.getAccountId(),
                  mutation.getSharedAccountId(),
                  mutation.getResourceId(),
                  mutation.getResourceType().name(),
                  mutation.getSharedAt());
    }

    @Override
    public String getDeleteQuery(final SharedResource mutation) {
        return "DELETE FROM user_content.shared_resource" +
                " WHERE id = ? AND account_id = ? AND shared_account_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final SharedResource mutation) {
        stmt.bind(mutation.getId(), mutation.getAccountId(), mutation.getSharedAccountId());
    }
}
