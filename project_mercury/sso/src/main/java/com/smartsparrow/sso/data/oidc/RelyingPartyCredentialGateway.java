package com.smartsparrow.sso.data.oidc;

import java.util.UUID;

import javax.inject.Inject;

import com.datastax.driver.core.Session;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.sso.service.OpenIDConnectRelyingPartyCredential;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RelyingPartyCredentialGateway {

    private final RelyingPartyCredentialMaterializer relyingPartyCredentialMaterializer;
    private final RelyingPartyCredentialMutator relyingPartyCredentialMutator;

    private final Session session;

    @Inject
    public RelyingPartyCredentialGateway(RelyingPartyCredentialMaterializer relyingPartyCredentialMaterializer,
            RelyingPartyCredentialMutator relyingPartyCredentialMutator,
            Session session) {
        this.relyingPartyCredentialMaterializer = relyingPartyCredentialMaterializer;
        this.relyingPartyCredentialMutator = relyingPartyCredentialMutator;
        this.session = session;
    }

    /**
     * Persist a Relying party credential
     *
     * @param credential the credential to persist
     * @return nothing.
     */
    @Trace(async = true)
    public Flux<Void> persist(final OpenIDConnectRelyingPartyCredential credential) {
        return Mutators.execute(session, Flux.just(relyingPartyCredentialMutator.upsert(credential))).doOnEach(
                ReactiveTransaction.linkOnNext());
    }

    /**
     * Find a Relying Party Credential
     *
     * @param relyingPartyId the relying party id
     * @return the credentials for this id.
     */
    public Mono<OpenIDConnectRelyingPartyCredential> find(final UUID relyingPartyId) {
        return ResultSets.query(session, relyingPartyCredentialMaterializer.find(relyingPartyId)) //
                .flatMapIterable(row -> row) //
                .map(relyingPartyCredentialMaterializer::fromRow) //
                .singleOrEmpty();
    }

}
