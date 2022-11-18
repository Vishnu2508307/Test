package com.smartsparrow.user_content.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class RecentlyViewedMutator extends SimpleTableMutator<RecentlyViewed> {

    @Override
    public String getUpsertQuery(RecentlyViewed mutation) {
        // @formatter:off
        return "INSERT INTO user_content.recently_viewed ("
                + "  id"
                + ", account_id"
                + ", root_element_id"
                + ", workspace_id"
                + ", project_id"
                + ", activity_id"
                + ", document_id"
                + ", resource_type"
                + ", last_viewed_at"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, RecentlyViewed mutation) {
        stmt.bind(mutation.getId(),
                  mutation.getAccountId(),
                  mutation.getRootElementId(),
                  mutation.getWorkspaceId(),
                  mutation.getProjectId(),
                  mutation.getActivityId(),
                  mutation.getDocumentId(),
                  mutation.getResourceType().name(),
                  mutation.getLastViewedAt());
    }

    @Override
    public String getDeleteQuery(final RecentlyViewed mutation) {
        return "DELETE FROM user_content.recently_viewed" +
                " WHERE id = ? AND account_id = ?";
    }

    @Override
    public void bindDelete(final BoundStatement stmt, final RecentlyViewed mutation) {
        stmt.bind(mutation.getId(), mutation.getAccountId());
    }

}
