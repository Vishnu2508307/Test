package com.smartsparrow.graphql.schema;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.Claim;
import com.smartsparrow.iam.service.ClaimService;

import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

@Singleton
public class IAMClaimSchema {

    private final ClaimService claimService;

    @Inject
    public IAMClaimSchema(ClaimService claimService) {
        this.claimService = claimService;
    }

    @GraphQLQuery(name = "claims", description = "claims provided by a third party authentication provider")
    public CompletableFuture<Map<String, String>> getClaims(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                            @GraphQLContext AccountIdentityAttributes identityAttributes) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        AuthenticationContext authenticationContext = context.getAuthenticationContext();
        UUID authoritySubscriptionId = authenticationContext.getWebSessionToken() != null ?
                authenticationContext.getWebSessionToken().getAuthoritySubscriptionId() : null;

        //
        if(authoritySubscriptionId == null) {
            Map<String, String> emptyMap = Collections.emptyMap();
            // an authentication was made via the SPR login;
            return Mono.just(emptyMap).toFuture();
        }

        return claimService.find(authenticationContext.getAccount().getId(), authoritySubscriptionId)
                .collectMap(Claim::getName, Claim::getValue)
                .toFuture();
    }

}
