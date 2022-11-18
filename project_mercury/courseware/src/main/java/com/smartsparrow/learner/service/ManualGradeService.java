package com.smartsparrow.learner.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.LearnerManualGradingConfiguration;
import com.smartsparrow.courseware.data.ManualGradingComponentByWalkable;
import com.smartsparrow.courseware.data.ManualGradingConfiguration;
import com.smartsparrow.courseware.data.ManualGradingConfigurationGateway;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerManualGradingComponentByWalkable;
import com.smartsparrow.learner.data.ManualGradeEntry;
import com.smartsparrow.learner.data.ManualGradeEntryGateway;
import com.smartsparrow.learner.data.ScoreReason;
import com.smartsparrow.learner.data.StudentManualGrade;
import com.smartsparrow.learner.data.StudentManualGradeReport;
import com.smartsparrow.learner.lang.AttemptNotFoundFault;
import com.smartsparrow.learner.lang.ManualGradesNotFoundFault;
import com.smartsparrow.learner.lang.ManualGradingConfigurationNotFoundFault;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Singleton
public class ManualGradeService {

    private final ManualGradeEntryGateway manualGradeEntryGateway;
    private final ManualGradingConfigurationGateway manualGradingConfigurationGateway;
    private final AttemptService attemptService;
    private final CoursewareService coursewareService;
    private final CoursewareHistoryService coursewareHistoryService;

    @Inject
    public ManualGradeService(ManualGradeEntryGateway manualGradeEntryGateway,
            ManualGradingConfigurationGateway manualGradingConfigurationGateway,
            AttemptService attemptService,
            CoursewareService coursewareService,
            CoursewareHistoryService coursewareHistoryService) {
        this.manualGradeEntryGateway = manualGradeEntryGateway;
        this.manualGradingConfigurationGateway = manualGradingConfigurationGateway;
        this.attemptService = attemptService;
        this.coursewareService = coursewareService;
        this.coursewareHistoryService = coursewareHistoryService;
    }

    /**
     * Create a manual grading configuration object for the learner and persist it to the database
     *
     * @param manualGradingConfiguration the manual grading configuration to publish
     * @param deployment the deployment the manual grading configuration belongs to
     * @param parentId the component parent id
     * @param parentType the component parent type
     * @return a flux of void
     */
    public Flux<Void> publishManualGradingConfiguration(final ManualGradingConfiguration manualGradingConfiguration,
                                                        final Deployment deployment,
                                                        final UUID parentId,
                                                        final CoursewareElementType parentType) {

        LearnerManualGradingConfiguration configuration = new LearnerManualGradingConfiguration()
                .setDeploymentId(deployment.getId())
                .setChangeId(deployment.getChangeId())
                .setComponentId(manualGradingConfiguration.getComponentId())
                .setMaxScore(manualGradingConfiguration.getMaxScore())
                .setParentId(parentId)
                .setParentType(parentType);

        return manualGradingConfigurationGateway.persist(configuration);
    }

