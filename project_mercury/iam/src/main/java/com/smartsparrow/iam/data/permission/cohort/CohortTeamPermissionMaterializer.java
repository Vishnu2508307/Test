package com.smartsparrow.iam.data.permission.cohort;

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

public class CohortTeamPermissionMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public CohortTeamPermissionMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement fetchPermission(UUID teamId, UUID cohortId) {
        final String BY_COHORT = "SELECT team_id, " +
                "cohort_id, " +
                "permission_level " +
                "FROM iam_global.cohort_permission_by_team " +
                "WHERE team_id = ? " +
                "AND cohort_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_COHORT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(teamId, cohortId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement fetchPermission(UUID teamId) {
        final String BY_TEAM = "SELECT team_id, " +
                "cohort_id, " +
                "permission_level " +
                "FROM iam_global.cohort_permission_by_team " +
                "WHERE team_id = ?";

        BoundStatement stmt = stmtCache.asBoundStatement(BY_TEAM);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(teamId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public TeamCohortPermission fromRow(Row row) {
        return new TeamCohortPermission()
                .setTeamId(row.getUUID("team_id"))
                .setCohortId(row.getUUID("cohort_id"))
                .setPermissionLevel(Enums.of(PermissionLevel.class, row.getString("permission_level")));
    }
}
