package com.smartsparrow.sso.data.oidc;

import javax.inject.Inject;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.sso.service.OpenIDConnectLogEvent;

import reactor.core.publisher.Flux;

public class LogEventGateway {

    private final LogEventMutator logEventMutator;

    private final Session session;

    @Inject
    public LogEventGateway(LogEventMutator logEventMutator, Session session) {
        this.logEventMutator = logEventMutator;
        this.session = session;
    }

    public Flux<Void> persist(final OpenIDConnectLogEvent logEvent) {
        return Mutators.execute(session, Flux.just(logEventMutator.upsert(logEvent)));
    }
}