    /**
     * Find all the manual grading configurations for a deployment
     *
     * @param deploymentId the deployment to find the configured manually gradable component for
     * @return a flux of manual grading configuration or an empty flux when none found
     * @throws IllegalArgumentFault when the deploymentId argument supplied is <strong>null</strong>
     */
    @Trace(async = true)
    public Flux<LearnerManualGradingConfiguration> findManualGradingConfigurations(final UUID deploymentId) {
        affirmArgument(deploymentId != null, "deploymentId is required");

        return manualGradingConfigurationGateway.findAll(deploymentId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the manual grading configuration for a component
     *
     * @param deploymentId the deployment the component belongs to
     * @param componentId the component to find the manual grading configuration for
     * @return a mono of learner manual grading configuration
     * @throws ManualGradingConfigurationNotFoundFault when the configuration is not found
     */
    @Trace(async = true)
    public Mono<LearnerManualGradingConfiguration> findManualGradingConfiguration(final UUID deploymentId, final UUID componentId) {
        affirmArgument(deploymentId != null, "deploymentId is required");
        affirmArgument(componentId != null, "componentId is required");

        return manualGradingConfigurationGateway.find(deploymentId, componentId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .single()
                .doOnError(NoSuchElementException.class, ex -> {
                    throw new ManualGradingConfigurationNotFoundFault(
                            String.format("configuration not found for deployment %s, component %s",
                                    deploymentId, componentId)
                    );
                });
    }

    /**
     * Build a manual grade report for a student over a manually gradable component based on the student latest attempt.
     * The latest attempt refers to the parent element the component is child of.
     *
     * @param studentId the student to build the manual grade report for
     * @param deploymentId the deployment the component belongs to
     * @param componentId the id of the manual grading component
     * @param parentElementId the component parent element id necessary to fetch the student latest attempt (because
     *                        components do not have attempts, need to look up the parent one)
     * @param parentType the parent element type
     * @return a mono of student manual grade report with:
     *     <br> {@link ScoreReason#NOT_ATTEMPTED} when the latest attempt is not found for the student
     *     <br> {@link ScoreReason#INSTRUCTOR_UNSCORED} when the attempt is found but no manual grade entries are found
     *     for the student
     *     <br> {@link ScoreReason#INSTRUCTOR_SCORED} when manual grade entries are found for the student
     */
    @Trace(async = true)
    public Mono<StudentManualGradeReport> findLatestAttemptManualGradeReport(final UUID studentId,
                                                                             final UUID deploymentId,
                                                                             final UUID componentId,
                                                                             final UUID parentElementId,
                                                                             final CoursewareElementType parentType) {

        return attemptService.findLatestAttempt(deploymentId, parentElementId, studentId)
                .flatMap(latestAttempt -> {
                    // find all the entries and build the report
                    return findManualGradeEntries(deploymentId, studentId, componentId, latestAttempt.getId())
                            // map each entry to a student manual grade obj to reduce size of redundant data
                            .map(manualGradeEntry -> new StudentManualGrade()
                                    .setId(manualGradeEntry.getId())
                                    .setScore(manualGradeEntry.getScore())
                                    .setOperator(manualGradeEntry.getOperator())
                                    .setInstructorId(manualGradeEntry.getInstructorId())
                                    .setCreatedAt(DateFormat.asRFC1123(manualGradeEntry.getId())))
                            .collectList()
                            // build the student manual grade report
                            .flatMap(entries -> Mono.just(new StudentManualGradeReport()
                                    .setDeploymentId(deploymentId)
                                    .setStudentId(studentId)
                                    .setComponentId(componentId)
                                    .setParentId(parentElementId)
                                    .setParentType(parentType)
                                    .setAttemptId(latestAttempt.getId())
                                    .setState(ScoreReason.INSTRUCTOR_SCORED)
                                    .setGrades(entries)))
                            .doOnEach(ReactiveTransaction.linkOnNext())
                            // when no manual grades are found build the report with instructor unscored state
                            .onErrorResume(ManualGradesNotFoundFault.class, ex -> {

                                // Find out if the attempt is complete
                                return coursewareHistoryService.findCompletedWalkable(deploymentId,
                                        studentId,
                                        latestAttempt.getCoursewareElementId(),
                                        latestAttempt.getId())
                                        .hasElement()
                                        .map(completed -> {
                                            StudentManualGradeReport gradeReport = new StudentManualGradeReport()
                                                    .setDeploymentId(deploymentId)
                                                    .setStudentId(studentId)
                                                    .setComponentId(componentId)
                                                    .setParentId(parentElementId)
                                                    .setParentType(parentType)
                                                    .setAttemptId(latestAttempt.getId());
                                            if(completed) {
                                                return gradeReport.setState(ScoreReason.INSTRUCTOR_UNSCORED);
                                            } else {
                                                return gradeReport.setState(ScoreReason.INCOMPLETE_ATTEMPT);
                                            }
                                        })
                                        .doOnEach(ReactiveTransaction.linkOnNext());
                            });
                })
                .doOnEach(ReactiveTransaction.linkOnNext())
                .onErrorResume(AttemptNotFoundFault.class, ex -> {
                    // if the attempt is not found return a report with reason NOT_ATTEMPTED
                    return Mono.just(new StudentManualGradeReport()
                            .setDeploymentId(deploymentId)
                            .setStudentId(studentId)
                            .setComponentId(componentId)
                            .setParentId(parentElementId)
                            .setParentType(parentType)
                            .setState(ScoreReason.NOT_ATTEMPTED));
                });
    }

    /**
     * Find all the manual grade entries for a student over a particular attempt on a component. Component do not really
     * have attempts, a component attempt is really its walkable parent attempt.
     *
     * @param deploymentId the deployment the component belongs to
     * @param studentId the student id to find the manual grade entries for
     * @param componentId the component id the grade refers to
     * @param attemptId the parent component attempt id
     * @return a flux of manual grade entries
     * @throws ManualGradesNotFoundFault when no manual grade entries are found
     */
    @Trace(async = true)
    public Flux<ManualGradeEntry> findManualGradeEntries(final UUID deploymentId, final UUID studentId,
                                                         final UUID componentId, final UUID attemptId) {
        return manualGradeEntryGateway.findAll(deploymentId, studentId, componentId, attemptId)
                .switchIfEmpty(Flux.error(new ManualGradesNotFoundFault()))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Create a manual grade entry for a student over a component. This method is only responsible for creating the
     * manual grade, however the grade is not reported in the student score entry logs. This method can be used when
     * drafting a grade (feature currently not supported). To create a manual grade and apply it to the score entries
     * so that it shows up in score computations use <br>
     * {@link StudentScoreService#createStudentManualGrade(UUID, UUID, UUID, UUID, Double, MutationOperator, UUID)}
     *
     * @param deploymentId the deployment the component belongs to
     * @param componentId the component to grade
     * @param studentId the student receiving the grade
     * @param attemptId the walkable attempt
     * @param score the score value
     * @param operator the score mutation operator
     * @param instructorId the instructor assigning the grade
     * @return a mono of the created manual grade entry
     * @throws ManualGradingConfigurationNotFoundFault when the manual grading configuration is not found
     * @throws IllegalArgumentFault when the supplied score is greater than the maxScore defined in the manual grading
     * configurations
     */
    @Trace(async = true)
    Mono<ManualGradeEntry> createManualGrade(final UUID deploymentId, final UUID componentId,
                                             final UUID studentId, final UUID attemptId, final Double score,
                                             final MutationOperator operator, final UUID instructorId) {
        return findManualGradingConfiguration(deploymentId, componentId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .map(manualGradingConfiguration -> {
                    Double maxScore = manualGradingConfiguration.getMaxScore();
                    // invalidate if the score value is greater than the configuration maxScore value when defined
                    if (maxScore != null && Double.compare(score, maxScore) > 0) {
                        throw new IllegalArgumentFault("score cannot be greater than " + manualGradingConfiguration.getMaxScore());
                    }

                    // invalidate if the score is a negative number
                    if (Double.compare(score, 0d) < 0) {
                        throw new IllegalArgumentFault("score cannot be a negative number");
                    }

                    // create the manual grade entry
                    return new ManualGradeEntry()
                            .setDeploymentId(deploymentId)
                            .setStudentId(studentId)
                            .setComponentId(componentId)
                            .setAttemptId(attemptId)
                            .setId(UUIDs.timeBased())
                            .setMaxScore(maxScore)
                            .setScore(score)
                            .setChangeId(manualGradingConfiguration.getChangeId())
                            .setParentId(manualGradingConfiguration.getParentId())
                            .setParentType(manualGradingConfiguration.getParentType())
                            .setOperator(operator)
                            .setInstructorId(instructorId);

                })
                // persist the manual grade entry
                .flatMap(manualGradeEntry -> manualGradeEntryGateway.persist(manualGradeEntry)
                        // return the persisted manual grade
                        .then(Mono.just(manualGradeEntry)));
    }

    /**
     * Create the manual grading configurations for a component. Track the manual grading component by each
     * walkable in the component ancestry list.
     *
     * @param componentId the component to create the manual grading configuration for
     * @param maxScore the max score property
     * @return a mono with the created manual grading configuration
     * @throws IllegalStateFault when the ancestry for the component has less than 2 elements. This is impossible,
     * the norm is at least the componentId and its parent are present in the list since components are not root elements.
     */
    @Trace(async = true)
    public Mono<ManualGradingConfiguration> createManualGradingConfiguration(final UUID componentId, @Nullable final Double maxScore) {

        // check the componentId is supplied
        affirmArgument(componentId != null, "componentId is required");

        // find the ancestry from the component to the last ancestor
        Mono<List<CoursewareElement>> ancestryMono = getAncestry(componentId);

        // create the manual grading component configurations
        ManualGradingConfiguration configuration = new ManualGradingConfiguration()
                .setComponentId(componentId)
                .setMaxScore(maxScore);

        // persist the created manual grading component configurations
        Mono<ManualGradingConfiguration> manualGradingConfigurationMono = manualGradingConfigurationGateway.persist(configuration)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .singleOrEmpty()
                .thenReturn(configuration);

        // prepare to track by walkable
        return Mono.zip(manualGradingConfigurationMono, ancestryMono)
                .flatMap(handleWalkableTracking(manualGradingConfigurationGateway::persist));
    }

    /**
     * Delete a manual grading configuration object
     *
     * @param componentId the component id to delete the manual grading configuration for
     * @return a flux of void
     */
    @Trace(async = true)
    public Flux<Void> deleteManualGradingConfiguration(final UUID componentId) {

        affirmArgument(componentId != null, "componentId is required");

        // find the ancestry from the component to the last ancestor
        Mono<List<CoursewareElement>> ancestryMono = getAncestry(componentId);

        ManualGradingConfiguration toDelete = new ManualGradingConfiguration()
                .setComponentId(componentId);

        Mono<ManualGradingConfiguration> deletedMono = manualGradingConfigurationGateway.delete(toDelete)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .then(Mono.just(toDelete));

        // prepare to track by walkable
        return Mono.zip(deletedMono, ancestryMono)
                .flatMap(handleWalkableTracking(manualGradingConfigurationGateway::delete))
                .thenMany(Flux.just(new Void[]{}));
    }

    /**
     * A function that process the manual grading configuration tracking for the walkable ancestry by applying the
     * mutation supplied as argument.
     *
     * @param mutation the mutation to apply for each manual component by walkable tracking
     * @return the function to invoke
     */
    private Function<Tuple2<ManualGradingConfiguration, List<CoursewareElement>>, Mono<ManualGradingConfiguration>>
    handleWalkableTracking(Function<ManualGradingComponentByWalkable, Flux<Void>> mutation) {
        return tuple2 -> {
            final List<CoursewareElement> ancestry = tuple2.getT2();
            final ManualGradingConfiguration persisted = tuple2.getT1();

            if (ancestry.isEmpty() || ancestry.size() < 2) {
                throw new IllegalStateFault("the ancestry should have at least 2 elements");
            }

            // get the parent element for the component (this will be useful in the report query later on)
            final CoursewareElement componentParent = ancestry.get(1);

            // remove any element that is not a walkable from the ancestry list
            final List<CoursewareElement> walkableAncestry = getWalkableAncestry(ancestry);

            return walkableAncestry.stream()
                    // for each walkable element in the list
                    .map(walkableAncestor -> {
                        // create a tracking manual component by walkable
                        ManualGradingComponentByWalkable manualComponent = new ManualGradingComponentByWalkable()
                                .setComponentId(persisted.getComponentId())
                                .setWalkableId(walkableAncestor.getElementId())
                                .setWalkableType(walkableAncestor.getElementType())
                                // always set the parent component info
                                .setComponentParentId(componentParent.getElementId())
                                .setParentComponentType(componentParent.getElementType());

                        // persist the tracking
                        return mutation.apply(manualComponent)
                                .then(Mono.just(manualComponent))
                                .flux();
                    })
                    .reduce(Flux::concatWith)
                    .orElse(Flux.empty())
                    // return the persisted manual grading component configurations
                    .then(Mono.just(persisted));
        };
    }

    /**
     * Get the component ancestry from the component id to the last ancestor
     *
     * @param componentId the component to get the ancestry for
     * @return a mono of list with ancestor elements
     */
    @Trace(async = true)
    private Mono<List<CoursewareElement>> getAncestry(final UUID componentId) {
        return coursewareService.getPath(componentId, CoursewareElementType.COMPONENT)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .map(ancestry -> {
                    Collections.reverse(ancestry);
                    return ancestry;
                });
    }

    /**
     * Get the walkable elements from the ancestry by filtering all other types of courseware elements
     *
     * @param ancestry the ancestry to filter
     * @return a list of walkable ancestry
     */
    private List<CoursewareElement> getWalkableAncestry(final List<CoursewareElement> ancestry) {
        return ancestry.stream()
                .filter(ancestor -> {
                    CoursewareElementType type = ancestor.getElementType();

                    return type.equals(CoursewareElementType.ACTIVITY) || type.equals(CoursewareElementType.INTERACTIVE);
                })
                .collect(Collectors.toList());
    }

    /**
     * Publish manual grading component descendants tracking for a walkable
     *
     * @param walkableId the walkable to find the manual component descendants to publish
     * @param deployment the deployment the walkable belongs to
     * @return a flux of the published manual component descendants
     */
    public Flux<LearnerManualGradingComponentByWalkable> publishManualComponentByWalkable(final UUID walkableId, final Deployment deployment) {
        return manualGradingConfigurationGateway.findManualComponentsByWalkable(walkableId)
                .flatMap(manualComponent -> {
                    LearnerManualGradingComponentByWalkable learnerManualComponent = new LearnerManualGradingComponentByWalkable()
                            .setDeploymentId(deployment.getId())
                            .setWalkableId(manualComponent.getWalkableId())
                            .setComponentId(manualComponent.getComponentId())
                            .setChangeId(deployment.getChangeId())
                            .setWalkableType(manualComponent.getWalkableType())
                            .setComponentParentId(manualComponent.getComponentParentId())
                            .setComponentParentType(manualComponent.getParentComponentType());

                    return manualGradingConfigurationGateway.persist(learnerManualComponent)
                            .then(Mono.just(learnerManualComponent))
                            .flux();
                });
    }

    /**
     * Find all the manual grading components for a walkable
     *
     * @param walkableId the walkable to find the manual grading components for
     * @return a flux of manual grading components
     */
    public Flux<ManualGradingComponentByWalkable> findManualGradingComponentByWalkable(final UUID walkableId) {
        return manualGradingConfigurationGateway.findManualComponentsByWalkable(walkableId);
    }

    /**
     * Find all the manual grading configuration by walkable that are direct children of the walkable.
     *
     * @param walkableId the walkable id to find the manual grading configurations for
     * @return a flux of manual grading configurations
     */
    @Trace(async = true)
    public Flux<ManualGradingConfiguration> findChildManualGradingConfigurationByWalkable(final UUID walkableId) {
        return manualGradingConfigurationGateway.findManualComponentsByWalkable(walkableId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                // by design all the MGC in the sub-tree are returned
                // only return the manual grading component that are direct children of the given walkable
                .filter(one -> one.getComponentParentId().equals(walkableId))
                .flatMap(manualGradingComponentByWalkable -> {
                    return manualGradingConfigurationGateway.find(manualGradingComponentByWalkable.getComponentId())
                            .flux();
                });
    }

    /**
     * Find Student manual grade latest report by walkable. The list is filtered by the latest changeId. This is
     * a temporary behaviour until https://jira.smartsparrow.com/browse/PLT-5393 is implemented
     *
     * @param deploymentId the deployment the walkable belongs to
     * @param walkableId the walkable id
     * @param studentId the student to find the latest report for
     * @return a flux of student manual grade report
     */
    @Trace(async = true)
    public Flux<StudentManualGradeReport> findLatestAttemptManualGradeReport(final UUID deploymentId, final UUID changeId, final UUID walkableId, final UUID studentId) {
        return manualGradingConfigurationGateway.findManualGradingComponentsByWalkable(deploymentId, walkableId)
                // filter by the deployment changeId, temporary behaviour
                // TODO: remove once https://jira.smartsparrow.com/browse/PLT-5393 is implemented
                .filter(manualGradingComponent -> manualGradingComponent.getChangeId().equals(changeId))
                .flatMap(manualComponent -> findLatestAttemptManualGradeReport(studentId, deploymentId, manualComponent.getComponentId(),
                        manualComponent.getComponentParentId(), manualComponent.getComponentParentType())
                        .flux())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }
}
