package com.smartsparrow.cohort.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CohortByAccountMutator extends SimpleTableMutator<CohortAccount> {

    @Override
    public String getUpsertQuery(CohortAccount mutation) {
        return "INSERT INTO cohort.cohort_by_account (" +
                "account_id, " +
                "cohort_id) VALUES (?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, CohortAccount mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getCohortId());
    }

    @Override
    public String getDeleteQuery(CohortAccount mutation) {
        return "DELETE FROM cohort.cohort_by_account " +
                "WHERE account_id = ? " +
                "AND cohort_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, CohortAccount mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getCohortId());
    }
}
