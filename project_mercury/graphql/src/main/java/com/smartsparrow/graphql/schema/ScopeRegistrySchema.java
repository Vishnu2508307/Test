package com.smartsparrow.graphql.schema;

import com.smartsparrow.graphql.service.GraphQLPageFactory;
import com.smartsparrow.learner.data.LearnerScopeReference;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.service.LearnerService;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Singleton
public class ScopeRegistrySchema {

    private final LearnerService learnerService;

    @Inject
    public ScopeRegistrySchema(LearnerService learnerService) {
        this.learnerService = learnerService;
    }

    @GraphQLQuery(name = "scopeRegistry", description = "scope Registry data")
    public CompletableFuture<Page<LearnerScopeReference>> getScopeRegistry(@GraphQLContext LearnerWalkable learnerWalkable,
                                                                           @GraphQLArgument(name = "before",
                                                                description = "fetching only nodes before this node (exclusive)") String before,
                                                                           @GraphQLArgument(name = "last",
                                                                description = "fetching only the last certain number of nodes") Integer last) {
        Mono<List<LearnerScopeReference>> learnerScopeReferences = learnerService
                .findAllRegistered(learnerWalkable.getStudentScopeURN(),
                                   learnerWalkable.getDeploymentId(),
                                   learnerWalkable.getChangeId())
                        .collectList();

        return GraphQLPageFactory.createPage(learnerScopeReferences, before, last).toFuture();
    }
}
