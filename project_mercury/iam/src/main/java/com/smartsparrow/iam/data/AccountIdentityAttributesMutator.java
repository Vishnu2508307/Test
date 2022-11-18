package com.smartsparrow.iam.data;

import java.util.UUID;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.google.common.collect.ImmutableSet;
import com.smartsparrow.dse.api.SimpleTableMutator;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.Region;

class AccountIdentityAttributesMutator extends SimpleTableMutator<AccountIdentityAttributes> {

    @Override
    public String getUpsertQuery(AccountIdentityAttributes mutation) {
        // @formatter:off
        return "INSERT INTO " + RegionKeyspace.map(mutation.getIamRegion(), "account_identity_attribute") + " ("
                + "  account_id"
                + ", iam_region"
                + ", subscription_id"
                + ", given_name"
                + ", family_name"
                + ", honorific_prefix"
                + ", honorific_suffix"
                + ", email"
                + ", primary_email"
                + ", affiliation"
                + ", job_title"
                + ") VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        // @formatter:on
    }

    @Override
    public void bindUpsert(BoundStatement stmt, AccountIdentityAttributes mutation) {
        stmt.bind(mutation.getAccountId(), mutation.getIamRegion().name(), mutation.getSubscriptionId(),
                  mutation.getGivenName(), mutation.getFamilyName(), mutation.getHonorificPrefix(),
                  mutation.getHonorificSuffix(), mutation.getEmail(), mutation.getPrimaryEmail(),
                  mutation.getAffiliation(), mutation.getJobTitle());
    }

    @Override
    public ConsistencyLevel upsertConsistencyLevel() {
        return isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM;
    }

    @Override
    public String getDeleteQuery(AccountIdentityAttributes mutation) {
        return "DELETE FROM " + RegionKeyspace.map(mutation.getIamRegion(), "account_identity_attribute")
                + " WHERE account_id = ?";
    }

    @Override
    public void bindDelete(BoundStatement stmt, AccountIdentityAttributes mutation) {
        stmt.bind(mutation.getAccountId());
    }

    @Override
    public ConsistencyLevel deleteConsistencyLevel() {
        return isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM;
    }

    public Statement setSubscription(Region region, UUID accountId, UUID subscriptionId) {
        // @formatter:off
        final String QUERY = "UPDATE " + RegionKeyspace.map(region, "account_identity_attribute")
                + "  SET subscription_id = ?"
                + " WHERE account_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(subscriptionId, accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement setPrimaryEmail(Region region, UUID accountId, String email) {
        // @formatter:off
        final String QUERY = "UPDATE " + RegionKeyspace.map(region, "account_identity_attribute")
                + "  SET primary_email = ?"
                + " WHERE account_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(email, accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement removeEmails(Region region, UUID accountId, String... emails) {
        // @formatter:off
        final String QUERY = "UPDATE " + RegionKeyspace.map(region, "account_identity_attribute")
                + "  SET email = email - ?"
                + " WHERE account_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(ImmutableSet.copyOf(emails), accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement addEmails(Region region, UUID accountId, String... emails) {
        // @formatter:off
        final String QUERY = "UPDATE " + RegionKeyspace.map(region, "account_identity_attribute")
                + "  SET email = email + ? "
                + " WHERE account_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(ImmutableSet.copyOf(emails), accountId);
        stmt.setIdempotent(true);
        return stmt;
    }

    public Statement setIdentityNames(Region region,
            UUID id,
            String honorificPrefix,
            String givenName,
            String familyName,
            String honorificSuffix) {
        // @formatter:off
        final String QUERY = "UPDATE " + RegionKeyspace.map(region, "account_identity_attribute")
                + "  SET honorific_prefix = ?"
                + ",     given_name = ?"
                + ",     family_name = ?"
                + ",     honorific_suffix = ?"
                + " WHERE account_id = ?";
        // @formatter:on

        BoundStatement stmt = stmtCache.asBoundStatement(QUERY);
        stmt.setConsistencyLevel(isForceLocalCL() ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM);
        stmt.bind(honorificPrefix, givenName, familyName, honorificSuffix, id);
        stmt.setIdempotent(true);
        return stmt;
    }

}
