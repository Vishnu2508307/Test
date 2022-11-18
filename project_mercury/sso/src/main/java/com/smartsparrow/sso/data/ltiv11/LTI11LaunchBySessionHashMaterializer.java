package com.smartsparrow.sso.data.ltiv11;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

class LTI11LaunchBySessionHashMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    LTI11LaunchBySessionHashMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findBy(final String hash, final UUID launchRequestId) {
        final String SELECT = "SELECT" +
                " hash" +
                ", launch_request_id" +
                ", status" +
                ", user_id" +
                ", cohort_id" +
                ", configuration_id" +
                ", continue_to" +
                " FROM iam_global.lti11_launch_by_session_hash" +
                " WHERE hash = ?" +
                " AND launch_request_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(hash, launchRequestId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LTI11LaunchSessionHash fromRow(final Row row) {
        return new LTI11LaunchSessionHash()
                .setHash(row.getString("hash"))
                .setUserId(row.getString("user_id"))
                .setLaunchRequestId(row.getUUID("launch_request_id"))
                .setCohortId(row.getUUID("cohort_id"))
                .setConfigurationId(row.getUUID("configuration_id"))
                .setContinueTo(row.getString("continue_to"))
                .setStatus(Enums.of(LTI11LaunchSessionHash.Status.class, row.getString("status")));
    }
}
