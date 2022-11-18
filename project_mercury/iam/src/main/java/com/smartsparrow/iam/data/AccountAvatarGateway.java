package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.map.HashedMap;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.Region;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

@Singleton
public class AccountAvatarGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AccountAvatarGateway.class);

    //
    private final Session session;

    private final AccountAvatarMutator accountAvatarMutator;
    private final AccountAvatarMaterializer accountAvatarMaterializer;

    @Inject
    public AccountAvatarGateway(Session session,
            AccountAvatarMutator accountAvatarMutator,
            AccountAvatarMaterializer accountAvatarMaterializer) {
        this.session = session;
        this.accountAvatarMutator = accountAvatarMutator;
        this.accountAvatarMaterializer = accountAvatarMaterializer;
    }

    /**
     * Persist an Account Avatar
     * Calling mutators in a blocking way is deprecated
     *
     * @param avatar the avatar to persist
     */
    @Deprecated
    public void persistBlocking(final AccountAvatar avatar) {
        Mutators.executeBlocking(session, accountAvatarMutator.upsert(avatar));
    }

    /**
     * Persist an Account Avatar in a non blocking way
     *
     * @param avatar the avatar to persist
     */
    public Flux<Void> persist(final AccountAvatar avatar) {
        return Mutators.execute(session, Flux.just(accountAvatarMutator.upsert(avatar)))
                .doOnEach(log.reactiveErrorThrowable("error while saving account avatar", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId",  avatar.getAccountId());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete an Account Avatar, requires the Account Id, Region, and Avatar Size
     * Calling mutators in a blocking way is deprecated
     *
     * @param avatar the avatar to delete
     */
    @Deprecated
    public void deleteBlocking(final AccountAvatar avatar) {
        Mutators.executeBlocking(session, accountAvatarMutator.delete(avatar));
    }

    /**
     * Delete an Account Avatar, requires the Account Id, Region, and Avatar Size
     *
     * @param avatar the avatar to delete
     */
    public Flux<Void> delete(final AccountAvatar avatar) {
        return Mutators.execute(session, Flux.just(accountAvatarMutator.delete(avatar)))
                .doOnEach(log.reactiveErrorThrowable("error while deleting account avatar", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId",  avatar.getAccountId());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Delete all the avatars for the specified account in the provided region.
     * Calling mutators in a blocking way is deprecated
     *
     * @param region the region
     * @param accountId the account id
     */
    @Deprecated
    public void deleteAllAvatarsBlocking(final Region region, final UUID accountId) {
        Mutators.executeBlocking(session, accountAvatarMutator.deleteAll(region, accountId));
    }

    /**
     * Delete all the avatars for the specified account in the provided region.
     *
     * @param region the region
     * @param accountId the account id
     */
    public Flux<Void> deleteAllAvatars(final Region region, final UUID accountId) {
        return Mutators.execute(session, Flux.just(accountAvatarMutator.deleteAll(region, accountId)))
                .doOnEach(log.reactiveErrorThrowable("error while deleting all avatars", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId",  accountId);
                        put("region",  region.name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all the account avatars for an account.
     *
     * @param region the data region to query
     * @param accountId the account id
     * @return a {@link Flux} of all the avatars for an account
     */
    public Flux<AccountAvatar> findAvatarByAccountId(final Region region, final UUID accountId) {
        return ResultSets.query(session, accountAvatarMaterializer.fetchAllByAccount(region, accountId))
                .flatMapIterable(row -> row)
                .map(this::mapRowToAccountAvatar)
                .doOnEach(log.reactiveErrorThrowable("error while fetching avatars", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId",  accountId);
                        put("region",  region.name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find a specific size/name avatar for an account.
     *
     * @param region the data region to query
     * @param accountId the account id
     * @param size the avatar size
     * @return a {@link Flux} of the sized account avatar
     */
    @Trace(async = true)
    public Flux<AccountAvatar> findAvatarByAccountId(final Region region, final UUID accountId, final AccountAvatar.Size size) {
        return ResultSets.query(session, accountAvatarMaterializer.fetchBySizeAccount(region, accountId, size))
                .flatMapIterable(row -> row)
                .map(this::mapRowToAccountAvatar)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(log.reactiveErrorThrowable("error while fetching avatars", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId",  accountId);
                        put("region",  region.name());
                        put("size",  size.name());
                    }
                }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Find all the Avatars for an account, without the data payload.
     * @param region
     * @param accountId
     * @return
     */
    public Flux<AccountAvatar> findAvatarInfoByAccountId(final Region region, final UUID accountId) {
        return ResultSets.query(session, accountAvatarMaterializer.fetchAllInfoByAccount(region, accountId))
                .flatMapIterable(row -> row)
                .map(this::mapRowToAccountAvatar)
                .doOnEach(log.reactiveErrorThrowable("error while fetching avatar info", throwable -> new HashedMap<String, Object>() {
                    {
                        put("accountId",  accountId);
                        put("region",  region.name());
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    private AccountAvatar mapRowToAccountAvatar(Row row) {
        return new AccountAvatar()
                //
                .setAccountId(row.getUUID("account_id"))
                .setIamRegion(Enums.of(Region.class, row.getString("iam_region")))
                .setName(Enums.of(AccountAvatar.Size.class, row.getString("name")))
                .setMimeType(row.getString("mime_type"))
                .setMeta(row.getMap("meta", String.class, String.class))
                // the supplied row may/may not include the actual data, support both.
                .setData(row.getColumnDefinitions().contains("data") ? row.getString("data") : null);
    }
}
