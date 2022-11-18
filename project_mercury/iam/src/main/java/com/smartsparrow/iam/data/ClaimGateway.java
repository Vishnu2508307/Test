package com.smartsparrow.iam.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.iam.service.Claim;
import com.smartsparrow.iam.service.Region;

import reactor.core.publisher.Flux;

/**
 *
 */
@Singleton
public class ClaimGateway {

    private Session session;

    private final ClaimByAccountMutator claimByAccountMutator;
    private final ClaimByAccountMaterializer claimByAccountMaterializer;
    private final ClaimNameBySubscriptionMutator claimNameBySubscriptionMutator;

    @Inject
    public ClaimGateway(Session session,
            ClaimByAccountMutator claimByAccountMutator, ClaimByAccountMaterializer claimByAccountMaterializer,
            ClaimNameBySubscriptionMutator claimNameBySubscriptionMutator) {
        this.session = session;
        this.claimByAccountMutator = claimByAccountMutator;
        this.claimByAccountMaterializer = claimByAccountMaterializer;
        this.claimNameBySubscriptionMutator = claimNameBySubscriptionMutator;
    }

    /**
     * Persist a claim
     * @param claim the claim to persist
     * @return a Flux void.
     */
    public Flux<Void> persist(final Claim claim) {
        return Mutators.execute(session, Flux.just(claimByAccountMutator.upsert(claim),
                                                   claimNameBySubscriptionMutator.upsert(claim)));
    }

    /**
     * Find all the claims for the provided account in the supplied subscription
     *
     * @param region the IAM region to query
     * @param accountId the account id
     * @param subscriptionId the subscription identifier
     * @return A Flux of the claims, or empty if none.
     */
    public Flux<Claim> find(final Region region, final UUID accountId, final UUID subscriptionId) {
        return ResultSets.query(session, claimByAccountMaterializer.find(region, accountId, subscriptionId))
                .flatMapIterable(row -> row)
                .map(claimByAccountMaterializer::fromRow);
    }

}
