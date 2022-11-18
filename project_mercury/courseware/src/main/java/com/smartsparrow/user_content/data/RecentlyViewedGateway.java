package com.smartsparrow.user_content.data;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

@Singleton
public class RecentlyViewedGateway {

    private final Session session;

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RecentlyViewedGateway.class);

    private final RecentlyViewedMutator recentlyViewedMutator;
    private final RecentlyViewedMaterializer recentlyViewedMaterializer;


    @Inject
    public RecentlyViewedGateway(final Session session,
                                 final RecentlyViewedMutator recentlyViewedMutator,
                                 final RecentlyViewedMaterializer recentlyViewedMaterializer) {
        this.session = session;
        this.recentlyViewedMutator = recentlyViewedMutator;
        this.recentlyViewedMaterializer = recentlyViewedMaterializer;
    }

    /**
     * Persist recentlyViewed object for the account id
     * @param recentlyViewed recently view details
     * @return Flux of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final RecentlyViewed recentlyViewed) {
        return Mutators.execute(session, Flux.just(recentlyViewedMutator.upsert(recentlyViewed)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(e -> {
                    log.error(String.format("Error: persist, with RecentlyViewed %s", recentlyViewed), e);
                    throw Exceptions.propagate(e);
                });
    }
}
