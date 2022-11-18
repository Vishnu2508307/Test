package com.smartsparrow.sso.data.ltiv11;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

class LTI11ConsumerCredentialsByConfigurationMutator extends SimpleTableMutator<LTIv11ConsumerKey> {

    @Override
    public String getUpsertQuery(LTIv11ConsumerKey mutation) {
        return "INSERT INTO iam_global.lti11_consumer_credentials_by_configuration (" +
                " id" +
                ", oauth_consumer_key" +
                ", oauth_consumer_secret" +
                ", cohort_id" +
                ", workspace_id" +
                ", consumer_configuration_id" +
                ", log_debug)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LTIv11ConsumerKey mutation) {
        stmt.bind(
                mutation.getId(),
                mutation.getOauthConsumerKey(),
                mutation.getOauthConsumerSecret(),
                mutation.getCohortId(),
                mutation.getWorkspaceId(),
                mutation.getConsumerConfigurationId(),
                mutation.isLogDebug()
        );
    }
}
