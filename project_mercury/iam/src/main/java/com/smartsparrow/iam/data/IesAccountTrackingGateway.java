package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class IesAccountTrackingGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(IesAccountTrackingGateway.class);

    private final Session session;

    private final AccountByIesUserMutator accountByIesUserMutator;
    private final AccountByIesUserMaterializer accountByIesUserMaterializer;
    private final IesUserByAccountMutator iesUserByAccountMutator;
    private final IesUserByAccountMaterializer iesUserByAccountMaterializer;

    @Inject
    public IesAccountTrackingGateway(final Session session,
                                     final AccountByIesUserMutator accountByIesUserMutator,
                                     final AccountByIesUserMaterializer accountByIesUserMaterializer,
                                     final IesUserByAccountMutator iesUserByAccountMutator,
                                     final IesUserByAccountMaterializer iesUserByAccountMaterializer) {
        this.session = session;
        this.accountByIesUserMutator = accountByIesUserMutator;
        this.accountByIesUserMaterializer = accountByIesUserMaterializer;
        this.iesUserByAccountMutator = iesUserByAccountMutator;
        this.iesUserByAccountMaterializer = iesUserByAccountMaterializer;
    }

    /**
     * Persist the tracking between a bronte account id and the ies user id
     *
     * @param iesAccountTracking the object representing the tracking relationship to persist
     * @return Flux of void
     */
    public Flux<Void> persist(final IESAccountTracking iesAccountTracking) {
        return Mutators.execute(session, Flux.just(
                accountByIesUserMutator.upsert(iesAccountTracking),
                iesUserByAccountMutator.upsert(iesAccountTracking)
        )).doOnEach(log.reactiveErrorThrowable("error persisting the ies account tracking record"));
    }

    /**
     * Find the Bronte account id for an ies user id
     *
     * @param iesUserId the ies user id to find the associated Bronte account id for
     * @return a mono of ies account tracking object or an empty stream when not found
     */
    @Trace(async = true)
    public Mono<IESAccountTracking> findAccountId(final String iesUserId) {
        return ResultSets.query(session, accountByIesUserMaterializer.findAccountId(iesUserId))
                .flatMapIterable(row -> row)
                .map(accountByIesUserMaterializer::fromRow)
                .doOnEach(log.reactiveErrorThrowable("error fetching the ies account tracking record"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .singleOrEmpty();
    }

    /**
     * Find the ies user id for a Bronte account id
     *
     * @param accountId the account id to find the ies user id for
     * @return a mono of ies account tracking or an empty stream when not found
     */
    public Mono<IESAccountTracking> findIesUserId(final UUID accountId) {
        return ResultSets.query(session, iesUserByAccountMaterializer.findIesUserId(accountId))
                .flatMapIterable(row -> row)
                .map(iesUserByAccountMaterializer::fromRow)
                .doOnEach(log.reactiveErrorThrowable("error fetching the ies account tracking record"))
                .singleOrEmpty();
    }
}
