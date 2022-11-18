package com.smartsparrow.cohort.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class LtiConsumerCredentialDetailByCohortMutator extends SimpleTableMutator<LtiConsumerCredentialDetail> {

    @Override
    public String getUpsertQuery(LtiConsumerCredentialDetail mutation) {
        return "INSERT INTO cohort.lti_consumer_cred_v11_by_cohort (" +
                "cohort_id, " +
                "key, " +
                "secret, " +
                "created_date, " +
                "log_debug) " +
                "VALUES(?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LtiConsumerCredentialDetail mutation) {
        stmt.bind(
                mutation.getCohortId(),
                mutation.getKey(),
                mutation.getSecret(),
                mutation.getCreatedDate(),
                mutation.isLogDebug()
        );
    }
}
