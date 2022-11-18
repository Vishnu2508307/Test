package com.smartsparrow.iam.service;

import java.time.Instant;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.iam.data.DeveloperKeyGateway;
import com.smartsparrow.util.Tokens;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class DeveloperKeyService {

    private final DeveloperKeyGateway developerKeyGateway;

    @Inject
    public DeveloperKeyService(DeveloperKeyGateway developerKeyGateway) {
        this.developerKeyGateway = developerKeyGateway;
    }

    /**
     * Generate a developer key for a subscription
     *
     * @param subscriptionId the subscription to associtated the key to
     * @return {@link DeveloperKey}
     */
    public DeveloperKey createKey(UUID subscriptionId, UUID accountId) {

        DeveloperKey developerKey = new DeveloperKey()
                .setKey(Tokens.generate())
                .setSubscriptionId(subscriptionId)
                .setAccountId(accountId)
                .setCreatedTs(Instant.now().toEpochMilli());

        developerKeyGateway.persist(developerKey).blockLast();

        return developerKey;
    }

    /**
     * Fetch all the keys associated to a subscription
     *
     * @param subscriptionId the associated subscription
     * @return a {@link Flux} of developer key
     */
    public Flux<DeveloperKey> fetchBySubscription(UUID subscriptionId) {
        return developerKeyGateway.fetchBySubscription(subscriptionId);
    }

    /**
     * Fetch a developer key by value
     *
     * @param key the developer key value
     * @return a {@link Mono} of developer key or an empty stream when the key is not found
     */
    public Mono<DeveloperKey> fetchByValue(String key) {
        return developerKeyGateway.fetchByKey(key);
    }

    /**
     * Fetch a developer key by the associated account id
     *
     * @param accountId the associated account id
     * @return a {@link Flux} of developer key or an empty stream when the key is not found
     */
    public Flux<DeveloperKey> fetchByAccount(UUID accountId) {
        return developerKeyGateway.fetchByAccount(accountId);
    }
}
