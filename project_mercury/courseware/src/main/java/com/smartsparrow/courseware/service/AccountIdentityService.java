package com.smartsparrow.courseware.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.data.CredentialsType;
import com.smartsparrow.iam.payload.AccountSummaryPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.sso.service.IESService;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;

@Singleton
public class AccountIdentityService {

    private final IESService iesService;
    private final AccountService accountService;
    private final CredentialService credentialService;


    @Inject
    public AccountIdentityService(final IESService iesService,
                                  final AccountService accountService,
                                  final CredentialService credentialService) {
        this.iesService = iesService;
        this.accountService = accountService;
        this.credentialService = credentialService;
    }

    /**
     * Fetch account summary details based on authentication type
     * @param accountIds list of account ids
     * @return flux of account summary payload object
     */
    @Trace(async = true)
    public Flux<AccountSummaryPayload> fetchAccountSummaryPayload(List<UUID> accountIds) {
        return fetchCredentialsType(accountIds)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .flatMap(credentialsType -> {
                    UUID accountId = credentialsType.getAccountId();
                    AuthenticationType authenticationType = credentialsType.getAuthenticationType();
                    switch (authenticationType) {
                        case BRONTE:
                            return accountService.getAccountSummaryPayload(accountId)
                                    .doOnEach(ReactiveTransaction.linkOnNext());
                        case IES:
                            return fetchIesAccountSummary(accountId)
                                    .doOnEach(ReactiveTransaction.linkOnNext());
                        case LTI:
                            return fetchIesAccountSummary(accountId)
                                    .doOnEach(ReactiveTransaction.linkOnNext());
                        default:
                            throw new UnsupportedOperationException("Unsupported credential type " + authenticationType);
                    }
                });
    }

    /**
     * fetch ies account summary payload
     * @param accountId the account id
     * @return flux of account summary payload
     */
    @Trace(async = true)
    private Flux<AccountSummaryPayload> fetchIesAccountSummary(UUID accountId) {
        return iesService.findIESId(accountId)
                .flux()
                .flatMap(iesAccountTracking ->
                                 iesService.getAccountSummaryPayload(Arrays.asList(iesAccountTracking)))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * fetch credentials type for list of account ids
     * @param accountIds list of account ids
     * @return flux of credentials type object
     */
    @Trace(async = true)
    private Flux<CredentialsType> fetchCredentialsType(List<UUID> accountIds) {
        return accountIds
                .stream()
                .map(accountId -> credentialService.fetchCredentialTypeByAccount(accountId))
                .reduce((prev, next) -> Flux.merge(prev, next))
                .orElse(Flux.empty())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
