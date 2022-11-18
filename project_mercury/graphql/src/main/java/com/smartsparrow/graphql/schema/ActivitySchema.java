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
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementMetaInformation;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.auth.AllowEnrolledStudent;
import com.smartsparrow.graphql.auth.AllowWorkspaceReviewerOrHigher;
import com.smartsparrow.graphql.type.EnrollmentLearnerActivity;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.Score;
import com.smartsparrow.learner.data.StudentManualGradeReport;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.service.CoursewareElementMetaInformationService;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.learner.service.LearnerActivityService;
import com.smartsparrow.learner.service.LearnerCoursewareService;
import com.smartsparrow.learner.service.ManualGradeService;
import com.smartsparrow.learner.service.StudentScoreService;
import com.smartsparrow.pubsub.subscriptions.learner.StudentWalkablePrefetchProducer;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.Workspace;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

@Singleton
public class ActivitySchema {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ActivitySchema.class);

    private static final String ERROR_MESSAGE = "Unauthorized";

    private final AllowCohortInstructor allowCohortInstructor;
    private final AllowEnrolledStudent allowEnrolledStudent;
    //
    private final LearnerActivityService learnerActivityService;
    private final DeploymentService deploymentService;
    private final LearnerCoursewareService learnerCoursewareService;
    private final StudentScoreService studentScoreService;
    private final ManualGradeService manualGradeService;
    private final ActivityService activityService;
    private final AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher;
    private final CoursewareService coursewareService;
    private final CoursewareElementMetaInformationService coursewareElementMetaInformationService;
    private final StudentWalkablePrefetchProducer studentWalkablePrefetchProducer;

    @Inject
    public ActivitySchema(final AllowCohortInstructor allowCohortInstructor,
                          final AllowEnrolledStudent allowEnrolledStudent,
                          final LearnerActivityService learnerActivityService,
                          final DeploymentService deploymentService,
                          final LearnerCoursewareService learnerCoursewareService,
                          final StudentScoreService studentScoreService,
                          final ManualGradeService manualGradeService,
                          final ActivityService activityService,
                          final AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher,
                          final CoursewareService coursewareService,
                          final CoursewareElementMetaInformationService coursewareElementMetaInformationService,
                          final StudentWalkablePrefetchProducer studentWalkablePrefetchProducer) {
        this.allowCohortInstructor = allowCohortInstructor;
        this.allowEnrolledStudent = allowEnrolledStudent;
        this.learnerActivityService = learnerActivityService;
        this.deploymentService = deploymentService;
        this.learnerCoursewareService = learnerCoursewareService;
        this.studentScoreService = studentScoreService;
        this.manualGradeService = manualGradeService;
        this.activityService = activityService;
        this.allowWorkspaceReviewerOrHigher = allowWorkspaceReviewerOrHigher;
        this.coursewareService = coursewareService;
        this.coursewareElementMetaInformationService = coursewareElementMetaInformationService;
        this.studentWalkablePrefetchProducer = studentWalkablePrefetchProducer;
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Activity.activity")
    @GraphQLQuery(name = "activity", description = "Learner activity data including config, theme and plugin info")
    public CompletableFuture<LearnerActivity> getLearnerActivity(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                 @GraphQLContext DeployedActivity deployment,
                                                                 @Nullable @GraphQLArgument(name = "activityId",
                                                                         description = "Fetch activity with specific id ") UUID activityId) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        UUID accountId = context.getAuthenticationContext().getAccount().getId();

        if (activityId != null) {
            return learnerActivityService.findActivity(activityId, deployment.getId())
                    .flatMap(activity -> {
                        studentWalkablePrefetchProducer.buildStudentWalkablePrefetchConsumable(accountId,
                                                                                               activity).produce();
                        return Mono.just(activity);
                    })
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .toFuture();
        }

        return learnerActivityService.findActivity(deployment.getActivityId(), deployment.getId())
                .flatMap(learnerActivity -> {
                    studentWalkablePrefetchProducer.buildStudentWalkablePrefetchConsumable(accountId,
                                                                                           learnerActivity).produce();
                    return Mono.just(learnerActivity);
                })
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    /**
     * Provide an ancestry field to LearnerActivity objects
     *
     * @param learnerActivity the context
     * @return a List of CoursewareElements
     */
    @SuppressWarnings("Duplicates")
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Activity.ancestry")
    @GraphQLQuery(name = "ancestry", description = "Get Learner Activity ancestry")
    public CompletableFuture<List<CoursewareElement>> getAncestry(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                  @GraphQLContext LearnerActivity learnerActivity,
                                                                  @Nullable @GraphQLArgument(name = "cohortId", description = "Fetch a cohort with specific id") UUID cohortId) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        // using the deployment id to get the cohort id if the cohort id argument is not available in graphql query
        // TODO: remove it when cohort id is available in graphql argument
        if (cohortId == null) {
            cohortId = deploymentService.findDeployment(learnerActivity.getDeploymentId())
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
        Mono<List<CoursewareElement>> ancestryListMono = learnerCoursewareService
                .getAncestry(learnerActivity.getDeploymentId(),//
                             learnerActivity.getId(),//
                             learnerActivity.getElementType()) //
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext());

        return  ancestryListMono
                .flatMapIterable(item -> item)
                .filter(item -> !learnerActivity.getId().equals(item.getElementId()))
                .collectList()
                .toFuture();
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Activity.learnerPerformanceByDeployment")
    @GraphQLQuery(name = "learnerPerformanceByDeployment", description = "Learner activity data, providing context")
    public CompletableFuture<EnrollmentLearnerActivity> getLearnerActivityByDeployment(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                       @GraphQLContext final CohortEnrollment cohortEnrollment,
                                                                                       @GraphQLArgument(name = "deploymentId", description = "the id of the deployment") @GraphQLNonNull UUID deploymentId,
                                                                                       @GraphQLArgument(name = "activityId", description = "the id of an activity somewhere within the specified deployment") @GraphQLNonNull UUID activityId) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        // only allow instructors to request this data as it is part of the Cohort Enrollment context.
        affirmPermission(allowCohortInstructor.test(context.getAuthenticationContext(),
                                                    cohortEnrollment.getCohortId()), "Unauthorized");
        //
        return learnerActivityService.findActivity(activityId, deploymentId)
                .map(publishedActivity -> new EnrollmentLearnerActivity() //
                        .setLearnerActivity(publishedActivity) //
                        .setEnrollment(cohortEnrollment)) //
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext()).toFuture();
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Activity.score")
    @GraphQLQuery(name = "score", description = "get the learner activity latest attempt score for the enrolled student")
    public CompletableFuture<Score> getScore(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                             @GraphQLContext final EnrollmentLearnerActivity enrollmentLearnerActivity) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        // only allow instructors to request this data as it is part of the Cohort Enrollment context.
        affirmPermission(allowCohortInstructor.test(context.getAuthenticationContext(),
                                                    enrollmentLearnerActivity.getEnrollment().getCohortId()),
                         "Unauthorized");

        final UUID studentId = enrollmentLearnerActivity.getEnrollment().getAccountId();
        final LearnerActivity learnerActivity = enrollmentLearnerActivity.getLearnerActivity();

        return studentScoreService.computeScore(learnerActivity.getDeploymentId(), studentId, learnerActivity.getId(),
                                                null)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Activity.getLatestAttemptActivityManualGradeReport")
    @GraphQLQuery(name = "getLatestAttemptActivityManualGradeReport", description = "fetch the manual grade reports for a student on all the manual component descendants of this activity")
    public CompletableFuture<List<StudentManualGradeReport>> getManualGradeReports(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                   @GraphQLContext final EnrollmentLearnerActivity enrollmentLearnerActivity) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        // only allow instructors to request this data as it is part of the Cohort Enrollment context.
        affirmPermission(allowCohortInstructor.test(context.getAuthenticationContext(),
                                                    enrollmentLearnerActivity.getEnrollment().getCohortId()),
                         "Unauthorized");

        final UUID studentId = enrollmentLearnerActivity.getEnrollment().getAccountId();
        final LearnerActivity learnerActivity = enrollmentLearnerActivity.getLearnerActivity();

        return manualGradeService.findLatestAttemptManualGradeReport(learnerActivity.getDeploymentId(),
                                                                     learnerActivity.getChangeId(),
                                                                     learnerActivity.getId(),
                                                                     studentId)
                .collectList()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = "Activity.coursewareActivity")
    @GraphQLQuery(name = "coursewareActivity", description = "Learner activity data including config, theme and plugin info")
    public CompletableFuture<Activity> getActivity(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                   @GraphQLContext Workspace workspace,
                                                   @GraphQLArgument(name = "activityId", description = "Fetch activity with specific id ") UUID activityId) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        affirmPermission(allowWorkspaceReviewerOrHigher.test(context.getAuthenticationContext(),
                                                             workspace.getId()), "Higher permission level required");

        return activityService.findById(activityId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .doOnError(ActivityNotFoundException.class, ex -> {
                    throw new NotFoundFault(ex.getMessage());
                })
                .toFuture();
    }

    /**
     * Fetch a list of configuration fields given their names for an activity element
     *
     * @param activity the context
     * @param fieldNames the name of the fields to find
     * @return a list of configuration fields
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Activity.coursewareConfigurationFields")
    @GraphQLQuery(name = "coursewareConfigurationFields", description = "fetch configuration fields values for an activity element")
    public CompletableFuture<List<ConfigurationField>> getActivityConfigurationFields(@GraphQLContext Activity activity,
                                                                                      @GraphQLArgument(name = "fieldNames", description = "fetch those field values within the configuration")
                                                                                              List<String> fieldNames) {

        affirmArgument(fieldNames != null, "fieldNames argument required");
        affirmArgument(!fieldNames.isEmpty(), "at least 1 field name must be supplied");

        return coursewareService.fetchConfigurationFields(activity.getId(), fieldNames)
                .collectList()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    /**
     * Fetch a list of configuration fields given their names for an activity element
     *
     * @param activity the context
     * @param keys a list of keys to fetch the meta info for
     * @return the found meta info
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Activity.metaInfo")
    @GraphQLQuery(name = "metaInfo", description = "fetch meta information values for an activity")
    public CompletableFuture<List<CoursewareElementMetaInformation>> getInteractiveMetaInfo(@GraphQLContext Activity activity,
                                                                                            @GraphQLArgument(name = "keys", description = "the meta info keys to fetch the values for")
                                                                                                    List<String> keys) {


        return coursewareElementMetaInformationService.findMetaInfo(activity.getId(), keys)
                .collectList()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }
}
