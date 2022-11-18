package com.smartsparrow.sso.data.ltiv11;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

class LTI11LaunchBySessionHashMutator extends SimpleTableMutator<LTI11LaunchSessionHash> {

    @Override
    public String getUpsertQuery(LTI11LaunchSessionHash mutation) {
        return "INSERT INTO iam_global.lti11_launch_by_session_hash (" +
                " hash" +
                ", launch_request_id" +
                ", status" +
                ", user_id" +
                ", cohort_id" +
                ", configuration_id" +
                ", continue_to" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, LTI11LaunchSessionHash mutation) {
        stmt.bind(
                mutation.getHash(),
                mutation.getLaunchRequestId(),
                Enums.asString(mutation.getStatus()),
                mutation.getUserId(),
                mutation.getCohortId(),
                mutation.getConfigurationId(),
                mutation.getContinueTo()
        );
    }
}
