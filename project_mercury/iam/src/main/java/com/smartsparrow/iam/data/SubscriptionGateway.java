package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.map.HashedMap;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.iam.service.Region;
import com.smartsparrow.iam.service.Subscription;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

@Singleton
public class SubscriptionGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(SubscriptionGateway.class);

    private final Session session;

    //
    private final SubscriptionMutator subscriptionMutator;
    private final SubscriptionMaterializer subscriptionMaterializer;

    @Inject
    public SubscriptionGateway(Session session,
            SubscriptionMutator subscriptionMutator,
            SubscriptionMaterializer subscriptionMaterializer) {
        this.session = session;
        this.subscriptionMutator = subscriptionMutator;
        this.subscriptionMaterializer = subscriptionMaterializer;
    }

    /**
     * Persist subscription mutations.
     * @deprecated use non-blocking {@link SubscriptionGateway#persist(Subscription...)} instead
     *
     * @param mutations mutations to persist
     */
    @Deprecated
    public void persistBlocking(final Subscription... mutations) {
        Iterable<? extends Statement> iter = Mutators.upsertAsIterable(subscriptionMutator, mutations);
        Mutators.executeBlocking(session, iter);
    }

    /**
     * Persist subscription mutations.
     *
     * @param mutations mutations to persist
     * @return Flux of {@link Void}
     */
    public Flux<Void> persist(final Subscription... mutations) {
        Flux<? extends Statement> iter = Mutators.upsert(subscriptionMutator, mutations);
        return Mutators.execute(session, iter)
                .doOnEach(log.reactiveErrorThrowable("error while saving subscriptions", throwable -> new HashedMap<String, Object>() {
                    {
                        put("mutations", mutations.length);
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch a subscription.
     *
     * @param id the id of the subscription
     * @return the subscription matching the id
     */
    public Flux<Subscription> fetchSubscription(final UUID id) {
        return ResultSets.query(session, subscriptionMaterializer.fetchSubscription(id))
                //
                .flatMapIterable(row -> row)
                //
                .map(row -> new Subscription().setId(row.getUUID("id"))
                        .setName(row.getString("name"))
                        .setIamRegion(Enums.of(Region.class, row.getString("iam_region"))))
                .doOnEach(log.reactiveErrorThrowable("error while fetching subscription", throwable -> new HashedMap<String, Object>() {
                    {
                        put("subscriptionId", id);
                    }
                }))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                });
    }
}
