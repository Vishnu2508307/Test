package com.smartsparrow.cohort.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CohortByLmsCourseMutator extends SimpleTableMutator<UUID> {

    public Statement upsert(String lmsCourseId, UUID cohortId) {
        String UPSERT = "INSERT INTO cohort.cohort_by_lms_course (" +
                "lms_course_id, " +
                "cohort_id) " +
                "VALUES (?,?)";

        BoundStatement stmt = stmtCache.asBoundStatement(UPSERT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(lmsCourseId, cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement remove(String lmsCourseId, UUID cohortId) {
        String REMOVE = "DELETE FROM cohort.cohort_by_lms_course " +
                "WHERE lms_course_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(REMOVE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(lmsCourseId, cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
