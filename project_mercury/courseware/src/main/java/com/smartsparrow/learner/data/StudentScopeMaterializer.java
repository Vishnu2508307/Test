package com.smartsparrow.learner.data;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.PreparedStatementCache;
import com.smartsparrow.dse.api.TableMaterializer;

public class StudentScopeMaterializer implements TableMaterializer {

    private final PreparedStatementCache stmtCache;

    @Inject
    public StudentScopeMaterializer(PreparedStatementCache stmtCache) {
        this.stmtCache = stmtCache;
    }

    public Statement findLatest(UUID deploymentId, UUID accountId, UUID scopeURN) {
        final String SELECT = "SELECT " +
                "deployment_id, " +
                "account_id, " +
                "scope_urn, " +
                "id " +
                "FROM learner.student_scope " +
                "WHERE deployment_id = ? " +
                "AND account_id = ? " +
                "AND scope_urn = ? " +
                "LIMIT 1";

        BoundStatement stmt = stmtCache.asBoundStatement(SELECT);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(deploymentId, accountId, scopeURN);
        stmt.setIdempotent(true);
        return stmt;
    }

    public StudentScope fromRow(Row row) {
        return new StudentScope()
                .setDeploymentId(row.getUUID("deployment_id"))
                .setAccountId(row.getUUID("account_id"))
                .setScopeUrn(row.getUUID("scope_urn"))
                .setId(row.getUUID("id"));
    }
}
