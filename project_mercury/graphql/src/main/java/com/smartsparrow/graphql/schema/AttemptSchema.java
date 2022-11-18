package com.smartsparrow.graphql.schema;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.service.AcquireAttemptService;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;

@Singleton
public class AttemptSchema {

    private final AcquireAttemptService acquireAttemptService;

    @Inject
    public AttemptSchema(AcquireAttemptService acquireAttemptService) {
        this.acquireAttemptService = acquireAttemptService;
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Attempt.attempt")
    @GraphQLQuery(name = "attempt", description = "The latest attempt")
    public CompletableFuture<Attempt> getAttempt(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                 @GraphQLContext LearnerWalkable walkable) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        UUID studentId = context.getAuthenticationContext().getAccount().getId();

        return acquireAttemptService.acquireLatestAttempt(walkable.getDeploymentId(), walkable.getId(),
                walkable.getElementType(), studentId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }
}
