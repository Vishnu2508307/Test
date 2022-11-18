package com.smartsparrow.cohort.data;

import static com.smartsparrow.dse.api.Mutators.bindNonNull;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CohortSettingsMutator extends SimpleTableMutator<CohortSettings> {

    @Override
    public String getUpsertQuery(CohortSettings mutation) {
        return "INSERT INTO cohort.settings_by_cohort ("
                + "  cohort_id"
                + ", banner_pattern"
                + ", color"
                + ", banner_image"
                + ", payment_methods_allowed"
                + ", cost_currency_code"
                + ", cost_amount_in_cents"
                + ", product_id"
                + ") VALUES (?,?,?,?,?,?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CohortSettings mutation) {
        stmt.setUUID(0, mutation.getCohortId());

        bindNonNull(stmt, 1, mutation.getBannerPattern());
        bindNonNull(stmt, 2, mutation.getColor());
        bindNonNull(stmt, 3, mutation.getBannerImage());
        // skip index 4 which is the payment method. This column will be dropped soon
        // skip index 5 which is the getCostCurrencyCode. This column will be dropped soon
        // skip index 6 which is the getCostAmountInCents. This column will be dropped soon
        bindNonNull(stmt, 7, mutation.getProductId());
    }

    public Statement update(CohortSettings mutation) {
        final String UPDATE = "UPDATE cohort.settings_by_cohort "
                + "SET banner_pattern = ?"
                + "    , color = ?"
                + "    , banner_image = ?"
                + "    , payment_methods_allowed = ?"
                + "    , cost_currency_code = ?"
                + "    , cost_amount_in_cents = ?"
                + "    , product_id = ?"
                + "WHERE cohort_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(UPDATE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(mutation.getBannerPattern(),  //
                  mutation.getColor(),  //
                  mutation.getBannerImage(), //
                  null, // paymentMethod will be removed
                  null, // getCostCurrencyCode() will be removed, //
                  null, // getCostAmountInCents() will be removed, //
                  mutation.getProductId(), //
                  mutation.getCohortId());
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement updateLearnerRedirectId(final UUID cohortId, final UUID learnerRedirectId) {
        final String UPDATE = "UPDATE cohort.settings_by_cohort "
                + "SET learner_redirect_id = ?"
                + "WHERE cohort_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(UPDATE);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(learnerRedirectId, cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }

    @Override
    public String getDeleteQuery(CohortSettings mutation) {
        return "DELETE FROM cohort.settings_by_cohort " +
                "WHERE cohort_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, CohortSettings mutation) {
        stmt.bind(mutation.getCohortId());
    }
}
