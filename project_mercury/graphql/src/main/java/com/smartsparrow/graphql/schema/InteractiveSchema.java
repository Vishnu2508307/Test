package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementMetaInformation;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.auth.AllowEnrolledStudent;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.Score;
import com.smartsparrow.learner.data.StudentManualGradeReport;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.service.CoursewareElementMetaInformationService;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.learner.service.LearnerCoursewareService;
import com.smartsparrow.learner.service.LearnerInteractiveService;
import com.smartsparrow.learner.service.ManualGradeService;
import com.smartsparrow.learner.service.StudentScoreService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

@Singleton
public class InteractiveSchema {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(InteractiveSchema.class);

    private static final String ERROR_MESSAGE = "Unauthorized";

    private final AllowCohortInstructor allowCohortInstructor;
    private final AllowEnrolledStudent allowEnrolledStudent;
    //
    private final LearnerInteractiveService learnerInteractiveService;
    private final DeploymentService deploymentService;
    private final LearnerCoursewareService learnerCoursewareService;
    private final ManualGradeService manualGradeService;
    private final StudentScoreService studentScoreService;
    private final CoursewareService coursewareService;
    private final CoursewareElementMetaInformationService coursewareElementMetaInformationService;

    @Inject
    public InteractiveSchema(final AllowCohortInstructor allowCohortInstructor,
                             final AllowEnrolledStudent allowEnrolledStudent,
                             final LearnerInteractiveService learnerInteractiveService,
                             final DeploymentService deploymentService,
                             final LearnerCoursewareService learnerCoursewareService,
                             final ManualGradeService manualGradeService,
                             final StudentScoreService studentScoreService,
                             final CoursewareService coursewareService,
                             final CoursewareElementMetaInformationService coursewareElementMetaInformationService) {
        this.allowCohortInstructor = allowCohortInstructor;
        this.allowEnrolledStudent = allowEnrolledStudent;
        this.learnerInteractiveService = learnerInteractiveService;
        this.deploymentService = deploymentService;
        this.learnerCoursewareService = learnerCoursewareService;
        this.manualGradeService = manualGradeService;
        this.studentScoreService = studentScoreService;
        this.coursewareService = coursewareService;
        this.coursewareElementMetaInformationService = coursewareElementMetaInformationService;
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Interactive.interactive")
    @GraphQLQuery(name = "interactive", description = "Learner interactive data")
    public CompletableFuture<LearnerInteractive> getInteractive(@GraphQLContext DeployedActivity deployment,
                                                                @GraphQLArgument(name = "interactiveId",
                                                                        description = "Fetch interactive with specific id") UUID interactiveId) {
        affirmArgument(deployment != null, "deployment context is required");
        affirmArgument(interactiveId != null, "interactiveId is required");

        return learnerInteractiveService
                .findInteractive(interactiveId, deployment.getId())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    /**
     * Provide an ancestry field to LearnerInteractive objects
     *
     * @param learnerInteractive the context
     * @return a List of Courseware Elements
     */
    @SuppressWarnings("Duplicates")
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Interactive.ancestry")
    @GraphQLQuery(name = "ancestry", description = "Get ancestry to the root courseware element")
    public CompletableFuture<List<CoursewareElement>> getAncestry(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                  @GraphQLContext LearnerInteractive learnerInteractive,
                                                                  @Nullable @GraphQLArgument(name = "cohortId", description = "Fetch a cohort with specific id") UUID cohortId) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        // using the deployment id to get the cohort id if the cohort id argument is not available in graphql query
        // TODO: remove it when cohort id is available in graphql argument
        if (cohortId == null) {
            cohortId = deploymentService.findDeployment(learnerInteractive.getDeploymentId())
                    .doOnError(DeploymentNotFoundException.class, ex -> {
                        throw new PermissionFault(ERROR_MESSAGE);
                    })
                    .map(deployedActivity -> deployedActivity.getCohortId()).block();
        }

        affirmPermission(allowEnrolledStudent.test(context.getAuthenticationContext(), cohortId) ||
                                 allowCohortInstructor.test(context.getAuthenticationContext(), cohortId), ERROR_MESSAGE);

        /*
         * return a list of element id & element type objects from the parent up to the root.
         * If it is the root node, naturally return an empty list
         */
        Mono<List<CoursewareElement>> ancestry = learnerCoursewareService
                .getAncestry(learnerInteractive.getDeploymentId(), //
                             learnerInteractive.getId(), //
                             learnerInteractive.getElementType()) //
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext());

        return ancestry
                .flatMapIterable(item -> item)
                .filter(item -> !learnerInteractive.getId().equals(item.getElementId()))
                .collectList()
                .toFuture();
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Interactive.getLatestAttemptInteractiveManualGradeReport")
    @GraphQLQuery(name = "getLatestAttemptInteractiveManualGradeReport", description = "fetch the manual grade reports for a student on all the manual component descendants of this interactive")
    public CompletableFuture<List<StudentManualGradeReport>> getManualGradeReports(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                   @GraphQLContext final CohortEnrollment cohortEnrollment,
                                                                                   @GraphQLArgument(name = "deploymentId", description = "the deployment id the interactive belongs to")
                                                                                           UUID deploymentId,
                                                                                   @GraphQLArgument(name = "interactiveId", description = "the interactive id to find the components for")
                                                                                           UUID interactiveId) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        // only allow instructors to request this data as it is part of the Cohort Enrollment context.
        affirmPermission(allowCohortInstructor.test(context.getAuthenticationContext(),
                                                    cohortEnrollment.getCohortId()),
                         "Unauthorized");

        final UUID studentId = cohortEnrollment.getAccountId();

        // this is a temporary behaviour this api will undergo changes once https://jira.smartsparrow.com/browse/PLT-5393
        // is implemented
        Mono<DeployedActivity> deployment = deploymentService.findDeployment(deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .single()
                .doOnError(e -> {
                    throw new IllegalArgumentFault(String.format("deployment with id %s not found", deploymentId));
                });

        return deployment
                .flatMapMany(deployedActivity -> manualGradeService
                        .findLatestAttemptManualGradeReport(deploymentId,
                                                            deployedActivity.getChangeId(),
                                                            interactiveId,
                                                            studentId))
                .collectList()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Interactive.interactiveScore")
    @GraphQLQuery(name = "interactiveScore", description = "get the learner interactive latest attempt score for the enrolled student")
    public CompletableFuture<Score> getScore(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                             @GraphQLContext final CohortEnrollment cohortEnrollment,
                                             @GraphQLArgument(name = "deploymentId", description = "the deployment id the interactive belongs to")
                                                     UUID deploymentId,
                                             @GraphQLArgument(name = "interactiveId", description = "the interactive id to get the score for")
                                                     UUID interactiveId) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        // only allow instructors to request this data as it is part of the Cohort Enrollment context.
        affirmPermission(allowCohortInstructor.test(context.getAuthenticationContext(),
                                                    cohortEnrollment.getCohortId()),
                         "Unauthorized");

        final UUID studentId = cohortEnrollment.getAccountId();

        return studentScoreService.computeScore(deploymentId, studentId, interactiveId,
                                                null)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    /**
     * Fetch a list of configuration fields given their names for an interactive element
     *
     * @param interactive the context
     * @param fieldNames the name of the fields to find
     * @return a list of configuration fields
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Interactive.coursewareConfigurationFields")
    @GraphQLQuery(name = "coursewareConfigurationFields", description = "fetch configuration fields values for an interactive element")
    public CompletableFuture<List<ConfigurationField>> getInteractiveConfigurationFields(@GraphQLContext Interactive interactive,
                                                                                         @GraphQLArgument(name = "fieldNames", description = "fetch those field values within the configuration")
                                                                                                 List<String> fieldNames) {
        affirmArgument(fieldNames != null, "fieldNames argument required");
        affirmArgument(!fieldNames.isEmpty(), "at least 1 field name must be supplied");

        return coursewareService.fetchConfigurationFields(interactive.getId(), fieldNames)
                .collectList()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    /**
     * Fetch a list of configuration fields given their names for an interactive element
     *
     * @param interactive the context
     * @param keys a list of keys to fetch the meta info for
     * @return the found meta info
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Interactive.metaInfo")
    @GraphQLQuery(name = "metaInfo", description = "fetch meta information values for an interactive")
    public CompletableFuture<List<CoursewareElementMetaInformation>> getInteractiveMetaInfo(@GraphQLContext Interactive interactive,
                                                                                            @GraphQLArgument(name = "keys", description = "the meta info keys to fetch the values for")
                                                                                                    List<String> keys) {

        return coursewareElementMetaInformationService.findMetaInfo(interactive.getId(), keys)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .toFuture();
    }
}
