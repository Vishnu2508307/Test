package com.smartsparrow.graphql.schema;

import static com.smartsparrow.iam.util.Permissions.affirmPermission;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.cohort.data.CohortEnrollment;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.LearnerManualGradingConfiguration;
import com.smartsparrow.courseware.data.ManualGradingConfiguration;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortContributorOrHigher;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.auth.AllowWorkspaceReviewerOrHigher;
import com.smartsparrow.graphql.service.GraphQLPageFactory;
import com.smartsparrow.graphql.type.mutation.LearnerManualGradingReportInput;
import com.smartsparrow.graphql.type.mutation.ManualGradeArg;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerComponent;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.data.StudentManualGrade;
import com.smartsparrow.learner.data.StudentManualGradeReport;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.learner.service.LearnerComponentService;
import com.smartsparrow.learner.service.LearnerService;
import com.smartsparrow.learner.service.ManualGradeService;
import com.smartsparrow.learner.service.StudentScoreService;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.Workspace;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Mono;

@Singleton
public class ComponentSchema {

    private final LearnerComponentService learnerComponentService;
    private final LearnerService learnerService;
    private final AllowCohortInstructor allowCohortInstructor;
    private final ManualGradeService manualGradeService;
    private final DeploymentService deploymentService;
    private final AllowCohortContributorOrHigher allowCohortContributorOrHigher;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final StudentScoreService studentScoreService;
    private final AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher;
    private final CoursewareService coursewareService;

    @Inject
    public ComponentSchema(final LearnerComponentService learnerComponentService,
                           final LearnerService learnerService,
                           final AllowCohortInstructor allowCohortInstructor,
                           final ManualGradeService manualGradeService,
                           final DeploymentService deploymentService,
                           final AllowCohortContributorOrHigher allowCohortContributorOrHigher,
                           final AuthenticationContextProvider authenticationContextProvider,
                           final StudentScoreService studentScoreService,
                           final AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher,
                           final CoursewareService coursewareService) {
        this.learnerComponentService = learnerComponentService;
        this.learnerService = learnerService;
        this.allowCohortInstructor = allowCohortInstructor;
        this.manualGradeService = manualGradeService;
        this.deploymentService = deploymentService;
        this.allowCohortContributorOrHigher = allowCohortContributorOrHigher;
        this.authenticationContextProvider = authenticationContextProvider;
        this.studentScoreService = studentScoreService;
        this.allowWorkspaceReviewerOrHigher = allowWorkspaceReviewerOrHigher;
        this.coursewareService = coursewareService;
    }
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Component.components")
    @GraphQLQuery(name = "components", description = "List of child components")
    public CompletableFuture<List<LearnerComponent>> getComponents(@GraphQLContext LearnerWalkable walkable) {
        return learnerComponentService.findComponents(walkable.getId(), walkable.getElementType(), walkable.getDeploymentId())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .toFuture();
    }

