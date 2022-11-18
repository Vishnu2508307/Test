package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.iam.service.DeveloperKey;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class DeveloperKeyGateway {

    private static final Logger log = LoggerFactory.getLogger(DeveloperKeyGateway.class);

    private Session session;

    private final DeveloperKeyMaterializer developerKeyMaterializer;
    private final DeveloperKeyMutator developerKeyMutator;
    private final DeveloperKeyBySubscriptionMaterializer developerKeyBySubscriptionMaterializer;
    private final DeveloperKeyBySubscriptionMutator developerKeyBySubscriptionMutator;
    private final DeveloperKeyByAccountMaterializer developerKeyByAccountMaterializer;
    private final DeveloperKeyByAccountMutator developerKeyByAccountMutator;

    @Inject
    public DeveloperKeyGateway(Session session,
                               DeveloperKeyMaterializer developerKeyMaterializer,
                               DeveloperKeyMutator developerKeyMutator,
                               DeveloperKeyBySubscriptionMaterializer developerKeyBySubscriptionMaterializer,
                               DeveloperKeyBySubscriptionMutator developerKeyBySubscriptionMutator,
                               DeveloperKeyByAccountMaterializer developerKeyByAccountMaterializer,
                               DeveloperKeyByAccountMutator developerKeyByAccountMutator) {
        this.session = session;
        this.developerKeyMaterializer = developerKeyMaterializer;
        this.developerKeyMutator = developerKeyMutator;
        this.developerKeyBySubscriptionMaterializer = developerKeyBySubscriptionMaterializer;
        this.developerKeyBySubscriptionMutator = developerKeyBySubscriptionMutator;
        this.developerKeyByAccountMaterializer = developerKeyByAccountMaterializer;
        this.developerKeyByAccountMutator = developerKeyByAccountMutator;
    }

    /**
     * Persist the created developer key to the developer key tables
     *
     * @param developerKey the {@link DeveloperKey} to persist
     * @return a {@link Flux} of void
     */
    public Flux<Void> persist(DeveloperKey developerKey) {
        return Mutators.execute(session, Flux.just(
                developerKeyMutator.upsert(developerKey),
                developerKeyBySubscriptionMutator.upsert(developerKey),
                developerKeyByAccountMutator.upsert(developerKey)))
                .doOnError(throwable -> {
                    log.error(String.format("error while saving developer key for account %s",
                            developerKey.getAccountId()), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch all the developer keys associated to a subscription
     *
     * @param subscriptionId the associated subscription
     * @return a {@link Flux} of developer key
     */
    public Flux<DeveloperKey> fetchBySubscription(UUID subscriptionId) {
        return ResultSets.query(session, developerKeyBySubscriptionMaterializer.fetchBySubscription(subscriptionId))
                .flatMapIterable(row->row)
                .map(this::mapRowToDeveloperKey)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching all developer keys for subscription %s",
                            subscriptionId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch a developer key by its value
     *
     * @param key the developer key value
     * @return a {@link Mono} of developer key or an empty stream when the materializer returns no results
     */
    public Mono<DeveloperKey> fetchByKey(String key) {
        return ResultSets.query(session, developerKeyMaterializer.fetchByKey(key))
                .flatMapIterable(row->row)
                .map(this::mapRowToDeveloperKey)
                .singleOrEmpty()
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching developer key for key %s", key), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch a developer key by account id
     *
     * @param accountId the associated account id
     * @return a {@link Mono} of developer key or an empty stream when the materializer returns no results
     */
    public Flux<DeveloperKey> fetchByAccount(UUID accountId) {
        return ResultSets.query(session, developerKeyByAccountMaterializer.fetchByAccount(accountId))
                .flatMapIterable(row->row)
                .map(this::mapRowToDeveloperKey)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching developer key for account %s", accountId), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Convenience method to map a {@link Row} to a developer key
     * @param row the row to convert
     * @return a {@link DeveloperKey} object
     */
    private DeveloperKey mapRowToDeveloperKey(Row row) {
        return new DeveloperKey()
                .setKey(row.getString("key"))
                .setSubscriptionId(row.getUUID("subscription_id"))
                .setAccountId(row.getUUID("account_id"))
                .setCreatedTs(row.getLong("created_ts"));
    }
}
