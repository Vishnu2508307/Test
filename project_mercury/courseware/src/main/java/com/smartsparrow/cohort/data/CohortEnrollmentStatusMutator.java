package com.smartsparrow.cohort.data;

import static java.util.Objects.isNull;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class CohortEnrollmentStatusMutator extends SimpleTableMutator<CohortEnrollment> {

    @Override
    public String getUpsertQuery(CohortEnrollment mutation) {
        return "INSERT INTO cohort_enrollment.enrollment_status_by_cohort (" +
                "cohort_id" +
                ", account_id" +
                ", enrollment_date" +
                ", enrolled_at" +
                ", enrollment_type" +
                ", ttl_seconds" +
                ", expires_at" +
                ", enrolled_by" +
                ", pearson_uid" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void bindUpsert(BoundStatement stmt, CohortEnrollment mutation) {
        stmt.setUUID(0, mutation.getCohortId());
        stmt.setUUID(1, mutation.getAccountId());
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
}
