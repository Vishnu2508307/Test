package com.smartsparrow.graphql.schema;

import static com.smartsparrow.annotation.service.Motivation.PUBLISHED_MOTIVATIONS;
import static com.smartsparrow.iam.util.Permissions.affirmPermission;
import static com.smartsparrow.iam.util.Permissions.failPermission;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.annotation.service.Annotation;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.LearnerAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.auth.AllowCoursewareElementContributorOrHigher;
import com.smartsparrow.graphql.auth.AllowCoursewareElementReviewerOrHigher;
import com.smartsparrow.graphql.auth.AllowEnrolledStudent;
import com.smartsparrow.graphql.auth.AllowWorkspaceReviewerOrHigher;
import com.smartsparrow.graphql.type.mutation.AnnotationArg;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.util.UUIDs;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

@Singleton
public class AnnotationSchema {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher;
    private final AllowCohortInstructor allowCohortInstructor;
    private final AllowEnrolledStudent allowEnrolledStudent;
    private final AccountService accountService;
    private final AnnotationService annotationService;
    private final ActivityService activityService;
    private final DeploymentService deploymentService;
    private final AllowCoursewareElementContributorOrHigher allowCoursewareElementContributorOrHigher;
    private final AllowCoursewareElementReviewerOrHigher allowCoursewareElementReviewerOrHigher;

    @Inject
    public AnnotationSchema(AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher,
                            AllowCohortInstructor allowCohortInstructor,
                            AllowEnrolledStudent allowEnrolledStudent,
                            AccountService accountService,
                            AnnotationService annotationService,
                            ActivityService activityService,
                            DeploymentService deploymentService,
                            AllowCoursewareElementContributorOrHigher allowCoursewareElementContributorOrHigher,
                            AllowCoursewareElementReviewerOrHigher allowCoursewareElementReviewerOrHigher) {
        this.allowWorkspaceReviewerOrHigher = allowWorkspaceReviewerOrHigher;
        this.allowCohortInstructor = allowCohortInstructor;
        this.allowEnrolledStudent = allowEnrolledStudent;
        this.accountService = accountService;
        this.annotationService = annotationService;
        this.activityService = activityService;
        this.deploymentService = deploymentService;
        this.allowCoursewareElementContributorOrHigher = allowCoursewareElementContributorOrHigher;
        this.allowCoursewareElementReviewerOrHigher = allowCoursewareElementReviewerOrHigher;
    }

    // AnnotationsByCourseware(rootElementId:UUID!, elementId:UUID, motivation:Motivation!)
    @GraphQLQuery(name = "AnnotationsByCourseware", description = "Find annotations by courseware root element, element and motivation")
    public CompletableFuture<? extends List<? extends Annotation>> getAnnotationsByCourseware(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                           @GraphQLNonNull @GraphQLArgument(name = "rootElementId") final UUID rootElementId,
                                                                                           @GraphQLArgument(name = "elementId") final UUID elementId,
                                                                                           @GraphQLNonNull @GraphQLArgument(name = "motivation") final Motivation motivation) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        //
        // allowed PermissionLevel Reviewer or higher
        //
        affirmPermission(allowCoursewareElementReviewerOrHigher.test(context.getAuthenticationContext(),
                                                                     rootElementId,
                                                                     CoursewareElementType.ACTIVITY));

