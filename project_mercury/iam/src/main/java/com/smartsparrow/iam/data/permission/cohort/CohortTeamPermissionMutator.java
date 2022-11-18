package com.smartsparrow.iam.data.permission.cohort;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class CohortTeamPermissionMutator extends SimpleTableMutator<TeamCohortPermission> {

    @Override
    public String getUpsertQuery(TeamCohortPermission mutation) {
        return "INSERT INTO iam_global.cohort_permission_by_team (" +
                "team_id, " +
                "cohort_id, " +
                "permission_level) VALUES (?,?,?)";
    }

    @Override
    public String getDeleteQuery(TeamCohortPermission mutation) {
        return "DELETE FROM iam_global.cohort_permission_by_team " +
                "WHERE team_id = ? " +
                "AND cohort_id = ?";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, TeamCohortPermission mutation) {
        stmt.bind(mutation.getTeamId(),
                mutation.getCohortId(),
                mutation.getPermissionLevel().name());
    }

    @Override
    public void bindDelete(BoundStatement stmt, TeamCohortPermission mutation) {
        stmt.bind(mutation.getTeamId(),
                mutation.getCohortId());
    }
}
