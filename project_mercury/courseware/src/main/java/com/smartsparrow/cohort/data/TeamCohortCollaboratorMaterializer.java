package com.smartsparrow.cohort.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.util.Enums;

public class TeamCohortCollaboratorMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public TeamCohortCollaboratorMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findTeams(UUID cohortId) {
        final String BY_COHORT = "SELECT cohort_id, " +
                "team_id, " +
                "permission_level " +
                "FROM cohort.team_by_cohort " +
                "WHERE cohort_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_COHORT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public TeamCohortCollaborator fromRow(Row row) {
        return new TeamCohortCollaborator()
                .setTeamId(row.getUUID("team_id"))
                .setCohortId(row.getUUID("cohort_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
