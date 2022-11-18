package com.smartsparrow.cohort.data;

import static com.smartsparrow.dse.api.ResultSets.getNullableEnum;
import static com.smartsparrow.dse.api.ResultSets.getNullableInteger;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.util.Enums;

public class CohortEnrollmentStatusMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CohortEnrollmentStatusMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findLatest(UUID cohortId, UUID accountId) {
        final String SELECT = "SELECT" +
                " cohort_id" +
                ", account_id " +
                ", enrollment_date" +
                ", enrolled_at" +
                ", enrollment_type" +
                ", ttl_seconds" +
                ", expires_at" +
                ", enrolled_by" +
                ", pearson_uid" +
                " FROM cohort_enrollment.enrollment_status_by_cohort" +
                " WHERE cohort_id = ?" +
                " AND account_id = ?" +
                " LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId, accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findEnrollments(UUID cohortId, UUID accountId) {
        final String SELECT = "SELECT" +
                " cohort_id" +
                ", account_id " +
                ", enrollment_date" +
                ", enrolled_at" +
                ", enrollment_type" +
                ", ttl_seconds" +
                ", expires_at" +
                ", enrolled_by" +
                ", pearson_uid" +
                " FROM cohort_enrollment.enrollment_status_by_cohort" +
                " WHERE cohort_id = ?" +
                " AND account_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId, accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public CohortEnrollment fromRow(Row row) {
        return new CohortEnrollment()
                .setCohortId(row.getUUID("cohort_id"))
                .setAccountId(row.getUUID("account_id"))
                .setEnrollmentDate(row.getUUID("enrollment_date"))
                .setEnrolledAt(row.getString("enrolled_at"))
                .setEnrollmentType(Enums.of(EnrollmentType.class, row.getString("enrollment_type")))
                .setExpiresAt(row.getString("expires_at"))
                .setEnrolledBy(row.getUUID("enrolled_by"))
                .setTtlSeconds(getNullableInteger(row, "ttl_seconds"))
                .setPearsonUid(row.getString("pearson_uid"));
    }
}
