package com.smartsparrow.graphql.schema;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.Subscription;
import com.smartsparrow.iam.service.SubscriptionService;

import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;

@Singleton
public class SubscriptionSchema {

    private final SubscriptionService subscriptionService;

    @Inject
    public SubscriptionSchema(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * Expose the subscription under an Account.
     *
     * @param account the account context
     * @return the subscription for the account.
     */
    @GraphQLQuery(name = "subscription", description = "Account subscription information")
    public CompletableFuture<Subscription> subscriptionFromAccount(@GraphQLContext Account account) {
        return subscriptionService.find(account.getSubscriptionId()).single().toFuture();
    }

}
