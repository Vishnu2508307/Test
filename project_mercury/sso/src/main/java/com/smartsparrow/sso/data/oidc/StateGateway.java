package com.smartsparrow.sso.data.oidc;

import javax.inject.Inject;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.sso.service.OpenIDConnectState;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class StateGateway {

    private final StateMaterializer stateMaterializer;
    private final StateMutator stateMutator;

    private final Session session;

    @Inject
    public StateGateway(StateMaterializer stateMaterializer,
            StateMutator stateMutator,
            Session session) {
        this.stateMaterializer = stateMaterializer;
        this.stateMutator = stateMutator;
        this.session = session;
    }

    /**
     * Persist an Open ID Connect State.
     *
     * @param openIDConnectState the state to persist.
     * @return nada.
     */
    public Flux<Void> persist(final OpenIDConnectState openIDConnectState) {
        return Mutators.execute(session, Flux.just(stateMutator.upsert(openIDConnectState)));
    }

    /**
     * Find a State
     *
     * @param state the state to find
     * @return the state represented by the supplied parameter
     */
    public Mono<OpenIDConnectState> find(final String state) {
        return ResultSets.query(session, stateMaterializer.find(state)) //
                .flatMapIterable(row -> row) //
                .map(stateMaterializer::fromRow) //
                .singleOrEmpty();
    }
}
