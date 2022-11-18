package com.smartsparrow.cohort.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class CohortByLmsCourseMaterializer  implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CohortByLmsCourseMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findCohort(String lmsCourseId) {
        final String SELECT = "SELECT " +
                "cohort_id " +
                "FROM cohort.cohort_by_lms_course " +
                "WHERE lms_course_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(lmsCourseId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public UUID fromRow(Row row) {
        return row.getUUID("cohort_id");
    }
}
