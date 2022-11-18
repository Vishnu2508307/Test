package com.smartsparrow.sso.data.oidc;

import javax.inject.Inject;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.sso.service.SessionAccount;

import reactor.core.publisher.Flux;

public class SessionAccountGateway {

    private final SessionByAccountMutator sessionByAccountMutator;
    private final AccountBySessionMutator accountBySessionMutator;

    private final Session session;

    @Inject
    public SessionAccountGateway(SessionByAccountMutator sessionByAccountMutator,
            AccountBySessionMutator accountBySessionMutator,
            Session session) {
        this.sessionByAccountMutator = sessionByAccountMutator;
        this.accountBySessionMutator = accountBySessionMutator;
        this.session = session;
    }

    public Flux<Void> persist(SessionAccount sessionAccount) {
        return Mutators.execute(session, Flux.just(sessionByAccountMutator.upsert(sessionAccount),
                                                   accountBySessionMutator.upsert(sessionAccount)));
    }
}
