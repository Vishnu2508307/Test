package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.map.HashedMap;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.courseware.data.CoursewareElementAncestry;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.auth.AllowEnrolledStudent;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.Evaluation;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.learner.service.EvaluationResultService;
import com.smartsparrow.learner.service.LearnerCoursewareService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;

@Singleton
public class DeploymentSchema {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeploymentSchema.class);

    private final DeploymentService deploymentService;
    private final EvaluationResultService evaluationResultService;
    private final LearnerCoursewareService learnerCoursewareService;
    private final AllowCohortInstructor allowCohortInstructor;
    private final AllowEnrolledStudent allowEnrolledStudent;

    @Inject
    public DeploymentSchema(DeploymentService deploymentService,
                            EvaluationResultService evaluationResultService,
                            LearnerCoursewareService learnerCoursewareService,
                            AllowCohortInstructor allowCohortInstructor,
                            AllowEnrolledStudent allowEnrolledStudent) {
        this.deploymentService = deploymentService;
        this.evaluationResultService = evaluationResultService;
        this.learnerCoursewareService = learnerCoursewareService;
        this.allowCohortInstructor = allowCohortInstructor;
        this.allowEnrolledStudent = allowEnrolledStudent;
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Deployment.deployment")
    @GraphQLQuery(name = "deployment", description = "Cohort deployment")
    public CompletableFuture<List<DeployedActivity>> getCohortDeployment(@GraphQLContext CohortSummary cohortSummary,
                                                                         @GraphQLArgument(name = "deploymentId", description = "Fetch a deployment with specific id") UUID deploymentId) {
        if (deploymentId == null) {
            return deploymentService.findDeployments(cohortSummary.getId())
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .collectList()
                    .toFuture();

            // for class on-demand cohorts, these DeployedActivities would point to the 'template'
            // cohort id (which may not necessarily be the cohort id that should be used downstream)
            // however, if these DeployedActivity ids are passed in this same GraphQL call, the code block
            // below would be called instead, and the cohort id would be properly updated
        } else {
            return deploymentService.findDeployment(deploymentId)
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .single()
                    .doOnError(throwable -> {
                        throw new IllegalArgumentFault("Deployment does not exist");
                    })
                    .map(deployedActivity -> {
                        if (!cohortSummary.getId().equals(deployedActivity.getCohortId())) {
                            // for class on-demand cohorts, the DeployedActivity cohort id points to the 'template'
                            // cohort (created during the publishing stage), not an on-demand 'instance' cohort (which
                            // are created during the LTI launch stage), so this might still be a valid DeployedActivity
                            // for this GraphQL CohortSummary context; let's validate this scenario (below)

                            // fetch all deployment ids based on the on-demand 'instance' cohort id
                            List<UUID> deploymentIds = deploymentService.findDeploymentIds(cohortSummary.getId())
                                    .collectList()
                                    .block();

                            // check if the DeployedActivity (deployment) id is in the deploymentIds-by-cohortId list
                            if (!deploymentIds.contains(deployedActivity.getId())) {
                                throw new IllegalArgumentFault("Deployment does not exist");
                            }

                            log.jsonInfo("overwriting cohort id in deployed activity for on-demand flow", new HashedMap<String, Object>() {
                                {
                                    put("cohortSummaryId", cohortSummary.getId());
                                    put("deployedActivityCohortId", deployedActivity.getCohortId());
                                }
                            });

                            // need to use the on-demand 'instance' cohort id in subsequent GraphQL calls (eg,
                            // 'getLearnerAncestry' permission check), so replace on-demand 'template' cohort id with
                            // the 'instance' cohort id in the DeployedActivity
                            deployedActivity.setCohortId(cohortSummary.getId());
                        }
                        return deployedActivity;
                    })
                    .flux()
                    .collectList()
                    .toFuture();
        }
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Deployment.evaluation")
    @GraphQLQuery(name = "evaluation", description = "The evaluation data for a completed walkable")
    public CompletableFuture<Evaluation> getEvaluation(@GraphQLContext DeployedActivity deployedActivity,
                                                       @GraphQLArgument(name = "evaluationId", description = "Fetch an evaluation with specific id") UUID evaluationId) {

        affirmArgument(evaluationId != null, "evaluationId is required");

        return evaluationResultService.fetch(evaluationId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    @GraphQLQuery(name = "getLearnerAncestry", description = "fetch the ancestry and element type given an id")
    public CompletableFuture<CoursewareElementAncestry> getLearnerAncestry(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                           @GraphQLContext DeployedActivity deployedActivity,
                                                                           @GraphQLArgument(name = "elementId", description = "the element id to find the ancestry for")
                                                                                   UUID elementId) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        final UUID cohortId = deployedActivity.getCohortId();
        // allow an instructor or an enrolled student to fetch the ancestry
        affirmPermission(allowEnrolledStudent.test(context.getAuthenticationContext(), cohortId) ||
                                 allowCohortInstructor.test(context.getAuthenticationContext(), cohortId),
                         "Not allowed");
        return learnerCoursewareService.findCoursewareElementAncestry(elementId, deployedActivity.getId())
                .toFuture();
    }
}
