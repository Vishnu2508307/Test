package com.smartsparrow.sso.data.ltiv11;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class LTI11ConsumerConfigurationByWorkspaceMutator extends SimpleTableMutator<LTIv11ConsumerConfiguration> {

    @Override
    public String getUpsertQuery(LTIv11ConsumerConfiguration mutation) {
        return "INSERT INTO iam_global.lti11_consumer_configuration_by_workspace (" +
                " workspace_id" +
                ", id" +
                ", comment)" +
                " VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LTIv11ConsumerConfiguration mutation) {
        stmt.bind(mutation.getWorkspaceId(), mutation.getId(), mutation.getComment());
    }
}
