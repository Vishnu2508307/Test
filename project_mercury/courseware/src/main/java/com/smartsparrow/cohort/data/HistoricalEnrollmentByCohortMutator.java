package com.smartsparrow.cohort.data;

import static java.util.Objects.isNull;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.util.Enums;

public class HistoricalEnrollmentByCohortMutator extends SimpleTableMutator<CohortEnrollment> {

    @Override
    public String getUpsertQuery(final CohortEnrollment mutation) {
        return "INSERT INTO cohort_enrollment.latest_enrollment_by_cohort_account (" +
                " cohort_id" +
                ", account_id" +
                ", enrolled_at" +
                ", enrolled_by" +
                ", enrollment_date" +
                ", enrollment_type" +
                ", expires_at" +
                ", ttl_seconds" +
                ", pearson_uid" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void bindUpsert(final BoundStatement stmt, final CohortEnrollment mutation) {
        stmt.setUUID(0, mutation.getCohortId());
        stmt.setUUID(1, mutation.getAccountId());
        stmt.setString(2, mutation.getEnrolledAt());
        Mutators.bindNonNull(stmt, 3, mutation.getEnrolledBy());
        stmt.setUUID(4, mutation.getEnrollmentDate());
        stmt.setString(5, Enums.asString(mutation.getEnrollmentType()));
        Mutators.bindNonNull(stmt, 6, mutation.getExpiresAt());
        Mutators.bindNonNull(stmt, 7, mutation.getTtlSeconds());
        stmt.setString(8, mutation.getPearsonUid());
    }
}