        UUID accountId = context.getAuthenticationContext().getAccount().getId();
        //
        if (elementId != null) {
            // find with an element id as a specifier
            return annotationService.fetchCoursewareAnnotation(rootElementId, elementId, motivation, accountId)
                    .collectList()
                    .toFuture();
        } else {
            // find all within the deployment
            return annotationService.findCoursewareAnnotation(rootElementId, motivation, accountId)
                    .collectList()
                    .toFuture();
        }
    }

    // mutation AnnotationForCoursewareCreate(rootElementId:UUID!, elementId:UUID!, annotation:Annotation!)
    @GraphQLMutation(name = "AnnotationForCoursewareCreate", description = "Create an annotation within courseware")
    public CompletableFuture<? extends Annotation> createAnnotationForCourseware(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                 @GraphQLNonNull @GraphQLArgument(name = "rootElementId") final UUID rootElementId,
                                                                                 @GraphQLNonNull @GraphQLArgument(name = "elementId") final UUID elementId,
                                                                                 @GraphQLNonNull @GraphQLArgument(name = "annotation") final AnnotationArg annotationArg) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        //
        affirmArgument(annotationArg.getId() == null, "id must not be set");
        affirmNotNull(annotationArg.getMotivation(), "missing motivation");

        //check permission level
        assertPermission(context.getAuthenticationContext(),
                         rootElementId,
                         annotationArg.getMotivation());

        //
        UUID id = UUIDs.timeBased();
        JsonNode bodyNode = parseJson(annotationArg.getBody(), "invalid body json");
        JsonNode targetNode = parseJson(annotationArg.getTarget(), "invalid target json");

        CoursewareAnnotation coursewareAnnotation = new CoursewareAnnotation() //
                .setId(id) //
                .setVersion(id) //
                .setMotivation(annotationArg.getMotivation()) //
                .setRootElementId(rootElementId) //
                .setElementId(elementId) //
                .setBodyJson(bodyNode) //
                .setTargetJson(targetNode) //
                .setCreatorAccountId(context.getAuthenticationContext().getAccount().getId());

        // create and wait.
        return Mono.just(coursewareAnnotation)
                .map(annotation -> {
                    annotationService.create(coursewareAnnotation).subscribe();
                    return annotation;
                })
                .toFuture();

    }

    // mutation AnnotationForCoursewareUpdate(annotation:Annotation!)
    @GraphQLMutation(name = "AnnotationForCoursewareUpdate", description = "Update an annotation within courseware")
    public CompletableFuture<? extends Annotation> updateAnnotationForCourseware(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                 @GraphQLNonNull @GraphQLArgument(name = "annotation") final AnnotationArg annotationArg) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        //
        affirmNotNull(annotationArg.getId(), "missing required annotation id");
        return annotationService.findCoursewareAnnotation(annotationArg.getId())
                .single()
                .doOnError(throwable -> {
                    throw new PermissionFault("Unauthorized");
                })
                .map(coursewareAnnotation -> {
                    assertPermission(context.getAuthenticationContext(),
                                     coursewareAnnotation.getRootElementId(),
                                     coursewareAnnotation.getMotivation());
                    JsonNode bodyNode = parseJson(annotationArg.getBody(), "invalid body json");
                    JsonNode targetNode = parseJson(annotationArg.getTarget(), "invalid target json");
                    CoursewareAnnotation annotation = new CoursewareAnnotation() //
                            .setId(coursewareAnnotation.getId()) // same id
                            .setVersion(UUIDs.timeBased()) // new version
                            .setMotivation(coursewareAnnotation.getMotivation()) // same motivation
                            .setRootElementId(coursewareAnnotation.getRootElementId()) //
                            .setElementId(coursewareAnnotation.getElementId()) //
                            .setBodyJson(bodyNode) //
                            .setTargetJson(targetNode) //
                            .setCreatorAccountId(coursewareAnnotation.getCreatorAccountId());// same creator.
                    annotationService.create(annotation).subscribe();
                    return annotation;
                })
                .doOnError(Mono::error)
                .toFuture();
    }

    @GraphQLMutation(name = "AnnotationForCoursewareDelete", description = "Delete an annotation from within courseware")
    public CompletableFuture<AnnotationArg> deleteAnnotationForCourseware(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                          @GraphQLNonNull @GraphQLArgument(name = "annotationId") final AnnotationArg annotationArg) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        //
        UUID annotationId = annotationArg.getId();
        affirmNotNull(annotationId, "missing required annotation id");
        final Mono<CoursewareAnnotation> annotation = annotationService
                .findCoursewareAnnotation(annotationId)
                .single()
                .doOnError(throwable -> {
                    throw new PermissionFault("Unauthorized");
                });

        return annotation
                .map(coursewareAnnotation -> {
                    if (coursewareAnnotation.getMotivation() != null && coursewareAnnotation.getMotivation().equals(
                            Motivation.identifying)) {
                        affirmPermission(allowCoursewareElementContributorOrHigher.test(context.getAuthenticationContext(),
                                                                                        coursewareAnnotation.getRootElementId(),
                                                                                        CoursewareElementType.ACTIVITY));
                    } else {
                        // check if the creator
                        if (!context.getAuthenticationContext().getAccount().getId().equals(coursewareAnnotation.getCreatorAccountId())) {
                            failPermission();
                        }
                    }
                    annotationService.deleteAnnotation(coursewareAnnotation).subscribe();
                    return annotationArg;
                })
                .doOnError(Mono::error)
                .toFuture();
    }

    // AnnotationsByDeployment(deploymentId:UUID!, motivation:Motivation!)
    @GraphQLQuery(name = "AnnotationsByDeployment", description = "Find deployment annotations by motivation")
    public CompletableFuture<? extends List<? extends Annotation>> getAnnotationsByDeployment(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                    @GraphQLNonNull @GraphQLArgument(name = "deploymentId") final UUID deploymentId,
                                                                                    @GraphQLNonNull @GraphQLArgument(name = "motivation") final Motivation motivation,
                                                                                    @GraphQLArgument(name = "elementId") final UUID elementId) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        // allowed if instructor on the cohort.
        assertInstructorOrEnrolledOnDeployment(context.getAuthenticationContext(), deploymentId);
        // allowed motivations
        assertPublishedMotivation(motivation);

        final Mono<DeployedActivity> deployment = deploymentService.findDeployment(deploymentId)
                .single()
                .doOnError(throwable -> {
                    throw new IllegalArgumentFault("deployment not found");
                });

        return deployment
                .flatMap(deployedActivity -> elementId != null ?
                        // find with an element id as a specifier
                        annotationService.findDeploymentAnnotations(deploymentId,
                                                                    deployedActivity.getChangeId(),
                                                                    motivation,
                                                                    elementId)
                                .collectList() :
                        // find all within the deployment
                        annotationService.findDeploymentAnnotations(deploymentId,
                                                                    deployedActivity.getChangeId(),
                                                                    motivation)
                                .collectList())
                .toFuture();
    }

    // AnnotationsByDeploymentAccount(accountId:UUID!, deploymentId:UUID!, motivation:Motivation!)
    @GraphQLQuery(name = "AnnotationsByDeploymentAccount", description = "Find annotations by deployment, creator and motivation")
    public CompletableFuture<? extends List<? extends Annotation>> getAnnotationsByDeploymentAccount(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                        @GraphQLNonNull @GraphQLArgument(name = "deploymentId") final UUID deploymentId,
                                                                                        @GraphQLNonNull @GraphQLArgument(name = "creatorAccountId") final UUID creatorAccountId,
                                                                                        @GraphQLNonNull @GraphQLArgument(name = "motivation") final Motivation motivation,
                                                                                        @GraphQLArgument(name = "elementId") final UUID elementId) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        // allowed if self
        if (!context.getAuthenticationContext().getAccount().getId().equals(creatorAccountId)) {
            // or instructor on the cohort.
            assertInstructorOnDeployment(context.getAuthenticationContext(), deploymentId);
        }

        //
        if (elementId != null) {
            // find with an element id as a specifier
            return annotationService.findLearnerAnnotation(deploymentId, creatorAccountId, motivation, elementId)
                    .collectList()
                    .toFuture();
        } else {
            // find all within the deployment
            return annotationService.findLearnerAnnotation(deploymentId, creatorAccountId, motivation)
                    .collectList()
                    .toFuture();
        }
    }

    // mutation AnnotationForDeploymentCreate(deploymentId:UUID!, elementId:UUID!, annotation:Annotation!)
    @GraphQLMutation(name = "AnnotationForDeploymentCreate", description = "Create an annotation within a deployment")
    public CompletableFuture<? extends Annotation> createAnnotationForDeployment(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                              @GraphQLNonNull @GraphQLArgument(name = "deploymentId") final UUID deploymentId,
                                                                              @GraphQLNonNull @GraphQLArgument(name = "elementId") final UUID elementId,
                                                                              @GraphQLNonNull @GraphQLArgument(name = "annotation") final AnnotationArg annotationArg) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        // allowed if enrolled or instructor on the cohort.
        assertInstructorOrEnrolledOnDeployment(context.getAuthenticationContext(), deploymentId);

        //
        UUID id = UUIDs.timeBased();
        JsonNode bodyNode = parseJson(annotationArg.getBody(), "invalid body json");
        JsonNode targetNode = parseJson(annotationArg.getTarget(), "invalid target json");

        LearnerAnnotation learnerAnnotation = new LearnerAnnotation() //
                .setId(id) //
                .setVersion(id) //
                .setMotivation(annotationArg.getMotivation()) //
                .setDeploymentId(deploymentId) //
                .setElementId(elementId) //
                .setBodyJson(bodyNode) //
                .setTargetJson(targetNode) //
                .setCreatorAccountId(context.getAuthenticationContext().getAccount().getId());

        return Mono
                .just(learnerAnnotation)
                .map(annotation -> {
                    annotationService.create(learnerAnnotation).subscribe();
                    return annotation;
                })
                .toFuture();
    }

    // mutation AnnotationForDeploymentUpdate(annotation:Annotation!)
    @GraphQLMutation(name = "AnnotationForDeploymentUpdate", description = "Update an annotation within a deployment")
    public CompletableFuture<? extends Annotation> updateAnnotationForDeployment(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                              @GraphQLNonNull @GraphQLArgument(name = "annotation") final AnnotationArg annotationArg) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        //
        UUID annotationId = annotationArg.getId();
        affirmNotNull(annotationId, "missing required annotation id");
        Mono<LearnerAnnotation> annotation = annotationService
                .findLearnerAnnotation(annotationId)
                .single()
                .doOnError(throwable -> {
                    throw new PermissionFault("Unauthorized");
                });

        return annotation.map(learnerAnnotation -> {
                    if (!context.getAuthenticationContext().getAccount().getId().equals(learnerAnnotation.getCreatorAccountId())) {
                        // or instructor on the cohort.
                        assertInstructorOnDeployment(context.getAuthenticationContext(), learnerAnnotation.getDeploymentId());
                    }
                    JsonNode bodyNode = parseJson(annotationArg.getBody(), "invalid body json");
                    JsonNode targetNode = parseJson(annotationArg.getTarget(), "invalid target json");
                    LearnerAnnotation anno = new LearnerAnnotation() //
                            .setId(learnerAnnotation.getId()) // keep the same id.
                            .setVersion(UUIDs.timeBased()) // update the version
                            .setMotivation(learnerAnnotation.getMotivation()) // keep the original motivation
                            .setDeploymentId(learnerAnnotation.getDeploymentId()) //
                            .setElementId(learnerAnnotation.getElementId()) //
                            .setBodyJson(bodyNode) //
                            .setTargetJson(targetNode) //
                            .setCreatorAccountId(learnerAnnotation.getCreatorAccountId()); // keep the original creator.

                    annotationService.create(anno).subscribe();
                    return learnerAnnotation;
                })
                .toFuture();
    }

    @GraphQLMutation(name = "AnnotationForDeploymentDelete", description = "Delete an annotation from within a deployment")
    public CompletableFuture<AnnotationArg> deleteAnnotationForDeployment(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                          @GraphQLNonNull @GraphQLArgument(name = "annotationId") final AnnotationArg annotationArg) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        //
        UUID annotationId = annotationArg.getId();
        affirmNotNull(annotationId, "missing required annotation id");
        final Mono<LearnerAnnotation> annotation = annotationService
                .findLearnerAnnotation(annotationId)
                .single()
                .doOnError(throwable -> {
                    throw new PermissionFault("Unauthorized");
                });

        return annotation
                .map(learnerAnnotation -> {
                    // allowed if the creator of the annotation
                    if (!context.getAuthenticationContext().getAccount().getId().equals(learnerAnnotation.getCreatorAccountId())) {
                        // or instructor on the cohort
                        assertInstructorOnDeployment(context.getAuthenticationContext(),
                                                     learnerAnnotation.getDeploymentId());
                    }
                    annotationService.deleteAnnotation(learnerAnnotation).subscribe();
                    return annotationArg;
                })
                .toFuture();
    }

    // expose a "creator" field within an Annotation.
    @GraphQLQuery(name = "creator")
    public CompletableFuture<Account> getCreator(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                 @GraphQLContext final Annotation annotation) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        //
        // allowed if self or
        //  + (LearnerAnnotation) instructor on the cohort.
        //  + (CoursewareAnnotation) rights on the courseware.
        //
        if (!context.getAuthenticationContext().getAccount().getId().equals(annotation.getCreatorAccountId())) {
            // ugh. this is ugly.
            if (annotation instanceof LearnerAnnotation) {
                UUID deploymentId = ((LearnerAnnotation) annotation).getDeploymentId();
                assertInstructorOnDeployment(context.getAuthenticationContext(), deploymentId);
            } else if (annotation instanceof CoursewareAnnotation) {
                UUID rootElementId = ((CoursewareAnnotation) annotation).getRootElementId();
                assertWorkspaceReviewerOrHigher(context.getAuthenticationContext(), rootElementId);
            } else {
                // fail.
                failPermission();
            }
        }

        return accountService
                .findById(annotation.getCreatorAccountId())
                .single()
                .toFuture();
    }

    /**
     * Parse the supplied JSON to a node.
     *
     * @param value the value to parse
     * @param errorMessage the error message if an IllegalArgumentFault is raised
     * @return the parsed json node
     * @throws IllegalArgumentFault if an underlying IOException or Parsing Exception occurs
     */
    JsonNode parseJson(final String value, final String errorMessage) {
        try {
            return mapper.readTree(value);
        } catch (IOException e) {
            throw new IllegalArgumentFault(errorMessage);
        }
    }

    /**
     * Check that the calling user has workspace reviewer or higher on the supplied courseware element
     *
     * @param coursewareElementId the target courseware element id
     */
    void assertWorkspaceReviewerOrHigher(AuthenticationContext authenticationContext, final UUID coursewareElementId) {

        affirmNotNull(coursewareElementId, "missing required argument");

        UUID workspaceId = activityService.findWorkspaceIdByActivity(coursewareElementId).block();
        // check this isn't null, preempting the IllegalArgumentFault in the .test()
        affirmPermission(workspaceId != null);
        affirmPermission(allowWorkspaceReviewerOrHigher.test(authenticationContext, workspaceId));
    }

    /**
     * Check that the caller is a cohort instructor, given the deployment id
     *
     * @param deploymentId the deployment id
     */
    void assertInstructorOnDeployment(AuthenticationContext authenticationContext, final UUID deploymentId) {
        affirmNotNull(deploymentId, "missing required argument");

        try {
            final DeployedActivity deployment = deploymentService.findDeployment(deploymentId).block();
            affirmPermission(deployment != null);
            final UUID cohortId = deployment.getCohortId();
            affirmPermission(cohortId != null);
            //
            affirmPermission(allowCohortInstructor.test(authenticationContext, cohortId));
        } catch (DeploymentNotFoundException e) {
            failPermission();
        }
    }

    /**
     * Check the the caller is a cohort instructor or an enrolled student.
     *
     * @param deploymentId the deployment id
     */
    void assertInstructorOrEnrolledOnDeployment(AuthenticationContext authenticationContext, final UUID deploymentId) {
        affirmNotNull(deploymentId, "missing required argument");

        try {
            final DeployedActivity deployment = deploymentService.findDeployment(deploymentId).block();
            affirmPermission(deployment != null);
            final UUID cohortId = deployment.getCohortId();
            affirmPermission(cohortId != null);
            //
            affirmPermission(allowEnrolledStudent.test(authenticationContext, cohortId) ||
                                     allowCohortInstructor.test(authenticationContext, cohortId));
        } catch (DeploymentNotFoundException e) {
            failPermission();
        }
    }

    /**
     * Check the caller has permission to perform mutations on courseware element
     *
     * @param rootElementId the root element id
     * @param motivation the motivation object
     */
    private void assertPermission(AuthenticationContext authenticationContext,
                                  UUID rootElementId,
                                  Motivation motivation) {
        if (motivation != null && motivation.equals(Motivation.identifying)) {
            affirmPermission(allowCoursewareElementContributorOrHigher.test(authenticationContext,
                                                                            rootElementId,
                                                                            CoursewareElementType.ACTIVITY));
        } else {
            affirmPermission(allowCoursewareElementReviewerOrHigher.test(authenticationContext,
                                                                         rootElementId,
                                                                         CoursewareElementType.ACTIVITY));
        }
    }

    /**
     * Check the motivation is allowed to be listed
     *
     * @param motivation the motivation object
     */
    private void assertPublishedMotivation(Motivation motivation) {
        if (motivation != null) {
            affirmArgument(PUBLISHED_MOTIVATIONS.contains(motivation), "motivation provided is not permitted");
        }
    }
}
