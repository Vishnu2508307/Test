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
public class SharedResourceGateway {
    private final Session session;
    private final static MercuryLogger log = MercuryLoggerFactory.getLogger(SharedResourceGateway.class);

    private final SharedResourceMutator sharedResourceMutator;
    private final SharedResourceMaterializer sharedResourceMaterializer;

    @Inject
    public SharedResourceGateway(final Session session,
                                 final SharedResourceMutator sharedResourceMutator,
                                 final SharedResourceMaterializer sharedResourceMaterializer) {
        this.session = session;
        this.sharedResourceMutator = sharedResourceMutator;
        this.sharedResourceMaterializer = sharedResourceMaterializer;
    }

    /**
     * Persist Shared resource with account id and shared account id
     * @param sharedResource course
     * @return mono of void
     */
    @Trace(async = true)
    public Flux<Void> persist(final SharedResource sharedResource) {

        return Mutators.execute(session, Flux.just(sharedResourceMutator.upsert(sharedResource)))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(e -> {
                    log.error(String.format("Error: persist, with SharedResource %s", sharedResource), e);
                    throw Exceptions.propagate(e);
                });
    }
}
