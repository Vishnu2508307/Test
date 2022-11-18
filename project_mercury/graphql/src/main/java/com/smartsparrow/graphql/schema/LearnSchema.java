package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowLearnspaceRoles;
import com.smartsparrow.graphql.type.Learn;

import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

@Singleton
public class LearnSchema {

    private final AllowLearnspaceRoles allowLearnspaceRoles;

    @Inject
    public LearnSchema(AllowLearnspaceRoles allowLearnspaceRoles) {
        this.allowLearnspaceRoles = allowLearnspaceRoles;
    }

    /**
     * This is just an entry point for learn queries.
     * Check that user has access to Learnspace.
     * @return {@link CompletableFuture<Learn>} a completable future of Learn
     */
    @GraphQLQuery(name = "learn", description = "Learn entry point")
    public CompletableFuture<Learn> getLearn(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        affirmPermission(allowLearnspaceRoles.test(context.getAuthenticationContext()),
                         "User does not have required roles for learnspace");

        return Mono.just(new Learn()).toFuture();
    }
}
