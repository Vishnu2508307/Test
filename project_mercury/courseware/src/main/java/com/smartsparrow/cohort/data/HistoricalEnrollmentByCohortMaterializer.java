package com.smartsparrow.cohort.data;

import static com.smartsparrow.dse.api.ResultSets.getNullableInteger;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.google.common.base.Strings;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.Enums;

public class HistoricalEnrollmentByCohortMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    private static final String SELECT_BY_COHORT = "SELECT" +
            " cohort_id" +
            ", account_id" +
            ", enrolled_at" +
            ", enrolled_by" +
            ", enrollment_date" +
            ", enrollment_type" +
            ", expires_at" +
            ", ttl_seconds" +
            ", pearson_uid" +
            " FROM cohort_enrollment.latest_enrollment_by_cohort_account" +
            " WHERE cohort_id = ?";

    private static final String SELECT_BY_ACCOUNT = SELECT_BY_COHORT + " AND account_id = ?";
    
    @Inject
    public HistoricalEnrollmentByCohortMaterializer(final PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findEnrollments(final UUID cohortId) {
        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_BY_COHORT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findEnrollment(final UUID cohortId, final UUID accountId) {
        BoundStatement stmt = stmtCache.asBoundStatement(SELECT_BY_ACCOUNT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId, accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public HistoricalCohortEnrollment fromRow(Row row) {
        HistoricalCohortEnrollment historicalCohortEnrollment = new HistoricalCohortEnrollment()
                .setCohortId(row.getUUID("cohort_id"))
                .setAccountId(row.getUUID("account_id"))
                .setEnrolledAt(row.getString("enrolled_at"))
                .setEnrolledBy(row.getUUID("enrolled_by"))
                .setEnrollmentDate(row.getUUID("enrollment_date"))
                .setEnrollmentType(Enums.of(EnrollmentType.class, row.getString("enrollment_type")))
                .setExpiresAt(row.getString("expires_at"))
                .setTtlSeconds(getNullableInteger(row, "ttl_seconds"))
                .setPearsonUid(row.getString("pearson_uid"));

        if (Strings.isNullOrEmpty(historicalCohortEnrollment.getEnrolledAt())) {
            // backward compatibility, TODO remove once backfill done
            historicalCohortEnrollment.setEnrolledAt(DateFormat.asRFC1123(historicalCohortEnrollment.getEnrollmentDate()));
        }

        return historicalCohortEnrollment;
    }
}
