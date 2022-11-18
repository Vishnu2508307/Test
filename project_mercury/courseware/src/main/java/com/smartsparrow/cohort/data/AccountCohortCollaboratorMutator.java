package com.smartsparrow.cohort.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class AccountCohortCollaboratorMutator extends SimpleTableMutator<AccountCohortCollaborator> {

    @Override
    public String getUpsertQuery(AccountCohortCollaborator mutation) {
        return "INSERT INTO cohort.account_by_cohort (" +
                "cohort_id, " +
                "account_id, " +
                "permission_level) VALUES (?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccountCohortCollaborator mutation) {
        stmt.bind(mutation.getCohortId(),
                mutation.getAccountId(),
                mutation.getPermissionLevel().name());
    }

    @Override
    public String getDeleteQuery(AccountCohortCollaborator mutation) {
        return "DELETE FROM cohort.account_by_cohort " +
                "WHERE cohort_id = ? " +
                "AND account_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, AccountCohortCollaborator mutation) {
        stmt.bind(mutation.getCohortId(),
                mutation.getAccountId());
    }
}
