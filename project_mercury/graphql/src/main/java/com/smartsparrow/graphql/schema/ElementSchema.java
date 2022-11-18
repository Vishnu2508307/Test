package com.smartsparrow.graphql.schema;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.iam.util.Permissions.affirmPermission;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.auth.AllowEnrolledStudent;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerActivityPayload;
import com.smartsparrow.learner.data.LearnerInteractivePayload;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.data.LearnerWalkablePayload;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.learner.service.LearnerActivityService;
import com.smartsparrow.learner.service.LearnerCoursewareService;
import com.smartsparrow.learner.service.LearnerInteractiveService;
import com.smartsparrow.learner.service.LearnerService;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ElementSchema {

    private static final String ERROR_MESSAGE = "Unauthorized";

    private final AllowCohortInstructor allowCohortInstructor;
    private final AllowEnrolledStudent allowEnrolledStudent;
    //
    private final LearnerActivityService learnerActivityService;
    private final LearnerInteractiveService learnerInteractiveService;
    private final LearnerCoursewareService learnerCoursewareService;
    private final LearnerService learnerService;

    @Inject
    public ElementSchema(final AllowCohortInstructor allowCohortInstructor,
                         final AllowEnrolledStudent allowEnrolledStudent,
                         final LearnerActivityService learnerActivityService,
                         final LearnerInteractiveService learnerInteractiveService,
                         final LearnerCoursewareService learnerCoursewareService,
                         final LearnerService learnerService,
                         final DeploymentService deploymentService,
                         final CohortService cohortService) {
        this.allowCohortInstructor = allowCohortInstructor;
        this.allowEnrolledStudent = allowEnrolledStudent;
        this.learnerActivityService = learnerActivityService;
        this.learnerInteractiveService = learnerInteractiveService;
        this.learnerCoursewareService = learnerCoursewareService;
        this.learnerService = learnerService;
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Element.element")
    @GraphQLQuery(name = "element", description = "Learner element data")
    public CompletableFuture<LearnerWalkablePayload> getElement(@GraphQLContext DeployedActivity deployment,
                                                                @GraphQLArgument(name = "elementId",
                                                                        description = "Fetch walkable with specific id") UUID elementId) {
        affirmArgument(deployment != null, "deployment context is required");
        affirmArgument(elementId != null, "elementId is required");

        return learnerService.findElementByDeployment(elementId, deployment.getId())
                .flatMap(element -> {
                    switch (element.getElementType()) {
                        case ACTIVITY:
                            return learnerActivityService.findActivity(elementId, deployment.getId())
                                    .map(activity -> new LearnerActivityPayload(activity));
                        case INTERACTIVE:
                            return learnerInteractiveService.findInteractive(elementId, deployment.getId())
                                    .map(interactive -> new LearnerInteractivePayload(interactive));
                        default:
                            return Mono.error(new UnsupportedOperationException("Unsupported courseware element type " + element.getElementType()));
                    }
                }).doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    /**
     * Provide an ancestry field to LearnerInteractive objects
     *
     * @return a List of Courseware Walkables
     */
    @SuppressWarnings("Duplicates")
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Element.ancestry")
    @GraphQLQuery(name = "ancestryWalkables", description = "Get ancestry to the root courseware element")
    public CompletableFuture<List<LearnerActivity>> getAncestry(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                @GraphQLContext LearnerWalkable learnerwalkable,
                                                                @GraphQLArgument(name = "cohortId",
                                                                          description = "Fetch walkable within cohort") UUID cohortId) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        affirmPermission(allowEnrolledStudent.test(context.getAuthenticationContext(), cohortId) ||
                                 allowCohortInstructor.test(context.getAuthenticationContext(), cohortId),
                         ERROR_MESSAGE);

        /*
         * return a list of walkable objects from the parent up to the root.
         * If it is the root node, naturally return an empty list
         */
        return learnerCoursewareService
                .getAncestry(learnerwalkable.getDeploymentId(), //
                             learnerwalkable.getId(), //
                             learnerwalkable.getElementType())
                .flatMapIterable(item -> item)
                .filter(item -> !learnerwalkable.getId().equals(item.getElementId()))
                .filter(item -> !item.getElementType().equals(CoursewareElementType.PATHWAY))
                .flatMap(item -> {
                    if (item.getElementType() == ACTIVITY) {
                        return learnerActivityService.findActivity(item.getElementId(),
                                                                   learnerwalkable.getDeploymentId());
                    }
                    return Mono.error(new UnsupportedOperationException("Unsupported courseware element type " + item.getElementType()));
                })
                .collectList()//
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }


    /**
     * Provide an ancestry field to LearnerWalkable objects
     *
     * @param walkable the context
     * @return a List of CoursewareElements
     */
    @SuppressWarnings("Duplicates")
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Element.elementAncestry")
    @GraphQLQuery(name = "elementAncestry", description = "Get Learner Element ancestry")
    public CompletableFuture<List<CoursewareElement>> getAncestry(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                  @GraphQLContext LearnerWalkablePayload walkable,
                                                                  @GraphQLArgument(name = "cohortId",
                                                                          description = "Fetch walkable within cohort") UUID cohortId){
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        affirmPermission(allowEnrolledStudent.test(context.getAuthenticationContext(), cohortId) ||
                                 allowCohortInstructor.test(context.getAuthenticationContext(), cohortId),
                         ERROR_MESSAGE);

        /*
         * return a list of element id & element type objects from the parent up to the root.
         * If it is the root node, naturally return an empty list
         */

        Mono<List<CoursewareElement>> ancestryListMono = learnerCoursewareService
                .getAncestry(walkable.getDeploymentId(),//
                             walkable.getId(),//
                             walkable.getElementType()) //
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext());

        return  ancestryListMono
                .flatMapIterable(item -> item)
                .filter(item -> !walkable.getId().equals(item.getElementId()))
                .collectList()
                .toFuture();
    }
}
