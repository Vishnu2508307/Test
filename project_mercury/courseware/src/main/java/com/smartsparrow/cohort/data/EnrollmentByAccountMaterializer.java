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

public class EnrollmentByAccountMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public EnrollmentByAccountMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    private static final String BY_ACCOUNT = "SELECT account_id, " +
            "cohort_id, " +
            "enrollment_date, " +
            "enrolled_at, " +
            "enrollment_type, " +
            "ttl_seconds, " +
            "expires_at, " +
            "enrolled_by, " +
            "pearson_uid " +
            "FROM cohort_enrollment.enrollment_by_account " +
            "WHERE account_id = ?";


    public Statement findEnrollments(UUID accountId) {
        BoundStatement stmt = stmtCache.asBoundStatement(BY_ACCOUNT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.setIdempotent(true);
        stmt.bind(accountId);
        return stmt;
    }

    public Statement findEnrollment(UUID accountId, UUID cohortId) {
        final String BY_ACCOUNT_COHORT = BY_ACCOUNT +
                " AND cohort_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_ACCOUNT_COHORT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(accountId, cohortId);
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