    /**
     * Find all the manual grading components in a deployment
     *
     * @param deployment the deployment to search for the manual grading component
     * @return a list of manual grading configurations
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Component.manualGradingComponents")
    @GraphQLQuery(name = "manualGradingComponents", description = "Find all the manual grading components")
    public CompletableFuture<List<LearnerManualGradingConfiguration>> getManualGradingConfigurations(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                                     @GraphQLContext Deployment deployment) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        affirmPermission(allowCohortInstructor.test(context.getAuthenticationContext(), deployment.getCohortId()), "Unauthorized");

        return manualGradeService.findManualGradingConfigurations(deployment.getId())
                // filter by the deployment changeId, temporary behaviour
                // TODO: remove once https://jira.smartsparrow.com/browse/PLT-5393 is implemented
                .filter(manualGradingConfiguration -> deployment.getChangeId().equals(manualGradingConfiguration.getChangeId()))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .toFuture();
    }

    /**
     * Find the component specific configuration fields with their values
     *
     * @param manualGradingConfiguration the manual grading configuration context
     * @param fieldNames a list of field names to fetch
     * @return a list of configuration fields
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Component.componentConfigurationFields")
    @GraphQLQuery(name = "componentConfigurationFields", description = "fetch configuration fields values for a component")
    public CompletableFuture<List<ConfigurationField>> getComponentFields(@GraphQLContext LearnerManualGradingConfiguration manualGradingConfiguration,
                                                                          @GraphQLArgument(name = "fieldNames", description = "fetch those field values within the configuration")
                                                               List<String> fieldNames) {

        affirmArgument(fieldNames != null, "fieldNames argument required");
        affirmArgument(!fieldNames.isEmpty(), "at least 1 field name must be supplied");

        return learnerService.fetchFields(manualGradingConfiguration.getDeploymentId(), manualGradingConfiguration.getChangeId(),
                manualGradingConfiguration.getComponentId(), fieldNames)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .toFuture();
    }

    /**
     * Find the component parent's specific configuration fields with their values
     *
     * @param manualGradingConfiguration the manual grading configuration contex
     * @param fieldNames a list of field names to fetch
     * @return a list of configuration fields
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Component.parentConfigurationFields")
    @GraphQLQuery(name = "parentConfigurationFields", description = "fetch configuration fields values for the parent walkable")
    public CompletableFuture<List<ConfigurationField>> getParentWalkableFields(@GraphQLContext LearnerManualGradingConfiguration manualGradingConfiguration,
                                                                               @GraphQLArgument(name = "fieldNames", description = "fetch those field values within the configuration")
                                                                    List<String> fieldNames) {

        affirmArgument(fieldNames != null, "fieldNames argument required");
        affirmArgument(!fieldNames.isEmpty(), "at least 1 field name must be supplied");

        return learnerService.fetchFields(manualGradingConfiguration.getDeploymentId(), manualGradingConfiguration.getChangeId(),
                manualGradingConfiguration.getParentId(), fieldNames)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .toFuture();
    }

    /**
     * Find a manual grading report for an enrolled student
     *
     * @param cohortEnrollment the cohort enrollment context
     * @param input the manual grading input information to find the report for
     * @return a student manual grade report object
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Component.getLatestAttemptStudentManualGradeReport")
    @GraphQLQuery(name = "getLatestAttemptStudentManualGradeReport", description = "fetch the manual grade report for a student based on the latest attempt over the manual grading component")
    public CompletableFuture<StudentManualGradeReport> getLatestAttemptStudentManualGradeReport(@GraphQLContext CohortEnrollment cohortEnrollment,
                                                                                                @GraphQLArgument(name = "input", description = "the component manual grading configuration")
                                                                             @Nonnull LearnerManualGradingReportInput input) {
        return manualGradeService.findLatestAttemptManualGradeReport(
                cohortEnrollment.getAccountId(),
                input.getDeploymentId(),
                input.getComponentId(),
                input.getParentId(),
                input.getParentType())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    /**
     * Mutation for creating a manual grade for a student over a particular component
     *
     * @param deploymentId the deployment the component belongs to
     * @param componentId the component id to grade
     * @param manualGradeArg a graphql input object the required fields for manual grade creation
     * @return the created student manual grade
     * @throws NotFoundFault when the deployment is not found
     * @throws PermissionFault when the authenticated user does not have the necessary permission level to perform the mutation
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Component.ManualGradeCreate")
    @GraphQLMutation(name = "ManualGradeCreate", description = "create a manual grade for a student over a component")
    public CompletableFuture<StudentManualGrade> createStudentManualGrade(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                          @GraphQLNonNull @GraphQLArgument(name = "deploymentId") final UUID deploymentId,
                                                                          @GraphQLNonNull @GraphQLArgument(name = "componentId") final UUID componentId,
                                                                          @GraphQLNonNull @GraphQLArgument(name = "manualGrade") final ManualGradeArg manualGradeArg) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        final UUID accountId = authenticationContextProvider.get().getAccount().getId();

        // prepare the variables
        final MutationOperator operator = manualGradeArg.getOperator();
        final UUID studentId = manualGradeArg.getStudentId();
        final Double score = manualGradeArg.getScore();
        final UUID attemptId = manualGradeArg.getAttemptId();

        // validate the parameters
        affirmArgument(operator != null, "operator is required");
        affirmArgument(studentId != null, "studentId is required");
        affirmArgument(score != null, "score is required");
        affirmArgument(attemptId != null, "attemptId is required");

        // find the deployment
        return deploymentService.findDeployment(deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .doOnError(DeploymentNotFoundException.class, ex -> {
                    throw new NotFoundFault("deployment not found");
                })
                .flatMap(deployedActivity -> {
                    if (!allowCohortContributorOrHigher.test(context.getAuthenticationContext(),
                                                           deployedActivity.getCohortId())) {
                        return Mono.error(new PermissionFault("Unauthorized"));
                    }
                    return studentScoreService
                            .createStudentManualGrade(deploymentId, componentId, studentId, attemptId, score, operator, accountId)
                            .doOnEach(ReactiveTransaction.linkOnNext());
                })
                .toFuture();
    }

    /**
     * Fetch all the manual grading configuration for a walkable
     *
     * @param workspace the workspace the walkable belongs to
     * @param walkableId the walkable id
     * @param before pagination arg
     * @param last pagination arg
     * @return a page of manual grading configuration
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Component.getManualGradingConfigurations")
    @GraphQLQuery(name = "getManualGradingConfigurations", description = "fetch manual grading configurations by walkable")
    public CompletableFuture<Page<ManualGradingConfiguration>> getManualGradingConfigurations(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                                              @GraphQLContext Workspace workspace,
                                                                                              @GraphQLArgument(name = "walkableId", description = "the walkable id to find manual grading components for")
                                                                                   UUID walkableId,
                                                                                              @GraphQLArgument(name = "before", description = "fetching only nodes before this node (exclusive)") String before,
                                                                                              @GraphQLArgument(name = "last", description = "fetching only the last certain number of nodes") Integer last) {
        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();
        // only allows a workspace reviewer to fetch this information
        affirmPermission(allowWorkspaceReviewerOrHigher.test(context.getAuthenticationContext(), workspace.getId()), "Not allowed");

        Mono<List<ManualGradingConfiguration>> all = manualGradeService.findChildManualGradingConfigurationByWalkable(walkableId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList();

        return GraphQLPageFactory.createPage(all, before, last).toFuture();
    }

    /**
     * Find a list of configuration fields for a component given a list of configuration field names
     *
     * @param manualGradingConfiguration the manual grading configuration to fetch the component configuration fields for
     * @param fieldNames the name of the component configuration fields to fetch
     * @return a list of configuration fields
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Component.componentConfigurationFields")
    @GraphQLQuery(name = "componentConfigurationFields", description = "fetch configuration fields values for a component")
    public CompletableFuture<List<ConfigurationField>> getManualGradingComponentConfigurationFields(@GraphQLContext ManualGradingConfiguration manualGradingConfiguration,
                                                                                                    @GraphQLArgument(name = "fieldNames", description = "fetch those field values within the configuration")
                                                                                         List<String> fieldNames) {

        affirmArgument(fieldNames != null, "fieldNames argument required");
        affirmArgument(!fieldNames.isEmpty(), "at least 1 field name must be supplied");

        return coursewareService.fetchConfigurationFields(manualGradingConfiguration.getComponentId(), fieldNames)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .toFuture();
    }
}
