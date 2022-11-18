package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class FederationGateway {

    private final FederatedIdentityMaterializer federatedIdentityMaterializer;
    private final FederatedIdentityMutator federatedIdentityMutator;

    private final Session session;

    @Inject
    public FederationGateway(FederatedIdentityMaterializer federatedIdentityMaterializer,
                             FederatedIdentityMutator federatedIdentityMutator,
                             Session session) {
        this.federatedIdentityMaterializer = federatedIdentityMaterializer;
        this.federatedIdentityMutator = federatedIdentityMutator;
        this.session = session;
    }

    /**
     * Persist federated identity
     *
     * @param federation the federated identity
     */
    public Flux<Void> persist(final FederatedIdentity federation) {
        return Mutators.execute(session, Flux.just(federatedIdentityMutator.upsert(federation)));
    }

    /**
     * Find federated identity
     */
    public Mono<FederatedIdentity> fetchByFederation(final UUID subscriptionId,
            final String clientId,
            final String subjectId) {
        return ResultSets.query(session, federatedIdentityMaterializer.findByFederation(subscriptionId, clientId, subjectId))
                .flatMapIterable(row -> row)
                .map(federatedIdentityMaterializer::fromRow)
                .singleOrEmpty();
    }
}
