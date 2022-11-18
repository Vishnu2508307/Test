package com.smartsparrow.cohort.data;

import static java.util.Objects.isNull;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class EnrollmentByAccountMutator extends SimpleTableMutator<CohortEnrollment> {

    @Override
    public String getUpsertQuery(CohortEnrollment mutation) {
        String INSERT = "INSERT INTO cohort_enrollment.enrollment_by_account (" +
                "account_id, " +
                "cohort_id, " +
                "enrollment_date, " +
                "enrolled_at, " +
                "enrollment_type, " +
                "ttl_seconds, " +
                "expires_at, " +
                "enrolled_by, " +
                "pearson_uid" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        if (mutation.getTtlSeconds() != null) {
            return INSERT + " USING TTL ?";
        }
        return INSERT;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void bindUpsert(BoundStatement stmt, CohortEnrollment mutation) {
        stmt.setUUID(0, mutation.getAccountId());
        stmt.setUUID(1, mutation.getCohortId());
        stmt.setUUID(2, mutation.getEnrollmentDate());
        stmt.setString(3, mutation.getEnrolledAt());
        stmt.setString(4, Enums.asString(mutation.getEnrollmentType()));

        if (!isNull(mutation.getTtlSeconds())) {
            stmt.setInt(5, mutation.getTtlSeconds());
        }
        Mutators.bindNonNull(stmt, 6, mutation.getExpiresAt());
        Mutators.bindNonNull(stmt, 7, mutation.getEnrolledBy());
        stmt.setString(8, mutation.getPearsonUid());
    }

    @Override
    public String getDeleteQuery(CohortEnrollment mutation) {
        return "DELETE FROM cohort_enrollment.enrollment_by_account " +
                "WHERE account_id = ? " +
                "AND cohort_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, CohortEnrollment mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getCohortId());
    }
}
