package com.smartsparrow.iam.data;

import static com.smartsparrow.dse.api.ResultSets.getNullableEnum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.map.HashedMap;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.dse.api.TableMutator;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAction;
import com.smartsparrow.iam.service.AccountHash;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AccountLogEntry;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AccountShadowAttribute;
import com.smartsparrow.iam.service.AccountShadowAttributeName;
import com.smartsparrow.iam.service.AccountShadowAttributeSource;
import com.smartsparrow.iam.service.AccountStatus;
import com.smartsparrow.iam.service.Region;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

/**
 *
 * TODO: many of these queries could shortcut a service lookup of the account, followed by identity.
 * TODO:    if needed, denormalize additional Account attributes and create additional gateway operations
 * TODO:    that will allow a direct load of all Account+Identity in a single query using region. (repeat as needed)
 *
 */
@Singleton
public class AccountGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AccountGateway.class);

    //
    private final Session session;

    // mutators
    private final AccountActionMutator accountActionMutator;
    private final AccountByHashMutator accountByHashMutator;
    private final AccountByHistoricalIdMutator accountByHistoricalIdMutator;
    private final AccountBySubscriptionMutator accountBySubscriptionMutator;
    private final AccountIdentityAttributesMutator accountIdentityAttributesMutator;
    private final AccountLogEntryMutator accountLogEntryMutator;
    private final AccountMutator accountMutator;
    private final AccountShadowAttributeMutator accountShadowAttributeMutator;

    // materializers
    private final AccountActionMaterializer accountActionMaterializer;
    private final AccountByHashMaterializer accountByHashMaterializer;
    private final AccountByHistoricalIdMaterializer accountByHistoricalIdMaterializer;
    private final AccountBySubscriptionMaterializer accountBySubscriptionMaterializer;
    private final AccountIdentityAttributesMaterializer accountIdentityAttributesMaterializer;
    private final AccountLogEventMaterializer accountLogEventMaterializer;
    private final AccountMaterializer accountMaterializer;
    private final AccountShadowAttributeMaterializer accountShadowAttributeMaterializer;

    // collector mutators.
    private final Collection<TableMutator<Account>> accountMutators = new ArrayList<>();

    @Inject
    public AccountGateway(Session session, AccountActionMutator accountActionMutator,
            AccountByHashMutator accountByHashMutator, AccountByHistoricalIdMutator accountByHistoricalIdMutator,
            AccountBySubscriptionMutator accountBySubscriptionMutator,
            AccountIdentityAttributesMutator accountIdentityAttributesMutator,
            AccountLogEntryMutator accountLogEntryMutator, AccountMutator accountMutator,
            AccountShadowAttributeMutator accountShadowAttributeMutator,
            AccountActionMaterializer accountActionMaterializer, AccountByHashMaterializer accountByHashMaterializer,
            AccountByHistoricalIdMaterializer accountByHistoricalIdMaterializer,
            AccountBySubscriptionMaterializer accountBySubscriptionMaterializer,
            AccountIdentityAttributesMaterializer accountIdentityAttributesMaterializer,
            AccountLogEventMaterializer accountLogEventMaterializer, AccountMaterializer accountMaterializer,
            AccountShadowAttributeMaterializer accountShadowAttributeMaterializer) {
        this.session = session;
        this.accountActionMutator = accountActionMutator;
        this.accountByHashMutator = accountByHashMutator;
        this.accountByHistoricalIdMutator = accountByHistoricalIdMutator;
        this.accountBySubscriptionMutator = accountBySubscriptionMutator;
        this.accountIdentityAttributesMutator = accountIdentityAttributesMutator;
        this.accountLogEntryMutator = accountLogEntryMutator;
        this.accountMutator = accountMutator;
        this.accountShadowAttributeMutator = accountShadowAttributeMutator;
        this.accountActionMaterializer = accountActionMaterializer;
        this.accountByHashMaterializer = accountByHashMaterializer;
        this.accountByHistoricalIdMaterializer = accountByHistoricalIdMaterializer;
        this.accountBySubscriptionMaterializer = accountBySubscriptionMaterializer;
        this.accountIdentityAttributesMaterializer = accountIdentityAttributesMaterializer;
        this.accountLogEventMaterializer = accountLogEventMaterializer;
        this.accountMaterializer = accountMaterializer;
        this.accountShadowAttributeMaterializer = accountShadowAttributeMaterializer;

        accountMutators.add(accountMutator);
        accountMutators.add(accountBySubscriptionMutator);
    }

    /**
     * Change the names on the account.
     *
     * @param region the data region
     * @param accountId the account id
     * @param honorificPrefix the prefix, e.g. Dr., Prof., Ms., etc.
     * @param givenName the account's given name
     * @param familyName the account's family name
     * @param honorificSuffix the suffix, e.g. Jr., III.
     */
    @Deprecated
    public void setIdentityNamesBlocking(final Region region, final UUID accountId, final String honorificPrefix, final String givenName,
                                         final String familyName, final String honorificSuffix) {
        Mutators.executeBlocking(session, accountIdentityAttributesMutator
                .setIdentityNames(region, accountId, honorificPrefix, givenName, familyName, honorificSuffix));
    }

    /**
     * Change the names on the account.
     *
     * @param region the data region
     * @param accountId the account id
     * @param honorificPrefix the prefix, e.g. Dr., Prof., Ms., etc.
     * @param givenName the account's given name
     * @param familyName the account's family name
     * @param honorificSuffix the suffix, e.g. Jr., III.
     */
    public Flux<Void> setIdentityNames(final Region region, final UUID accountId, final String honorificPrefix, final String givenName,
            String familyName, String honorificSuffix) {
        return Mutators.execute(session, Flux.just(accountIdentityAttributesMutator
                .setIdentityNames(region, accountId, honorificPrefix, givenName, familyName, honorificSuffix)))
                .doOnEach(log.reactiveErrorThrowable("error while setting identity names", throwable -> new HashedMap<String, Object>() {
                    {
                        put("region", region.name());
                        put("accountId", accountId);
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Change the status on an account.
     *
     * @param accountId the account id to apply the new status to.
     * @param status the status of the account.
     */
    @Deprecated
    public void mutateStatusBlocking(final UUID accountId, final AccountStatus status) {
        Mutators.executeBlocking(session, accountMutator.mutateStatus(accountId, status));
    }

    /**
     * Change the status on an account.
     *
     * @param accountId the account id to apply the new status to.
     * @param status the status of the account.
     */
    public Flux<Void> mutateStatus(final UUID accountId, final AccountStatus status) {
        return Mutators.execute(session, Flux.just(accountMutator.mutateStatus(accountId, status)))
                .doOnEach(log.reactiveErrorThrowable("error while changing status on account", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Set the roles on the supplied account
     *
     * @param accountId the account id
     * @param roles the roles to set
     */
    @Deprecated
    public void setRolesBlocking(final UUID accountId, final Set<AccountRole> roles) {
        Mutators.executeBlocking(session, accountMutator.setRoles(accountId, roles));
    }

    /**
     * Set the roles on the supplied account
     *
     * @param accountId the account id
     * @param roles the roles to set
     */
    public Flux<Void> setRoles(final UUID accountId, final Set<AccountRole> roles) {
        return Mutators.execute(session, Flux.just(accountMutator.setRoles(accountId, roles)))
                .doOnEach(log.reactiveErrorThrowable("error while setting roles for account", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Add a role to an account
     *
     * @param accountId the account id
     * @param role the role to add
     */
    @Deprecated
    public void addRoleBlocking(final UUID accountId, final AccountRole role) {
        Mutators.executeBlocking(session, accountMutator.addRole(accountId, role));
    }

    /**
     * Add a role to an account
     *
     * @param accountId the account id
     * @param role the role to add
     */
    public Flux<Void> addRole(final UUID accountId, final AccountRole role) {
        return Mutators.execute(session, Flux.just(accountMutator.addRole(accountId, role)))
                .doOnEach(log.reactiveErrorThrowable("error while adding role to account", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Remove a role from an account
     *
     * @param accountId the account id
     * @param role the role to remove
     */
    @Deprecated
    public void removeRoleBlocking(final UUID accountId, final AccountRole role) {
        Mutators.executeBlocking(session, accountMutator.removeRole(accountId, role));
    }

    /**
     * Remove a role from an account
     *
     * @param accountId the account id
     * @param role the role to remove
     */
    public Flux<Void> removeRole(final UUID accountId, final AccountRole role) {
        return Mutators.execute(session, Flux.just(accountMutator.removeRole(accountId, role)))
                .doOnEach(log.reactiveErrorThrowable("error while removing role from account", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Set the primary email field on an account identity attribute
     *
     * @param region the data region
     * @param accountId  the account id
     * @param emailAddress the new email address
     */
    @Deprecated
    public void setPrimaryEmailBlocking(final Region region, final UUID accountId, final String emailAddress) {
        Mutators.executeBlocking(session,
                accountIdentityAttributesMutator.setPrimaryEmail(region, accountId, emailAddress));
    }

    /**
     * Set the primary email field on an account identity attribute
     *
     * @param region the data region
     * @param accountId  the account id
     * @param emailAddress the new email address
     */
    public Flux<Void> setPrimaryEmail(final Region region, final UUID accountId, final String emailAddress) {
        return Mutators.execute(session,
                Flux.just(accountIdentityAttributesMutator.setPrimaryEmail(region, accountId, emailAddress)))
                .doOnEach(log.reactiveErrorThrowable("error while setting the primary email", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                        put("region", region.name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Add an email address to an account
     *
     * @param region the data region
     * @param accountId the account id
     * @param emailAddress the new email address
     */
    @Deprecated
    public void addEmailBlocking(final Region region, final UUID accountId, final String emailAddress) {
        Mutators.executeBlocking(session, accountIdentityAttributesMutator.addEmails(region, accountId, emailAddress));
    }

    /**
     * Add an email address to an account
     *
     * @param region the data region
     * @param accountId the account id
     * @param emailAddress the new email address
     */
    public Flux<Void> addEmail(final Region region, final UUID accountId, final String emailAddress) {
        return Mutators.execute(session,
                Flux.just(accountIdentityAttributesMutator.addEmails(region, accountId, emailAddress)))
                .doOnEach(log.reactiveErrorThrowable("error while adding email address", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                        put("region", region.name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Remove an email address from an account identity attribute
     *
     * @param region the data region
     * @param accountId the account id
     * @param emailAddress the email address to remove
     */
    @Deprecated
    public void removeEmailBlocking(final Region region, final UUID accountId, final String emailAddress) {
        Mutators.executeBlocking(session,
                accountIdentityAttributesMutator.removeEmails(region, accountId, emailAddress));
    }

    /**
     * Remove an email address from an account identity attribute
     *
     * @param region the data region
     * @param accountId the account id
     * @param emailAddress the email address to remove
     */
    public Flux<Void> removeEmail(final Region region, final UUID accountId, final String emailAddress) {
        return Mutators.execute(session,
                Flux.just(accountIdentityAttributesMutator.removeEmails(region, accountId, emailAddress)))
                .doOnEach(log.reactiveErrorThrowable("error while removing an email address", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                        put("region", region.name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /*
     *
     *  Persisters!
     *
     */

    /**
     * Persist an Account
     *
     * @param account the account to persist.
     */
    @Deprecated
    public void persistBlocking(final Account account) {
        Iterable<? extends Statement> iter = Mutators.upsertAsIterable(accountMutators, account);
        Mutators.executeBlocking(session, iter);
    }

    /**
     * Persist an Account
     *
     * @param account the account to persist.
     */
    public Flux<Void> persist(final Account account) {
        Flux<? extends Statement> iter = Mutators.upsert(accountMutators, account);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving account", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", account.getId());
                        put("region", account.getIamRegion().name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist the Account Identity Attributes
     *
     * @param accountIdentityAttributes the object to persist.
     */
    @Deprecated
    public void persistBlocking(final AccountIdentityAttributes accountIdentityAttributes) {
        Mutators.executeBlocking(session, accountIdentityAttributesMutator.upsert(accountIdentityAttributes));
    }

    /**
     * Persist the Account Identity Attributes
     *
     * @param accountIdentityAttributes the object to persist.
     */
    public Flux<Void> persist(final AccountIdentityAttributes accountIdentityAttributes) {
        return Mutators.execute(session, Flux.just(accountIdentityAttributesMutator.upsert(accountIdentityAttributes)))
                .doOnEach(log.reactiveErrorThrowable("error while saving identity attributes", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountIdentityAttributes.getAccountId());
                        put("region", accountIdentityAttributes.getIamRegion().name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete the Account Identity Attributes (in the region specified in the object)
     *
     * @param accountIdentityAttributes the object to persist.
     */
    @Deprecated
    public void deleteBlocking(final AccountIdentityAttributes accountIdentityAttributes) {
        Mutators.executeBlocking(session, accountIdentityAttributesMutator.delete(accountIdentityAttributes));
    }

    /**
     * Delete the Account Identity Attributes (in the region specified in the object)
     *
     * @param accountIdentityAttributes the object to persist.
     */
    public Flux<Void> delete(final AccountIdentityAttributes accountIdentityAttributes) {
        return Mutators.execute(session, Flux.just(accountIdentityAttributesMutator.delete(accountIdentityAttributes)))
                .doOnEach(log.reactiveErrorThrowable("error while deleting identity attributes", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountIdentityAttributes.getAccountId());
                        put("region", accountIdentityAttributes.getIamRegion().name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist the Account Hash
     *
     * @param accountHash the account hash to persist
     */
    @Deprecated
    public void persistBlocking(final AccountHash accountHash) {
        Mutators.executeBlocking(session, accountByHashMutator.upsert(accountHash));
    }

    /**
     * Persist the Account Hash
     *
     * @param accountHash the account hash to persist
     */
    public Flux<Void> persist(final AccountHash accountHash) {
        return Mutators.execute(session, Flux.just(accountByHashMutator.upsert(accountHash)))
                .doOnEach(log.reactiveErrorThrowable("error while saving account", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountHash.getAccountId());
                        put("region", accountHash.getIamRegion().name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }


    /**
     * Delete the Account Hash
     *
     * @param accountHash the account hash to persist
     */
    @Deprecated
    public void deleteBlocking(final AccountHash accountHash) {
        Mutators.executeBlocking(session, accountByHashMutator.delete(accountHash));
    }

    /**
     * Delete the Account Hash
     *
     * @param accountHash the account hash to persist
     */
    public Flux<Void> delete(final AccountHash accountHash) {
        return Mutators.execute(session, Flux.just(accountByHashMutator.delete(accountHash)))
                .doOnEach(log.reactiveErrorThrowable("error while deleting account", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountHash.getAccountId());
                        put("region", accountHash.getIamRegion().name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist an Account Action
     *
     * @param action the action to persist
     */
    @Deprecated
    public void persistBlocking(final AccountAction action) {
        Mutators.executeBlocking(session, accountActionMutator.upsert(action));
    }

    /**
     * Persist an Account Action
     *
     * @param action the action to persist
     */
    public Flux<Void> persist(final AccountAction action) {
        return Mutators.execute(session, Flux.just(accountActionMutator.upsert(action)))
                .doOnEach(log.reactiveErrorThrowable("error while saving account action", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", action.getAccountId());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist an Account Log Entry
     *
     * @param logEntry the log entry to persist
     */
    @Deprecated
    public void persistBlocking(final AccountLogEntry logEntry) {
        Mutators.executeBlocking(session, accountLogEntryMutator.upsert(logEntry));
    }

    /**
     * Persist an Account Log Entry
     *
     * @param logEntry the log entry to persist
     */
    @Trace(async = true)
    public Flux<Void> persist(final AccountLogEntry logEntry) {
        return Mutators.execute(session, Flux.just(accountLogEntryMutator.upsert(logEntry)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(log.reactiveErrorThrowable("error while saving account log entry", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", logEntry.getAccountId());
                        put("region", logEntry.getIamRegion().name());
                    }
                }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete all the log entries for the specified account in the provided region.
     *
     * @param region the region
     * @param accountId the account id
     */
    @Deprecated
    public void deleteAllLogEntriesBlocking(final Region region, final UUID accountId) {
        Mutators.executeBlocking(session, accountLogEntryMutator.deleteAll(region, accountId));
    }

    /**
     * Delete all the log entries for the specified account in the provided region.
     *
     * @param region the region
     * @param accountId the account id
     */
    public Flux<Void> deleteAllLogEntries(final Region region, final UUID accountId) {
        return Mutators.execute(session, Flux.just(accountLogEntryMutator.deleteAll(region, accountId)))
                .doOnEach(log.reactiveErrorThrowable("error while deleting all log entries for account", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                        put("region", region.name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist an Shadow Attribute
     *
     * @param shadowAttributes the shadow attributes to persist
     */
    @Deprecated
    public void persistBlocking(final AccountShadowAttribute... shadowAttributes) {
        Iterable<? extends Statement> statements = Mutators
                .upsertAsIterable(accountShadowAttributeMutator, shadowAttributes);
        Mutators.executeBlocking(session, statements);
    }

    /**
     * Persist an Shadow Attribute
     *
     * @param shadowAttributes the shadow attributes to persist
     */
    public Flux<Void> persist(final AccountShadowAttribute... shadowAttributes) {
        Flux<? extends Statement> statements = Mutators
                .upsert(accountShadowAttributeMutator, shadowAttributes);
        return Mutators.execute(session, statements)
                .doOnEach(log.reactiveErrorThrowable("error while saving shadow attributes", throwable -> new HashedMap<String, Object>() {
                    {
                        put("attributes", shadowAttributes.length);
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete all the shadow attributes for the specified account in the provided region.
     *
     * @param region the region
     * @param accountId the account id
     */
    @Deprecated
    public void deleteAllShadowAttributesBlocking(final Region region, final UUID accountId) {
        Mutators.executeBlocking(session, accountShadowAttributeMutator.deleteAll(region, accountId));
    }

    /**
     * Delete all the shadow attributes for the specified account in the provided region.
     *
     * @param region the region
     * @param accountId the account id
     */
    public Flux<Void> deleteAllShadowAttributes(final Region region, final UUID accountId) {
        return Mutators.execute(session, Flux.just(accountShadowAttributeMutator.deleteAll(region, accountId)))
                .doOnEach(log.reactiveErrorThrowable("error while deleting all shadow attributes", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                        put("region", region.name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }
    /**
     * Delete specified shadow attributes for the specified account in the provided region.
     *
     * @param accountShadowAttributeName shadow attribute
     * @param region the region
     * @param accountId the account id
     */
    public Flux<Void> deleteShadowAttributes(final Region region, final UUID accountId, final AccountShadowAttributeName accountShadowAttributeName) {
        return Mutators.execute(session, Flux.just(accountShadowAttributeMutator.delete(region, accountId, accountShadowAttributeName)))
                .doOnEach(log.reactiveErrorThrowable("error while deleting specified shadow attribute", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                        put("region", region.name());
                        put("accountShadowAttribute", accountShadowAttributeName);
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }


    /*
     *
     * Finders!
     *
     */

    /**
     * Find an account by id.
     *
     * @param accountId the account id to find.
     * @return a {@link Flux} of an account represented by the id.
     */
    @Trace(async = true)
    public Flux<Account> findAccountById(final UUID accountId) {
        //
        return ResultSets.query(session, accountMaterializer.fetchById(accountId))
                //
                .flatMapIterable(row -> row)
                //
                .map(row -> new Account()
                        //
                        .setId(row.getUUID("id")).setIamRegion(Enums.of(Region.class, row.getString("iam_region")))
                        .setSubscriptionId(row.getUUID("subscription_id")).setHistoricalId(row.getLong("historical_id"))
                        .setStatus(Enums.of(AccountStatus.class, row.getString("status")))
                        .setRoles(Enums.of(AccountRole.class, row.getSet("roles", String.class)))
                        .setPasswordHash(row.getString("password_hash"))
                        .setPasswordExpired(row.getBool("password_expired"))
                        .setPasswordTemporary(row.getString("password_temporary")))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(log.reactiveErrorThrowable("error while fetching account", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find the Account and Identity Attributes by Region and id.
     *
     * @param region the data region to query
     * @param accountId the account id
     * @return a {@link Flux} of an account identity attributes represented by the id.
     */
    @Trace(async = true)
    public Flux<AccountIdentityAttributes> findAccountIdentityById(final Region region, final UUID accountId) {
        //
        return ResultSets.query(session, accountIdentityAttributesMaterializer.fetchByAccountId(region, accountId))
                //
                .flatMapIterable(row -> row)
                //
                .map(row -> new AccountIdentityAttributes() //
                        .setAccountId(row.getUUID("account_id")) //
                        .setIamRegion(getNullableEnum(row, "iam_region", Region.class)) //
                        .setSubscriptionId(row.getUUID("subscription_id")) //
                        .setHonorificPrefix(row.getString("honorific_prefix")) //
                        .setGivenName(row.getString("given_name")) //
                        .setFamilyName(row.getString("family_name")) //
                        .setHonorificSuffix(row.getString("honorific_suffix")) //
                        .setEmail(row.getSet("email", String.class)) //
                        .setPrimaryEmail(row.getString("primary_email")) //
                        .setAffiliation(row.getString("affiliation")) //
                        .setJobTitle(row.getString("job_title"))) //
                //
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(log.reactiveErrorThrowable("error while fetching identity attributes", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                        put("region", region.name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find an Account by using a hash, typically email.
     *
     * @param hash the hash of some attribute, typically email.
     * @return a {@link Flux} of an account represented by the hash.
     */
    @Trace(async = true)
    public Flux<Account> findAccountByHash(final String hash) {
        return ResultSets.query(session, accountByHashMaterializer.fetchByHash(hash))
                .flatMapIterable(row -> row)
                .flatMap(row -> findAccountById(row.getUUID("account_id")))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(log.reactiveErrorThrowable("error while fetching account", throwable -> new HashedMap<String, Object>() {
                    {
                        put("hash", hash);
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find an Account by using the historical identifier.
     *
     * @param historicalId the identifier (i.e. the id used in legacy MySQL tables)
     * @return a {@link Flux} of an account represented by the historical id.
     */
    public Flux<Account> findAccountByHistoricalId(final Long historicalId) {
        //
        return ResultSets.query(session, accountByHistoricalIdMaterializer.fetchByHistoricalId(historicalId))
                .flatMapIterable(row -> row)
                .flatMap(row -> findAccountById(row.getUUID("account_id")))
                .doOnEach(log.reactiveErrorThrowable("error while fetching account", throwable -> new HashedMap<String, Object>() {
                    {
                        put("historicalId", historicalId);
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all accounts which belong to a particular subscription.
     *
     * @param subscriptionId the subscription id
     * @return a {@link Flux} of accounts tied to a particular subscription.
     */
    @Trace(async = true)
    public Flux<Account> findAccountsBySubscription(final UUID subscriptionId) {
        return ResultSets.query(session, accountBySubscriptionMaterializer.fetchAllBySubscription(subscriptionId))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMapIterable(row -> row)
                .flatMap(row -> findAccountById(row.getUUID("account_id")))
                .doOnEach(log.reactiveErrorThrowable("error while fetching account", throwable -> new HashedMap<String, Object>() {
                    {
                        put("subscriptionId", subscriptionId);
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all the actions performed by an account.
     *
     * @param accountId the account id
     * @return a {@link Flux} of account actions
     */
    public Flux<AccountAction> findActionsByAccountId(final UUID accountId) {
        return ResultSets.query(session, accountActionMaterializer.fetchByAccount(accountId))
                .flatMapIterable(row -> row)
                .map(row -> new AccountAction().setAccountId(row.getUUID("account_id"))
                        .setAction(Enums.of(AccountAction.Action.class, row.getString("action")))
                        .setId(row.getUUID("id")))
                .doOnEach(log.reactiveErrorThrowable("error while fetching account actions", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all the log entries for an account
     *
     * @param region the data region
     * @param accountId the account id
     * @return a {@link Flux} of the account's log entries
     */
    public Flux<AccountLogEntry> findAccountLogEntry(final Region region, final UUID accountId) {
        return ResultSets.query(session, accountLogEventMaterializer.fetchAllByAccount(region, accountId))
                .flatMapIterable(row -> row)
                .map(row -> new AccountLogEntry()
                        //
                        .setAccountId(row.getUUID("account_id"))
                        .setIamRegion(Enums.of(Region.class, row.getString("iam_region")))
                        .setId(row.getUUID("id"))
                        .setAction(Enum.valueOf(AccountLogEntry.Action.class, row.getString("action")))
                        .setOnBehalfOf(row.getUUID("on_behalf_of"))
                        .setMessage(row.getString("message")))
                .doOnEach(log.reactiveErrorThrowable("error while fetching account log entries", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                        put("region", region.name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all the shadow attributes for an account
     *
     * @param region the data region
     * @param accountId the account id
     * @return a {@link Flux} of the account's shadow attributes
     */
    public Flux<AccountShadowAttribute> findAccountShadowAttribute(final Region region, final UUID accountId) {
        return ResultSets.query(session, accountShadowAttributeMaterializer.fetchAllForAccount(region, accountId))
                .flatMapIterable(row -> row)
                .map(this::mapRowToAccountShadowAttribute)
                .doOnEach(log.reactiveErrorThrowable("error while fetching shadow attributes", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                        put("region", region.name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find a specific shadow attribute for an account
     *
     * @param region the data region
     * @param accountId the account id
     * @param name the shadow attribute name
     * @return a {@link Flux} of the specified shadow attribute on the account
     */
    public Flux<AccountShadowAttribute> findAccountShadowAttribute(final Region region, final UUID accountId,
            AccountShadowAttributeName name) {
        return ResultSets
                .query(session, accountShadowAttributeMaterializer.fetchAttributeForAccount(region, accountId, name))
                .flatMapIterable(row -> row)
                .map(this::mapRowToAccountShadowAttribute)
                .doOnEach(log.reactiveErrorThrowable("error while fetching shadow attributes", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                        put("region", region.name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /*
     *
     * Internal Helpers.
     *
     */
    @SuppressWarnings("unchecked")
    private AccountShadowAttribute mapRowToAccountShadowAttribute(Row row) {
        return new AccountShadowAttribute()
                //
                .setAccountId(row.getUUID("account_id"))
                .setIamRegion(Enums.of(Region.class, row.getString("iam_region")))
                .setAttribute(Enums.of(AccountShadowAttributeName.class, row.getString("name")))
                .setValue(row.getString("value"))
                // this gets inferred as a Map<?, AccountShaddowAttributeSource>; cast needed.
                .setSource((Map<UUID, AccountShadowAttributeSource>) Enums
                        .mapValues(AccountShadowAttributeSource.class, row.getMap("source", UUID.class, String.class)));
    }

    /*
     * The following should be migrated to a Credentials service
     */
    @Deprecated
    public void setPasswordFieldsBlocking(final UUID accountId, final String passwordHash, final Boolean passwordExpired, final String tempPassword) {
        Mutators.executeBlocking(session, accountMutator.setPasswordFields(accountId, passwordHash, passwordExpired, tempPassword));
    }

    @Trace(async = true)
    public Flux<Void> setPasswordFields(final UUID accountId, final String passwordHash, final Boolean passwordExpired, final String tempPassword) {
        return Mutators.execute(session,
                Flux.just(accountMutator.setPasswordFields(accountId, passwordHash, passwordExpired, tempPassword)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(log.reactiveErrorThrowable("error while setting password fields", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId", accountId);
                    }
                }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Update the subscription id for an account.
     *
     * @param account the account to update
     * @param subscriptionId the subscription id that should be assigned to the account
     * @param roles the new set or roles to assign to the account
     * @return a {@link Flux} of {@link Void}
     */
    public Flux<Void> updateSubscriptionAndRoles(final Account account, final UUID subscriptionId, final Set<AccountRole> roles) {
        return Mutators.execute(session,
                Flux.just(accountMutator.setSubscription(account.getId(), subscriptionId),
                        accountMutator.setRoles(account.getId(), roles),
                        accountBySubscriptionMutator.delete(account),
                        accountBySubscriptionMutator.addAccountSubscription(account.getId(), subscriptionId),
                        accountIdentityAttributesMutator.setSubscription(account.getIamRegion(), account.getId(),
                                subscriptionId)));
    }

}
