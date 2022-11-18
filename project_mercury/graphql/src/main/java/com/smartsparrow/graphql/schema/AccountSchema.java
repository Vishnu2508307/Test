package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.service.AccountShadowAttributeName.AERO_ACCESS;
import static com.smartsparrow.iam.util.Permissions.affirmPermission;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowSupportRole;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAccess;
import com.smartsparrow.iam.service.AccountLogEntry;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AccountShadowAttributeSource;
import com.smartsparrow.util.Hashing;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

@Singleton
public class AccountSchema {

    private final AccountService accountService;
    private final AllowSupportRole allowSupportRole;

    @Inject
    public AccountSchema(AccountService accountService,
                         AllowSupportRole allowSupportRole) {
        this.accountService = accountService;
        this.allowSupportRole = allowSupportRole;
    }

    /**
     * Get the account of the current authorized user.
     * @param resolutionEnvironment graphQL resolution environment
     * @return the account
     */
    @GraphQLQuery(name = "account", description = "Get an account")
    public CompletableFuture<Account> getAccount(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        return Mono.just(context.getAuthenticationContext().getAccount()).toFuture();
    }

    /**
     * Get the account within the enrollment context
     *
     * @param cohortEnrollment the enrollment context
     * @return the account
     */
    @GraphQLQuery(name = "account", description = "Get an account")
    public CompletableFuture<Account> getAccount(@GraphQLContext CohortEnrollment cohortEnrollment) {
        return accountService.findById(cohortEnrollment.getAccountId()).singleOrEmpty().toFuture();
    }

    /**
     * Get the account by email
     *
     * @param email the email to find the account for
     * @return the account
     */
    @GraphQLQuery(name = "accountByEmail", description = "Get an account by email")
    public CompletableFuture<Account> getAccountByEmail(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
            @GraphQLArgument(name = "email", description = "the email to find the account for") String email) {
        affirmArgument(email != null, "email argument is required");

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        // only allow a user with SUPPORT role
        affirmPermission(allowSupportRole.test(context.getAuthenticationContext()), "Not allowed");

        Account account = context.getAuthenticationContext().getAccount();

        // track the action in the account logs table for the requesting account
        return accountService.findByEmail(email).singleOrEmpty()
                .map(acc -> {
                    accountService.addLogEntry(account.getId(), AccountLogEntry.Action.ACCOUNT_INFO_REQUESTED, null,
                                               String.format("requesting information on account hash %s", Hashing.email(email)));
                    accountService.addLogEntry(acc.getId(), AccountLogEntry.Action.ACCOUNT_INFO_REQUESTED, null,
                                               String.format("this account information requested by account %s", account.getId()));
                    return acc;
                }).toFuture();
    }

    /**
     * Find if the account has access to AERO or not
     * @param resolutionEnvironment the resolution environment
     * @param account the account to verify the AERO access for
     * @return the account access info
     */
    @GraphQLQuery(name = "accountAccess", description = "Find if the account has access to AERO")
    public CompletableFuture<AccountAccess> getAccountAccess(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                             @GraphQLContext Account account) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        // only allow a user with SUPPORT role
        affirmPermission(allowSupportRole.test(context.getAuthenticationContext()), "Not allowed");

        return accountService.findShadowAttribute(account, AERO_ACCESS)
                .onErrorResume(NotFoundFault.class, ex -> Mono.empty())
                .hasElement()
                .map(truthy -> new AccountAccess()
                        .setAccountId(account.getId())
                        .setAeroAccess(truthy)).toFuture();
    }

    /**
     * Grant aero access to an account; this is an idempotent action.
     *
     * @param accountId the account id to grant access to
     * @return true or throws an exception.
     */
    @GraphQLMutation(name = "grantAeroAccess")
    public CompletableFuture<Boolean> grantAeroAccess(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                      @GraphQLNonNull @GraphQLArgument(name = "accountId") final UUID accountId) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        // only allow a user with SUPPORT role
        affirmPermission(allowSupportRole.test(context.getAuthenticationContext()), "Not allowed");

        return accountService.findShadowAttributes(AERO_ACCESS, accountId)
                .doOnError(Exception.class, ex -> {
                    throw new IllegalArgumentFault("unexpected null value while determining access");
                })
                .hasElements()
                .map(truthy -> {
                    if (!truthy) {
                        accountService.addShadowAttribute(accountId,
                                                          AERO_ACCESS,
                                                          "true",
                                                          AccountShadowAttributeSource.REQUEST);
                    }
                    return true;
                }).toFuture();
    }
}
