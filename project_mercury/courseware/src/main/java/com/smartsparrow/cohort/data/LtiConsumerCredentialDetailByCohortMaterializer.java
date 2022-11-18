package com.smartsparrow.cohort.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class LtiConsumerCredentialDetailByCohortMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public LtiConsumerCredentialDetailByCohortMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchByCohortId(UUID cohortId){
        final String SELECT = "SELECT cohort_id, " +
                "key, " +
                "secret, " +
                "created_date, " +
                "log_debug " +
                "FROM cohort.lti_consumer_cred_v11_by_cohort " +
                "WHERE cohort_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchByCohortIdAndKey(UUID cohortId, String key){
        final String SELECT = "SELECT cohort_id, " +
                "key, " +
                "secret, " +
                "created_date, " +
                "log_debug " +
                "FROM cohort.lti_consumer_cred_v11_by_cohort " +
                "WHERE cohort_id = ? AND key = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId, key);
        stmt.setIdempotent(true);
        return stmt;
    }

    public LtiConsumerCredentialDetail fromRow(Row row){
        return new LtiConsumerCredentialDetail()
                .setCohortId(row.getUUID("cohort_id"))
                .setKey(row.getString("key"))
                .setSecret(row.getString("secret"))
                .setCreatedDate(row.getLong("created_date"))
                .setLogDebug(row.getBool("log_debug"));
    }
}
