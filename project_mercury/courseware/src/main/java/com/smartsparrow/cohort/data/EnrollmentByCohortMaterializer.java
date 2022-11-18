package com.smartsparrow.cohort.data;

import static com.smartsparrow.dse.api.ResultSets.getNullableEnum;
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

public class EnrollmentByCohortMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public EnrollmentByCohortMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findEnrollments(UUID cohortId) {
        final String BY_COHORT = "SELECT cohort_id, " +
                "account_id, " +
                "enrollment_date, " +
                "enrolled_at, " +
                "enrollment_type, " +
                "ttl_seconds, " +
                "expires_at, " +
                "enrolled_by, " +
                "pearson_uid " +
                "FROM cohort_enrollment.enrollment_by_cohort " +
                "WHERE cohort_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_COHORT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findEnrolledAccountIds(UUID cohortId) {
        final String SELECT = "SELECT account_id" +
                " FROM cohort_enrollment.enrollment_by_cohort " +
                " WHERE cohort_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }

    @SuppressWarnings("Duplicates")
    public CohortEnrollment fromRow(Row row) {
        CohortEnrollment cohortEnrollment = new CohortEnrollment()
                .setCohortId(row.getUUID("cohort_id"))
                .setAccountId(row.getUUID("account_id"))
                .setEnrollmentDate(row.getUUID("enrollment_date"))
                .setEnrollmentType(Enums.of(EnrollmentType.class, row.getString("enrollment_type")))
                .setExpiresAt(row.getString("expires_at"))
                .setEnrolledBy(row.getUUID("enrolled_by"))
                .setTtlSeconds(getNullableInteger(row, "ttl_seconds"))
                .setEnrolledAt(row.getString("enrolled_at"))
                .setPearsonUid(row.getString("pearson_uid"));

        if (Strings.isNullOrEmpty(cohortEnrollment.getEnrolledAt())) {
            // backward compatibility, TODO remove once backfill done
            cohortEnrollment.setEnrolledAt(DateFormat.asRFC1123(cohortEnrollment.getEnrollmentDate()));
        }

        return cohortEnrollment;
    }
}
