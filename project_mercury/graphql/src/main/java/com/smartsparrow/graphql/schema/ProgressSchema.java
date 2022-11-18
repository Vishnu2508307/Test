package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.type.EnrollmentLearnerActivity;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.data.Score;
import com.smartsparrow.learner.lang.ProgressNotFoundFault;
import com.smartsparrow.learner.progress.Completion;
import com.smartsparrow.learner.progress.GeneralProgress;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.service.AcquireAttemptService;
import com.smartsparrow.learner.service.ProgressService;
import com.smartsparrow.learner.service.StudentScoreService;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

@Singleton
public class ProgressSchema {

    private final AllowCohortInstructor allowCohortInstructor;
    //
    private final ProgressService progressService;
    private final AcquireAttemptService acquireAttemptService;
    private final StudentScoreService studentScoreService;

    @Inject
    public ProgressSchema(AllowCohortInstructor allowCohortInstructor,
                          ProgressService progressService,
                          AcquireAttemptService acquireAttemptService,
                          StudentScoreService studentScoreService) {
        this.allowCohortInstructor = allowCohortInstructor;
        this.progressService = progressService;
        this.acquireAttemptService = acquireAttemptService;
        this.studentScoreService = studentScoreService;
    }

    @GraphQLQuery(name = "progress", description = "The latest progress")
    public CompletableFuture<Progress> getProgress(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                   @GraphQLContext LearnerWalkable walkable) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        UUID studentId = context.getAuthenticationContext().getAccount().getId();

        return acquireAttemptService.acquireLatestAttempt(walkable.getDeploymentId(), walkable.getId(), walkable.getElementType(), studentId)
                .flatMap(attempt -> progressService.findLatest(walkable.getDeploymentId(), walkable.getId(), studentId)
                        .onErrorResume(ProgressNotFoundFault.class, ex -> Mono.empty())
                        .filter(progress -> attempt.getId().equals(progress.getAttemptId())))
                .defaultIfEmpty(new GeneralProgress().setCompletion(new Completion().setValue(0f).setConfidence(0f)))
                .toFuture();
    }

    @GraphQLQuery(name = "progress", description = "Find the latest progress for the enrollment")
    public CompletableFuture<Progress> getProgress(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                   @GraphQLContext EnrollmentLearnerActivity enrollmentLearnerActivity) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        // only allow instructors to request this data as it is part of the Cohort Enrollment context.
        affirmPermission(allowCohortInstructor.test(context.getAuthenticationContext(),
                                                    enrollmentLearnerActivity.getEnrollment().getCohortId()),
                         "Unauthorized");

        UUID deploymentId = enrollmentLearnerActivity.getLearnerActivity().getDeploymentId();
        UUID walkableId = enrollmentLearnerActivity.getLearnerActivity().getId();
        UUID studentId = enrollmentLearnerActivity.getEnrollment().getAccountId();

        return progressService.findLatest(deploymentId, walkableId, studentId)
                .defaultIfEmpty(new GeneralProgress().setCompletion(new Completion().setValue(0f).setConfidence(0f)))
                // suppress any not found data exceptions.
                .onErrorResume(ProgressNotFoundFault.class, ex -> Mono.empty())
                .toFuture();
    }


    @Trace(dispatcher = true, nameTransaction = false, metricName = "Progress.score")
    @GraphQLQuery(name = "score", description = "get the learner walkable latest attempt score for the enrolled student")
    public CompletableFuture<Score> getScore(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                             @GraphQLContext final Progress progress,
                                             @GraphQLArgument(name = "deploymentId",
                                                     description = "Fetch score with specific deployment id ") UUID deploymentId,
                                             @GraphQLArgument(name = "elementId",
                                                     description = "Fetch score with specific element id ") UUID elementId) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        final UUID studentId = context.getAuthenticationContext().getAccount().getId();

        return studentScoreService.computeScore(deploymentId, studentId, elementId,
                                                null)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

}
