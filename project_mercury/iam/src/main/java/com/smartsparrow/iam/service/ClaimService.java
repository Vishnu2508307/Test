package com.smartsparrow.iam.service;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.smartsparrow.iam.data.ClaimGateway;

import reactor.core.publisher.Flux;

/**
 * Manage IAM claims.
 *
 * A claim is a name-value pair provided by a third party authentication mechanism. These claims are not intended
 * to have their attribute changes tracked over time.
 */
public class ClaimService {

    private final AccountService accountService;
    private final ClaimGateway claimGateway;

    @Inject
    public ClaimService(AccountService accountService, ClaimGateway claimGateway) {
        this.accountService = accountService;
        this.claimGateway = claimGateway;
    }

    /**
     * Add a claim to a user.
     *
     * @param accountId the account id
     * @param subscriptionId the subscription id of the associated identity provider
     * @param name the name of the claim
     * @param value the value of the claim
     * @return a flux void to subscribe to as part of the reactive chain
     */
    public Flux<Void> add(final UUID accountId,
            final UUID subscriptionId,
            final String name,
            @Nullable final String value) {
        affirmArgument(accountId != null, "missing account id");
        affirmArgument(subscriptionId != null, "missing subscription id");
        affirmArgumentNotNullOrEmpty(name, "missing name");
        //
        return accountService.findById(accountId) //
                .map(account -> new Claim() //
                        .setAccountId(account.getId())
                        .setIamRegion(account.getIamRegion())
                        .setSubscriptionId(subscriptionId)
                        .setName(name)
                        .setValue(value)) //
                .flatMap(claim -> claimGateway.persist(claim));
    }

    /**
     * Find the claims
     *
     * @param accountId the account id
     * @param subscriptionId the subscription id of the associated identity provider
     * @return the set of claims as specified by this subscription
     */
    public Flux<Claim> find(final UUID accountId, final UUID subscriptionId) {
        affirmArgument(accountId != null, "missing account id");
        affirmArgument(subscriptionId != null, "missing subscription id");
        //
        return accountService.findById(accountId) //
                .flatMap(account -> claimGateway.find(account.getIamRegion(), account.getId(), subscriptionId));
    }

}
