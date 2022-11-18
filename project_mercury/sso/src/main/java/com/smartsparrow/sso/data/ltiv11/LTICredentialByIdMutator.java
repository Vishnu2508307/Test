package com.smartsparrow.sso.data.ltiv11;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.sso.service.LTIConsumerKey;

/**
 * This class has been deprecated and should not be used
 */
@Deprecated
class LTICredentialByIdMutator extends SimpleTableMutator<LTIConsumerKey> {

    @Override
    public String getUpsertQuery(LTIConsumerKey mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.fidm_ltiv11_credential ("
                                            + "  id"
                                            + ", oauth_consumer_key"
                                            + ", oauth_consumer_secret"
                                            + ", subscription_id"
                                            + ", comment"
                                            + ", log_debug"
                                            + ") VALUES (?, ?, ?, ?, ?, ?)";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LTIConsumerKey mutation) {
        stmt.bind(mutation.getId(),
                  mutation.getKey(),
                  mutation.getSecret(),
                  mutation.getSubscriptionId(),
                  mutation.getComment(),
                  mutation.isLogDebug());
    }

}
