package com.smartsparrow.graphql.schema;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Sets;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.sso.service.IESService;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;

@Singleton
public class AccountIdentitySchema {

    private final AccountService accountService;
    private final IESService iesService;

    @Inject
    public AccountIdentitySchema(final AccountService accountService, IESService iesService) {
        this.accountService = accountService;
        this.iesService = iesService;
    }

    /**
     * Expose the identity attributes under an Account
     *
     * @param resolutionEnvironment the graphQL resolution environment
     * @param account the account context
     * @return the identity attributes for that account
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Account.identity")
    @GraphQLQuery(name = "identity", description = "Get the account identity attributes")
    public CompletableFuture<AccountIdentityAttributes> identityFromAccount(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                            @GraphQLContext Account account) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        MutableAuthenticationContext mutableAuthenticationContext = context.getMutableAuthenticationContext();

        // Ideally at this stage all accounts should be IES authorized, for now still support non IES accounts
        if (mutableAuthenticationContext != null &&
                mutableAuthenticationContext.getPearsonUid() != null &&
                mutableAuthenticationContext.getAuthenticationType().equals(AuthenticationType.IES)) {

            return iesService.getProfile(mutableAuthenticationContext.getPearsonUid(),
                                         mutableAuthenticationContext.getPearsonToken())
                    .single()
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnError(Exception.class, ex -> {
                        throw new NotFoundFault("identity profile not found");
                    })
                    .map(identityProfile -> new AccountIdentityAttributes()
                            .setAccountId(account.getId())
                            .setPrimaryEmail(identityProfile.getPrimaryEmail())
                            .setGivenName(identityProfile.getGivenName())
                            .setFamilyName(identityProfile.getFamilyName())
                            .setEmail(Sets.newHashSet(identityProfile.getPrimaryEmail())))
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .toFuture();
        }
        return accountService.findIdentityById(account.getId())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }
}
