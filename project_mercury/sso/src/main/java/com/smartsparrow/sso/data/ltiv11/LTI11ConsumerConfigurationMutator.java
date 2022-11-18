package com.smartsparrow.sso.data.ltiv11;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class LTI11ConsumerConfigurationMutator extends SimpleTableMutator<LTIv11ConsumerConfiguration> {

    @Override
    public String getUpsertQuery(LTIv11ConsumerConfiguration mutation) {
        return "INSERT INTO iam_global.lti11_consumer_configuration (" +
                " id" +
                ", workspace_id" +
                ", comment)" +
                " VALUES (?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LTIv11ConsumerConfiguration mutation) {
        stmt.bind(mutation.getId(), mutation.getWorkspaceId(), mutation.getComment());
    }
}
