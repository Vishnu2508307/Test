package com.smartsparrow.cohort.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class CohortSettingsMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CohortSettingsMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchByCohort(UUID cohortId) {
        String BY_COHORT = "SELECT "
                + "  cohort_id"
                + ", banner_pattern"
                + ", color"
                + ", banner_image"
                + ", payment_methods_allowed"
                + ", cost_currency_code"
                + ", cost_amount_in_cents"
                + ", product_id"
                + ", learner_redirect_id"
                + " FROM cohort.settings_by_cohort "
                + " WHERE cohort_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_COHORT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }
}
