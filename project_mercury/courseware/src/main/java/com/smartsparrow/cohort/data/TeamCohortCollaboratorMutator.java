package com.smartsparrow.cohort.data;

import com.datastax.driver.core.BoundStatement;
import com.smartsparrow.dse.api.SimpleTableMutator;

public class TeamCohortCollaboratorMutator extends SimpleTableMutator<TeamCohortCollaborator> {

    @Override
    public String getUpsertQuery(TeamCohortCollaborator mutation) {
        return "INSERT INTO cohort.team_by_cohort (" +
                "cohort_id, " +
                "team_id, " +
                "permission_level) VALUES (?,?,?)";
    }

    @Override
    public void bindUpsert(BoundStatement stmt, TeamCohortCollaborator mutation) {
        stmt.bind(mutation.getCohortId(), mutation.getTeamId(), mutation.getPermissionLevel().name());
    }

    @Override
    public String getDeleteQuery(TeamCohortCollaborator mutation) {
        return "DELETE FROM cohort.team_by_cohort " +
                "WHERE cohort_id = ? " +
                "AND team_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, TeamCohortCollaborator mutation) {
        stmt.bind(mutation.getCohortId(), mutation.getTeamId());
    }
}
