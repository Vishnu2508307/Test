package com.smartsparrow.iam.data;

import java.util.Set;
import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.google.common.collect.ImmutableSet;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AccountStatus;
import com.smartsparrow.util.Enums;

class AccountMutator extends SimpleTableMutator<Account> {

    @Override
    public String getUpsertQuery(Account mutation) {
        // @formatter:off
        return "INSERT INTO iam_global.account ("
                + "  id"
                + ", historical_id"
                + ", subscription_id"
                + ", iam_region"
                + ", status"
                + ", roles"
                + ", password_hash"
                + ", password_expired"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, Account mutation) {
        stmt.bind(mutation.getId(), mutation.getHistoricalId(), mutation.getSubscriptionId(),
                  mutation.getIamRegion().name(), mutation.getStatus().name(), Enums.asString(mutation.getRoles()),
                  mutation.getPasswordHash(), mutation.getPasswordExpired());
    }

    /*
     * Custom Mutators
     */

    public Statement mutateStatus(UUID id, AccountStatus status) {
        // @formatter:off
        final String QUERY = "UPDATE iam_global.account "
                + "  SET status = ?"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(status.name(), id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement setRoles(UUID id, Set<AccountRole> roles) {
        // @formatter:off
        final String QUERY = "UPDATE iam_global.account "
                + "  SET roles = ?"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(Enums.asString(roles), id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement addRole(UUID id, AccountRole role) {
        // @formatter:off
        final String QUERY = "UPDATE iam_global.account "
                + "  SET roles = roles + ?"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(ImmutableSet.of(role.name()), id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement removeRole(UUID id, AccountRole role) {
        // @formatter:off
        final String QUERY = "UPDATE iam_global.account "
                + "  SET roles = roles - ?"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(ImmutableSet.of(role.name()), id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement setSubscription(UUID id, UUID subscriptionId) {
        // @formatter:off
        final String QUERY = "UPDATE iam_global.account "
                + "  SET subscription_id = ?"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(subscriptionId, id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement setPasswordFields(UUID id, String passwordHash, Boolean passwordExpired, String passwordTemporary) {
        // @formatter:off
        final String QUERY = "UPDATE iam_global.account "
                + "  SET password_hash = ?"
                + ",     password_expired = ?"
                + ",     password_temporary = ?"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(passwordHash, passwordExpired, passwordTemporary, id);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement setHistoricalId(UUID id, Long historicalId) {
        // @formatter:off
        final String QUERY = "UPDATE iam_global.account "
                + "  SET historical_id = ?"
                + " WHERE id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        stmt.bind(historicalId, id);
        stmt.setIdempotent(true);
        return stmt;
    }
}
