package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class MyCloudAccountTrackingGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MyCloudAccountTrackingGateway.class);

    private final Session session;

    private final AccountByMyCloudUserMutator accountByMyCloudUserMutator;
    private final AccountByMyCloudUserMaterializer accountByMyCloudUserMaterializer;
    private final MyCloudUserByAccountMutator myCloudUserByAccountMutator;
    private final MyCloudUserByAccountMaterializer myCloudUserByAccountMaterializer;

    @Inject
    public MyCloudAccountTrackingGateway(final Session session,
                                     final AccountByMyCloudUserMutator accountByMyCloudUserMutator,
                                     final AccountByMyCloudUserMaterializer accountByMyCloudUserMaterializer,
                                     final MyCloudUserByAccountMutator myCloudUserByAccountMutator,
                                     final MyCloudUserByAccountMaterializer myCloudUserByAccountMaterializer) {
        this.session = session;
        this.accountByMyCloudUserMutator = accountByMyCloudUserMutator;
        this.accountByMyCloudUserMaterializer = accountByMyCloudUserMaterializer;
        this.myCloudUserByAccountMutator = myCloudUserByAccountMutator;
        this.myCloudUserByAccountMaterializer = myCloudUserByAccountMaterializer;
    }

    /**
     * Persist the tracking between a bronte account id and the myCloud user id
     *
     * @param myCloudAccountTracking the object representing the tracking relationship to persist
     * @return Flux of void
     */
    public Flux<Void> persist(final MyCloudAccountTracking myCloudAccountTracking) {
        return Mutators.execute(session, Flux.just(
                accountByMyCloudUserMutator.upsert(myCloudAccountTracking),
                myCloudUserByAccountMutator.upsert(myCloudAccountTracking)
        )).doOnEach(log.reactiveErrorThrowable("error persisting the ies account tracking record"));
    }

    /**
     * Find the Bronte account id for an myCloud user id
     *
     * @param iesUserId the myCloud user id to find the associated Bronte account id for
     * @return a mono of myCloud account tracking object or an empty stream when not found
     */
    public Mono<MyCloudAccountTracking> findAccountId(final String iesUserId) {
        return ResultSets.query(session, accountByMyCloudUserMaterializer.findAccountId(iesUserId))
                .flatMapIterable(row -> row)
                .map(accountByMyCloudUserMaterializer::fromRow)
                .doOnEach(log.reactiveErrorThrowable("error fetching the ies account tracking record"))
                .singleOrEmpty();
    }

    /**
     * Find the myCloud user id for a Bronte account id
     *
     * @param accountId the account id to find the ies user id for
     * @return a mono of myCloud account tracking or an empty stream when not found
     */
    public Mono<MyCloudAccountTracking> findIesUserId(final UUID accountId) {
        return ResultSets.query(session, myCloudUserByAccountMaterializer.findMyCloudUserId(accountId))
                .flatMapIterable(row -> row)
                .map(myCloudUserByAccountMaterializer::fromRow)
                .doOnEach(log.reactiveErrorThrowable("error fetching the myCloud account tracking record"))
                .singleOrEmpty();
    }
}
