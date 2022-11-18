package com.smartsparrow.graphql.schema;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.CoursewareElementMetaInformation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ManualGradingConfiguration;
import com.smartsparrow.courseware.data.Walkable;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.service.GraphQLPageFactory;
import com.smartsparrow.learner.data.CompletedWalkable;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.Evaluation;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.data.Score;
import com.smartsparrow.learner.data.ScoreReason;
import com.smartsparrow.learner.data.StudentManualGradeReport;
import com.smartsparrow.learner.service.CoursewareElementMetaInformationService;
import com.smartsparrow.learner.service.CoursewareHistoryService;
import com.smartsparrow.learner.service.EvaluationResultService;
import com.smartsparrow.learner.service.LearnerActivityService;
import com.smartsparrow.learner.service.LearnerInteractiveService;
import com.smartsparrow.learner.service.LearnerService;
import com.smartsparrow.learner.service.ManualGradeService;
import com.smartsparrow.learner.service.StudentScoreService;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLEnvironment;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class WalkableSchema {

    private static final Logger log = LoggerFactory.getLogger(WalkableSchema.class);

    private final LearnerActivityService learnerActivityService;
    private final LearnerInteractiveService learnerInteractiveService;
    private final CoursewareHistoryService coursewareHistoryService;
    private final EvaluationResultService evaluationResultService;
    private final LearnerService learnerService;
    private final StudentScoreService studentScoreService;
    private final PathwayService pathwayService;
    private final ActivityService activityService;
    private final InteractiveService interactiveService;
    private final ManualGradeService manualGradeService;
    private final CoursewareService coursewareService;
    private final CoursewareElementMetaInformationService coursewareElementMetaInformationService;

    @Inject
    public WalkableSchema(final LearnerActivityService learnerActivityService,
                          final LearnerInteractiveService learnerInteractiveService,
                          final CoursewareHistoryService coursewareHistoryService,
                          final EvaluationResultService evaluationResultService,
                          final LearnerService learnerService,
                          final StudentScoreService studentScoreService,
                          final PathwayService pathwayService,
                          final ActivityService activityService,
                          final InteractiveService interactiveService,
                          final ManualGradeService manualGradeService,
                          final CoursewareService coursewareService,
                          final CoursewareElementMetaInformationService coursewareElementMetaInformationService) {
        this.learnerActivityService = learnerActivityService;
        this.learnerInteractiveService = learnerInteractiveService;
        this.coursewareHistoryService = coursewareHistoryService;
        this.evaluationResultService = evaluationResultService;
        this.learnerService = learnerService;
        this.studentScoreService = studentScoreService;
        this.pathwayService = pathwayService;
        this.activityService = activityService;
        this.interactiveService = interactiveService;
        this.manualGradeService = manualGradeService;
        this.coursewareService = coursewareService;
        this.coursewareElementMetaInformationService = coursewareElementMetaInformationService;
    }

    /**
     * Parameters 'before' and 'last' are required to be compatible with the Relay Connection spec. (can be disabled)
     * @return
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Walkable.walkables")
    @GraphQLQuery(name = "walkables", description = "List of child walkables (interactive or activity)")
    public CompletableFuture<Page<LearnerWalkable>> getLearnerWalkables(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                        @GraphQLContext LearnerPathway pathway,
                                                                        @GraphQLArgument(name = "before", description = "fetching only nodes before this node (exclusive)") String before,
                                                                        @GraphQLArgument(name = "last", description = "fetching only the last certain number of nodes") Integer last) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        UUID studentId = context.getAuthenticationContext().getAccount().getId();
        Flux<WalkableChild> list = pathway.supplyRelevantWalkables(studentId);

        Mono<List<LearnerWalkable>> walkables = list.concatMap(child -> {
                    if (child.getElementType().equals(CoursewareElementType.ACTIVITY)) {
                        return learnerActivityService.findActivity(child.getElementId(), pathway.getDeploymentId());
                    } else {
                        return learnerInteractiveService.findInteractive(child.getElementId(), pathway.getDeploymentId());
                    }
                }).doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .defaultIfEmpty(Lists.newArrayList());

        //checkArgument(walkables != null, "list of walkables can not be null");

        return GraphQLPageFactory.createPage(walkables, before, last).toFuture();
    }

    /**
     * Fetch the history of completed walkables on a learnerPathway. (Currently supports only learnerInteractive)
     *
     * @param pathway          the pathway to find the completed walkables for
     * @param pathwayAttemptId the parent pathway attempt id (when not supplied the latest attempt is looked up)
     * @return a page of completed walkables
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Walkable.history")
    @GraphQLQuery(name = "history", description = "List of completed walkables (only supports interactives)")
    public CompletableFuture<Page<CompletedWalkable>> getHistory(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                                                 @GraphQLContext LearnerPathway pathway,
                                                                 @GraphQLArgument(name = "pathwayAttemptId", description = "the pathway attempt id to find the history for (latest attempt will be used when not specified)")
                                              @Nullable UUID pathwayAttemptId,
                                                                 @GraphQLArgument(name = "before", description = "fetching only nodes before this node (exclusive)") String before,
                                                                 @GraphQLArgument(name = "last", description = "fetching only the last certain number of nodes") Integer last) {

        // TODO: require pathwayAttemptId and remove the nullable annotation. The argument should always be required
        // FIXME: this nullable is a temporary behaviour until the attempts are exposed via api

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        final UUID studentId = context.getAuthenticationContext().getAccount().getId();

        Flux<CompletedWalkable> historyFlux;
        if(pathwayAttemptId != null) {
            historyFlux = coursewareHistoryService.fetchHistory(pathway, studentId, pathwayAttemptId);
        }else {
            historyFlux = coursewareHistoryService.fetchHistory(pathway, studentId);
        }

        Mono<List<CompletedWalkable>> history = historyFlux
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .defaultIfEmpty(new ArrayList<>());

        return GraphQLPageFactory.createPage(history, before, last).toFuture();
    }

    /**
     * Fetch the evaluation data that represents the state of a completed walkable
     *
     * @param completedWalkable the walkable to find the evaluation data for
     * @return the evaluation data
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Walkable.evaluation")
    @GraphQLQuery(name = "evaluation", description = "The evaluation data for a completed walkable")
    public CompletableFuture<Evaluation> getEvaluationData(@GraphQLContext CompletedWalkable completedWalkable) {
        return evaluationResultService.fetch(completedWalkable.getEvaluationId())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    /**
     * Fetch the completed walkable by a student for an instructor to review. Permission check not required here
     * since that is performed in {@link ComponentSchema#getManualGradingConfigurations(ResolutionEnvironment, Deployment)} already.
     * Attempt id could be <strong>null</strong> when the report state is {@link ScoreReason#NOT_ATTEMPTED}
     *
     * @param studentManualGradeReport the manual grade report to find the completed walkable for
     * @return the completed walkable
     * @throws IllegalArgumentFault when the attemptId is <strong>null</strong>
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Walkable.completedWalkable")
    @GraphQLQuery(name = "completedWalkable", description = "find the completed walkable for a student manual grade report")
    public CompletableFuture<CompletedWalkable> getCompletedWalkable(@GraphQLContext StudentManualGradeReport studentManualGradeReport) {

        final UUID deploymentId = studentManualGradeReport.getDeploymentId();
        final UUID studentId = studentManualGradeReport.getStudentId();
        final UUID elementId = studentManualGradeReport.getParentId();
        final UUID attemptId = studentManualGradeReport.getAttemptId();

        affirmArgument(attemptId != null, "attemptId is required");

        return coursewareHistoryService.findCompletedWalkable(deploymentId, studentId, elementId, attemptId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    /**
     * Fetch a list of configuration fields given their names for a completed walkable
     *
     * @param completedWalkable the context
     * @param fieldNames        the name of the fields to find
     * @return a list of configuration fields
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Walkable.configurationFieldsCompletedWalkable")
    @GraphQLQuery(name = "configurationFields", description = "fetch configuration fields values for a completed walkable")
    public CompletableFuture<List<ConfigurationField>> getCompletedWalkableFields(@GraphQLContext CompletedWalkable completedWalkable,
                                                                                  @GraphQLArgument(name = "fieldNames", description = "fetch those field values within the configuration")
                                                                       List<String> fieldNames) {

        affirmArgument(fieldNames != null, "fieldNames argument required");
        affirmArgument(!fieldNames.isEmpty(), "at least 1 field name must be supplied");

        return learnerService.fetchFields(completedWalkable.getDeploymentId(), completedWalkable.getChangeId(),
                completedWalkable.getElementId(), fieldNames)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .toFuture();
    }

    /**
     * Compute a learner walkable score for the authenticated student
     *
     * @param learnerWalkable the learner walkable to compute the score for
     * @return the computed score
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Walkable.score")
    @GraphQLQuery(name = "score", description = "get the learner walkable latest attempt score for the authenticated student")
    public CompletableFuture<Score> getScore(@GraphQLEnvironment ResolutionEnvironment resolutionEnvironment,
                                             @GraphQLContext final LearnerWalkable learnerWalkable) {

        BronteGQLContext context = resolutionEnvironment.dataFetchingEnvironment.getContext();

        final UUID studentId = context.getAuthenticationContext().getAccount().getId();

        return studentScoreService.computeScore(learnerWalkable.getDeploymentId(), studentId, learnerWalkable.getId(),
                null)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .toFuture();
    }

    /**
     * Fetch a list of configuration fields given their names for a learner walkable
     *
     * @param learnerWalkable the context
     * @param fieldNames      the name of the fields to find
     * @return a list of configuration fields
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Walkable.configurationFieldsLearnerWalkable")
    @GraphQLQuery(name = "configurationFields", description = "fetch configuration fields values for a walkable")
    public CompletableFuture<List<ConfigurationField>> getLearnerWalkableFields(@GraphQLContext LearnerWalkable learnerWalkable,
                                                                                @GraphQLArgument(name = "fieldNames", description = "fetch those field values within the configuration")
                                                                     List<String> fieldNames) {

        affirmArgument(fieldNames != null, "fieldNames argument required");
        affirmArgument(!fieldNames.isEmpty(), "at least 1 field name must be supplied");

        return learnerService.fetchFields(learnerWalkable.getDeploymentId(), learnerWalkable.getChangeId(),
                learnerWalkable.getId(), fieldNames)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .toFuture();
    }

    /**
     * Parameters 'before' and 'last' are required to be compatible with the Relay Connection spec. (can be disabled)
     * @return
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Walkable.coursewareWalkables")
    @GraphQLQuery(name = "coursewareWalkables", description = "List of child walkables (interactive or activity)")
    public CompletableFuture<Page<Walkable>> getWalkables(@GraphQLContext Pathway pathway,
                                                          @GraphQLArgument(name = "before", description = "fetching only nodes before this node (exclusive)") String before,
                                                          @GraphQLArgument(name = "last", description = "fetching only the last certain number of nodes") Integer last) {

        // find the ordered walkable children for a pathway
        Mono<List<WalkableChild>> walkableChildren = pathwayService.getOrderedWalkableChildren(pathway.getId())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .defaultIfEmpty(Lists.newArrayList());

        Mono<List<Walkable>> walkables = walkableChildren
                .flatMapIterable(item -> item)
                .flatMapSequential(walkableChild -> {
                    if (walkableChild.getElementType().equals(CoursewareElementType.ACTIVITY)) {
                        return activityService.findById(walkableChild.getElementId())
                                // resume error when not found
                                .onErrorResume(ActivityNotFoundException.class, ex -> {
                                    log.warn("activity could not be found {}", ex.getMessage());
                                    return Mono.empty();
                                });
                    }

                    return interactiveService.findById(walkableChild.getElementId())
                            // resume error when not found
                            .onErrorResume(InteractiveNotFoundException.class, ex -> {
                                log.warn("interactive could not be found {}", ex.getMessage());
                                return Mono.empty();
                            });
                })
                .filter(Objects::nonNull)
                .collectList();

        return GraphQLPageFactory.createPage(walkables, before, last).toFuture();
    }

    /**
     * Fetch all the manual grading configuration for a walkable (filtered by the first level of children)
     *
     * @param walkable the walkable
     * @param before   pagination arg
     * @param last     pagination arg
     * @return a page of manual grading configuration
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Walkable.getManualGradingConfigurationsCount")
    @GraphQLQuery(name = "getManualGradingConfigurationsCount", description = "fetch manual grading configurations by walkable")
    public CompletableFuture<Integer> getManualGradingConfigurationsCount(@GraphQLContext Walkable walkable,
                                                                          @GraphQLArgument(name = "before", description = "fetching only nodes before this node (exclusive)") String before,
                                                                          @GraphQLArgument(name = "last", description = "fetching only the last certain number of nodes") Integer last) {

        Mono<List<ManualGradingConfiguration>> all = manualGradeService.findChildManualGradingConfigurationByWalkable(
                        walkable.getId())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .defaultIfEmpty(Lists.newArrayList());

        return all
                .flatMapIterable(item -> item)
                .count()
                .map(Long::intValue)
                .toFuture();
    }

    /**
     * Fetch a list of configuration fields given their names for a walkabe element
     *
     * @param walkable   the context
     * @param fieldNames the name of the fields to find
     * @return a list of configuration fields
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Walkable.coursewareWalkableConfigurationFields")
    @GraphQLQuery(name = "coursewareWalkableConfigurationFields", description = "fetch configuration fields values for a walkable")
    public CompletableFuture<List<ConfigurationField>> getInteractiveConfigurationFields(@GraphQLContext Walkable walkable,
                                                                                         @GraphQLArgument(name = "fieldNames", description = "fetch those field values within the configuration")
                                                                              List<String> fieldNames) {

        affirmArgument(fieldNames != null, "fieldNames argument required");
        affirmArgument(!fieldNames.isEmpty(), "at least 1 field name must be supplied");

        return coursewareService.fetchConfigurationFields(walkable.getId(), fieldNames)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .toFuture();
    }

    /**
     * Fetch a list of configuration fields given their names for a walkable element
     *
     * @param walkable the context
     * @param keys a list of keys to fetch the meta info for
     * @return the found meta info
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "Walkable.coursewareWalkableConfigurationFields")
    @GraphQLQuery(name = "coursewareMetaInfo", description = "fetch meta information values for a walkable")
    public CompletableFuture<List<CoursewareElementMetaInformation>> getInteractiveMetaInfo(@GraphQLContext Walkable walkable,
                                                                                            @GraphQLArgument(name = "keys", description = "the meta info keys to fetch the values for")
                                                                                 List<String> keys) {


        return coursewareElementMetaInformationService.findMetaInfo(walkable.getId(), keys)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .collectList()
                .toFuture();
    }
}
