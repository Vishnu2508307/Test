package com.smartsparrow.user_content.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class FavoriteMutator extends SimpleTableMutator<Favorite> {

    @Override
    public String getUpsertQuery(Favorite mutation) {
        // @formatter:off
        return "INSERT INTO user_content.favorite ("
                + "  id"
                + ", account_id"
                + ", root_element_id"
                + ", workspace_id"
                + ", project_id"
                + ", activity_id"
                + ", document_id"
                + ", resource_type"
                + ", created_at"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Favorite mutation) {
        stmt.bind(mutation.getId(),
                  mutation.getAccountId(),
                  mutation.getRootElementId(),
                  mutation.getWorkspaceId(),
                  mutation.getProjectId(),
                  mutation.getActivityId(),
                  mutation.getDocumentId(),
                  mutation.getResourceType().name(),
                  mutation.getCreatedAt());
    }

    @Override
    public String getDeleteQuery(final Favorite mutation) {
        return "DELETE FROM user_content.favorite" +
                " WHERE account_id = ? AND id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final Favorite mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getId());
    }

}
