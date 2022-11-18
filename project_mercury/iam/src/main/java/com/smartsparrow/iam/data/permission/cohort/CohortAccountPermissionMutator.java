package com.smartsparrow.iam.data.permission.cohort;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CohortAccountPermissionMutator extends SimpleTableMutator<AccountCohortPermission> {

    @Override
    public String getUpsertQuery(AccountCohortPermission mutation) {
        return "INSERT INTO iam_global.cohort_permission_by_account (" +
                "account_id, " +
                "cohort_id, " +
                "permission_level) VALUES (?,?,?)";
    }

    @Override
    public String getDeleteQuery(AccountCohortPermission mutation) {
        return "DELETE FROM iam_global.cohort_permission_by_account " +
                "WHERE account_id = ? " +
                "AND cohort_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccountCohortPermission mutation) {
        stmt.bind(mutation.getAccountId(),
                mutation.getCohortId(),
                mutation.getPermissionLevel().name());
    }

    @Override
    public void bindDelete(BoundStatement stmt, AccountCohortPermission mutation) {
        stmt.bind(mutation.getAccountId(),
                mutation.getCohortId());
    }
}
