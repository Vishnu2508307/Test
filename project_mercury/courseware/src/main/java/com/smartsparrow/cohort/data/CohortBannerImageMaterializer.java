package com.smartsparrow.cohort.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class CohortBannerImageMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    private static final String BY_COHORT = "SELECT cohort_id, " +
            "size, " +
            "banner_image " +
            "FROM cohort.banner_image_by_cohort_size " +
            "WHERE cohort_id = ?";

    @Inject
    public CohortBannerImageMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    @SuppressWarnings("Duplicates")
    public Statement findCohortBannerImage(UUID cohortId) {
        BoundStatement stmt = stmtCache.asBoundStatement(BY_COHORT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement findCohortBannerImage(UUID cohortId, CohortBannerImage.Size size) {

        final String BY_SIZE = BY_COHORT + " AND size=?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_SIZE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId, size.name());
        stmt.setIdempotent(true);
        return stmt;
    }
}
