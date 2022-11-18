package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.service.CoursewareElementDataStub.build;
import static com.smartsparrow.learner.event.EvaluationEventMessageDataStub.evaluationEventMessage;
import static com.smartsparrow.learner.service.ChangeScoreActionDataStub.buildChangeScoreAction;
import static com.smartsparrow.learner.service.DeploymentDataStub.buildDeployment;
import static com.smartsparrow.learner.service.EvaluationDataStub.buildEvaluationResult;
import static com.smartsparrow.learner.service.ManualGradeEntryDataStub.createManualGradeEntry;
import static com.smartsparrow.learner.service.StudentScoreEntryDataStub.buildStudentScoreEntry;
import static com.smartsparrow.learner.service.StudentScoreEntryDataStub.studentScoreEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.score.ChangeScoreAction;
import com.smartsparrow.eval.data.ScenarioEvaluationResult;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.ManualGradeEntry;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.data.Score;
import com.smartsparrow.learner.data.ScoreReason;
import com.smartsparrow.learner.data.StudentManualGrade;
import com.smartsparrow.learner.data.StudentScoreEntry;
import com.smartsparrow.learner.data.StudentScoreGateway;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.lang.AttemptNotFoundFault;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class StudentScoreServiceTest {

    @InjectMocks
    private StudentScoreService studentScoreService;

    @Mock
    private StudentScoreGateway studentScoreGateway;

    @Mock
    private AttemptService attemptService;

    @Mock
    private EvaluationRequestService evaluationRequestService;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private LearnerCoursewareService learnerCoursewareService;

    @Mock
    private ManualGradeService manualGradeService;

    private ChangeScoreAction action;
    private EvaluationEventMessage eventMessage;
    private static final Deployment deployment = buildDeployment();
    private static final UUID clientId = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID scenarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        eventMessage = evaluationEventMessage(buildEvaluationResult(true), new ArrayList<>(),
                clientId, studentId);

        action = buildChangeScoreAction(7.0, elementId, CoursewareElementType.INTERACTIVE, MutationOperator.ADD);

        when(evaluationRequestService.findTruthful(any())).thenReturn(new ScenarioEvaluationResult()
                .setScenarioId(scenarioId));
    }

    @Test
    void create() {
        ArgumentCaptor<StudentScoreEntry> scoreEntryCaptor = ArgumentCaptor.forClass(StudentScoreEntry.class);

        when(studentScoreGateway.persist(any(StudentScoreEntry.class))).thenReturn(Mono.just(new StudentScoreEntry()));

        StudentScoreEntry created = studentScoreService.create(action, eventMessage)
                .block();

        assertNotNull(created);

        verify(studentScoreGateway).persist(scoreEntryCaptor.capture());

        StudentScoreEntry persisted = scoreEntryCaptor.getValue();

        assertNotNull(persisted);
        assertEquals(Double.valueOf(7.0), persisted.getAdjustmentValue());
        assertEquals(elementId, persisted.getElementId());
        assertEquals(CoursewareElementType.INTERACTIVE, persisted.getElementType());
        assertNotNull(persisted.getAdjustmentValue());
        assertNull(persisted.getSourceElementId());
        assertNull(persisted.getSourceAccountId());
        assertNotNull(persisted.getId());
        assertEquals(eventMessage.getAttemptId(), persisted.getAttemptId());
        assertEquals(eventMessage.getDeploymentId(), persisted.getDeploymentId());
        assertEquals(eventMessage.getChangeId(), persisted.getChangeId());
        assertEquals(eventMessage.getStudentId(), persisted.getStudentId());
        assertEquals(eventMessage.getEvaluationResult().getDeployment().getCohortId(), persisted.getCohortId());
    }

    @Test
    void create_SET_operation() {
        action = buildChangeScoreAction(10d, elementId, CoursewareElementType.INTERACTIVE, MutationOperator.SET);

        when(studentScoreGateway.find(any(UUID.class), eq(studentId), eq(elementId), any(UUID.class)))
                .thenReturn(Flux.just(
                        studentScoreEntry(3d)
                ));

        ArgumentCaptor<StudentScoreEntry> scoreEntryCaptor = ArgumentCaptor.forClass(StudentScoreEntry.class);

        when(studentScoreGateway.persist(any(StudentScoreEntry.class))).thenReturn(Mono.just(new StudentScoreEntry()));

        StudentScoreEntry created = studentScoreService.create(action, eventMessage)
                .block();

        assertNotNull(created);

        verify(studentScoreGateway).persist(scoreEntryCaptor.capture());

        StudentScoreEntry persisted = scoreEntryCaptor.getValue();

        assertNotNull(persisted);
        assertEquals(Double.valueOf(7.0), persisted.getAdjustmentValue());

    }

    @Test
    void create_SET_operation_scoreEntriesNotFound() {
        action = buildChangeScoreAction(10d, elementId, CoursewareElementType.INTERACTIVE, MutationOperator.SET);

        when(studentScoreGateway.find(any(UUID.class), eq(studentId), eq(elementId), any(UUID.class)))
                .thenReturn(Flux.empty());

        ArgumentCaptor<StudentScoreEntry> scoreEntryCaptor = ArgumentCaptor.forClass(StudentScoreEntry.class);

        when(studentScoreGateway.persist(any(StudentScoreEntry.class))).thenReturn(Mono.just(new StudentScoreEntry()));

        StudentScoreEntry created = studentScoreService.create(action, eventMessage)
                .block();

        assertNotNull(created);

        verify(studentScoreGateway).persist(scoreEntryCaptor.capture());

        StudentScoreEntry persisted = scoreEntryCaptor.getValue();

        assertNotNull(persisted);
        assertEquals(Double.valueOf(10d), persisted.getAdjustmentValue());
    }

    @Test
    void create_SET_operation_negativeFirstValue() {
        action = buildChangeScoreAction(10d, elementId, CoursewareElementType.INTERACTIVE, MutationOperator.SET);

        when(studentScoreGateway.find(any(UUID.class), eq(studentId), eq(elementId), any(UUID.class)))
                .thenReturn(Flux.just(
                        studentScoreEntry(-5d),
                        studentScoreEntry(2d)
                ));

        ArgumentCaptor<StudentScoreEntry> scoreEntryCaptor = ArgumentCaptor.forClass(StudentScoreEntry.class);

        when(studentScoreGateway.persist(any(StudentScoreEntry.class))).thenReturn(Mono.just(new StudentScoreEntry()));

        StudentScoreEntry created = studentScoreService.create(action, eventMessage)
                .block();

        assertNotNull(created);

        verify(studentScoreGateway).persist(scoreEntryCaptor.capture());

        StudentScoreEntry persisted = scoreEntryCaptor.getValue();

        assertNotNull(persisted);
        assertEquals(Double.valueOf(8), persisted.getAdjustmentValue());
    }

    @Test
    void create_SET_operationWithNegativeValue() {
        action = buildChangeScoreAction(-10d, elementId, CoursewareElementType.INTERACTIVE, MutationOperator.SET);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> studentScoreService.create(action, eventMessage)
                .block());

        assertNotNull(e);
        assertEquals("Negative value is not allowed with a SET operator", e.getMessage());
    }

    @Test
    void fetchScoreEntries_nullAttemptId() {
        Attempt attempt = new Attempt()
                .setId(UUID.randomUUID());

        when(attemptService.findLatestAttempt(deployment.getId(), elementId, studentId))
                .thenReturn(Mono.just(attempt));

        when(studentScoreGateway.find(any(UUID.class), eq(studentId), eq(elementId),
                eq(attempt.getId())))
                .thenReturn(Flux.just(
                        studentScoreEntry(1.0)
                ));

        List<StudentScoreEntry> all = studentScoreService
                .fetchScoreEntries(deployment.getId(), studentId, elementId, null)
                .collectList()
                .block();

        assertNotNull(all);
        assertEquals(1, all.size());

        verify(attemptService).findLatestAttempt(deployment.getId(), elementId, studentId);
    }

    @Test
    void fetchScoreEntries() {
        when(studentScoreGateway.find(any(UUID.class), eq(studentId), eq(elementId), any(UUID.class)))
                .thenReturn(Flux.just(
                        studentScoreEntry(1.0)
                ));

        List<StudentScoreEntry> all = studentScoreService
                .fetchScoreEntries(deployment.getId(), studentId, elementId, UUID.randomUUID())
                .collectList()
                .block();

        assertNotNull(all);
        assertEquals(1, all.size());

        verify(attemptService, never()).findLatestAttempt(any(UUID.class), any(UUID.class), any(UUID.class));
    }

    @Test
    void computeScore_negativeValueFirst() {
        when(studentScoreGateway.find(any(UUID.class), eq(studentId), eq(elementId), any(UUID.class)))
                .thenReturn(Flux.just(
                        studentScoreEntry(-5.0),
                        studentScoreEntry(7d),
                        studentScoreEntry(-3d)
                ));

        Score result = studentScoreService.computeScore(deployment.getId(), studentId, elementId, UUID.randomUUID())
                .block();

        assertNotNull(result);
        assertEquals(Double.valueOf(4), result.getValue());
    }

    @Test
    void computeScore_negativeValueMiddle() {
        when(studentScoreGateway.find(any(UUID.class), eq(studentId), eq(elementId), any(UUID.class)))
                .thenReturn(Flux.just(
                        studentScoreEntry(5d),
                        studentScoreEntry(7d),
                        studentScoreEntry(-15d),
                        studentScoreEntry(2d)
                ));

        Score result = studentScoreService.computeScore(deployment.getId(), studentId, elementId, UUID.randomUUID())
                .block();

        assertNotNull(result);
        assertEquals(Double.valueOf(2), result.getValue());
    }

    @Test
    void computeScore_negativeResult() {
        when(studentScoreGateway.find(any(UUID.class), eq(studentId), eq(elementId), any(UUID.class)))
                .thenReturn(Flux.just(
                        studentScoreEntry(5d),
                        studentScoreEntry(7d),
                        studentScoreEntry(-15d)
                ));

        Score result = studentScoreService.computeScore(deployment.getId(), studentId, elementId, UUID.randomUUID())
                .block();

        assertNotNull(result);
        assertEquals(Double.valueOf(0), result.getValue());
    }

    @Test
    void computeScore_oneNegativeEntry() {
        when(studentScoreGateway.find(any(UUID.class), eq(studentId), eq(elementId), any(UUID.class)))
                .thenReturn(Flux.just(
                        studentScoreEntry(-5d)
                ));

        Score result = studentScoreService.computeScore(deployment.getId(), studentId, elementId, UUID.randomUUID())
                .block();

        assertNotNull(result);
        assertEquals(Double.valueOf(0), result.getValue());
    }

    @Test
    void computeScore_doesNotGoBelowZero() {
        when(studentScoreGateway.find(any(UUID.class), eq(studentId), eq(elementId), any(UUID.class)))
                .thenReturn(Flux.just(
                        studentScoreEntry(5d),
                        studentScoreEntry(7d),
                        studentScoreEntry(-3d),
                        studentScoreEntry(17d)
                ));

        Score result = studentScoreService.computeScore(deployment.getId(), studentId, elementId, UUID.randomUUID())
                .block();

        assertNotNull(result);
        assertEquals(Double.valueOf(26), result.getValue());
    }

    @Test
    void computeScore_withDecimalValues() {
        when(studentScoreGateway.find(any(UUID.class), eq(studentId), eq(elementId), any(UUID.class)))
                .thenReturn(Flux.just(
                        studentScoreEntry(5.15),
                        studentScoreEntry(7.55),
                        studentScoreEntry(-3.5),
                        studentScoreEntry(17.345)
                ));

        Score result = studentScoreService.computeScore(deployment.getId(), studentId, elementId, UUID.randomUUID())
                .block();

        assertNotNull(result);
        // result with no decimal format 26.544999999999998
        assertEquals(Double.valueOf(26.545), result.getValue());
    }

    @Test
    void createAncestorEntry() {
        ArgumentCaptor<StudentScoreEntry> ancestorEntryCaptor = ArgumentCaptor.forClass(StudentScoreEntry.class);

        final UUID attemptId = UUID.randomUUID();
        StudentScoreEntry originalEntry = buildStudentScoreEntry(10d, MutationOperator.ADD, deployment, studentId);
        when(studentScoreGateway.persist(any(StudentScoreEntry.class))).thenReturn(Mono.just(new StudentScoreEntry()));

        CoursewareElement ancestorElement = build(CoursewareElementType.PATHWAY);
        when(attemptService.findLatestAttempt(deployment.getId(), ancestorElement.getElementId(), studentId))
                .thenReturn(Mono.just(new Attempt().setId(attemptId)));

        StudentScoreEntry ancestorScoreEntry = studentScoreService.createAncestorEntry(originalEntry, ancestorElement)
                .block();

        assertNotNull(ancestorScoreEntry);

        verify(studentScoreGateway).persist(ancestorEntryCaptor.capture());

        StudentScoreEntry persistedAncestorEntry = ancestorEntryCaptor.getValue();

        assertNotNull(persistedAncestorEntry);
        assertEquals(deployment.getCohortId(), persistedAncestorEntry.getCohortId());
        assertEquals(deployment.getId(), persistedAncestorEntry.getDeploymentId());
        assertEquals(deployment.getChangeId(), persistedAncestorEntry.getChangeId());
        assertEquals(studentId, persistedAncestorEntry.getStudentId());
        assertEquals(ancestorElement.getElementId(), persistedAncestorEntry.getElementId());
        assertEquals(ancestorElement.getElementType(), persistedAncestorEntry.getElementType());
        assertEquals(attemptId, persistedAncestorEntry.getAttemptId());
        assertNotNull(persistedAncestorEntry.getId());
        assertEquals(Double.valueOf(10d), persistedAncestorEntry.getValue());
        assertEquals(Double.valueOf(10d), persistedAncestorEntry.getAdjustmentValue());
        assertEquals(originalEntry.getElementId(), persistedAncestorEntry.getSourceElementId());
        assertEquals(originalEntry.getSourceScenarioId(), persistedAncestorEntry.getSourceScenarioId());
        assertNull(persistedAncestorEntry.getSourceAccountId());
        assertEquals(MutationOperator.ADD, persistedAncestorEntry.getOperator());
    }

    @Test
    void createAncestorEntry_SET_noScoreFound() {
        ArgumentCaptor<StudentScoreEntry> ancestorEntryCaptor = ArgumentCaptor.forClass(StudentScoreEntry.class);

        final UUID attemptId = UUID.randomUUID();
        StudentScoreEntry originalEntry = buildStudentScoreEntry(10d, MutationOperator.SET, deployment, studentId);
        when(studentScoreGateway.persist(any(StudentScoreEntry.class))).thenReturn(Mono.just(new StudentScoreEntry()));
        CoursewareElement ancestorElement = build(CoursewareElementType.PATHWAY);
        when(studentScoreGateway.find(deployment.getId(), studentId, ancestorElement.getElementId(), attemptId))
                .thenReturn(Flux.empty());

        when(attemptService.findLatestAttempt(deployment.getId(), ancestorElement.getElementId(), studentId))
                .thenReturn(Mono.just(new Attempt().setId(attemptId)));

        StudentScoreEntry ancestorScoreEntry = studentScoreService.createAncestorEntry(originalEntry, ancestorElement)
                .block();

        assertNotNull(ancestorScoreEntry);

        verify(studentScoreGateway).persist(ancestorEntryCaptor.capture());

        StudentScoreEntry persistedAncestorEntry = ancestorEntryCaptor.getValue();

        assertNotNull(persistedAncestorEntry);
        assertEquals(deployment.getCohortId(), persistedAncestorEntry.getCohortId());
        assertEquals(deployment.getId(), persistedAncestorEntry.getDeploymentId());
        assertEquals(deployment.getChangeId(), persistedAncestorEntry.getChangeId());
        assertEquals(studentId, persistedAncestorEntry.getStudentId());
        assertEquals(ancestorElement.getElementId(), persistedAncestorEntry.getElementId());
        assertEquals(ancestorElement.getElementType(), persistedAncestorEntry.getElementType());
        assertEquals(attemptId, persistedAncestorEntry.getAttemptId());
        assertNotNull(persistedAncestorEntry.getId());
        assertEquals(Double.valueOf(10d), persistedAncestorEntry.getValue());
        assertEquals(Double.valueOf(10d), persistedAncestorEntry.getAdjustmentValue());
        assertEquals(originalEntry.getElementId(), persistedAncestorEntry.getSourceElementId());
        assertEquals(originalEntry.getSourceScenarioId(), persistedAncestorEntry.getSourceScenarioId());
        assertNull(persistedAncestorEntry.getSourceAccountId());
        assertEquals(MutationOperator.SET, persistedAncestorEntry.getOperator());
    }

    @Test
    void createAncestorEntry_SET_previousScoreEntriesFound() {
        ArgumentCaptor<StudentScoreEntry> ancestorEntryCaptor = ArgumentCaptor.forClass(StudentScoreEntry.class);

        final UUID attemptId = UUID.randomUUID();
        StudentScoreEntry originalEntry = buildStudentScoreEntry(10d, MutationOperator.SET, deployment, studentId);
        when(studentScoreGateway.persist(any(StudentScoreEntry.class))).thenReturn(Mono.just(new StudentScoreEntry()));
        CoursewareElement ancestorElement = build(CoursewareElementType.PATHWAY);
        when(studentScoreGateway.find(deployment.getId(), studentId, ancestorElement.getElementId(), attemptId))
                .thenReturn(Flux.just(
                        studentScoreEntry(-5d),
                        studentScoreEntry(7d),
                        studentScoreEntry(-3d),
                        studentScoreEntry(17d)
                ));

        when(attemptService.findLatestAttempt(deployment.getId(), ancestorElement.getElementId(), studentId))
                .thenReturn(Mono.just(new Attempt().setId(attemptId)));

        StudentScoreEntry ancestorScoreEntry = studentScoreService.createAncestorEntry(originalEntry, ancestorElement)
                .block();

        assertNotNull(ancestorScoreEntry);

        verify(studentScoreGateway).persist(ancestorEntryCaptor.capture());

        StudentScoreEntry persistedAncestorEntry = ancestorEntryCaptor.getValue();

        assertNotNull(persistedAncestorEntry);
        assertEquals(deployment.getCohortId(), persistedAncestorEntry.getCohortId());
        assertEquals(deployment.getId(), persistedAncestorEntry.getDeploymentId());
        assertEquals(deployment.getChangeId(), persistedAncestorEntry.getChangeId());
        assertEquals(studentId, persistedAncestorEntry.getStudentId());
        assertEquals(ancestorElement.getElementId(), persistedAncestorEntry.getElementId());
        assertEquals(ancestorElement.getElementType(), persistedAncestorEntry.getElementType());
        assertEquals(attemptId, persistedAncestorEntry.getAttemptId());
        assertNotNull(persistedAncestorEntry.getId());
        assertEquals(Double.valueOf(10d), persistedAncestorEntry.getValue());
        assertEquals(originalEntry.getElementId(), persistedAncestorEntry.getSourceElementId());
        assertEquals(originalEntry.getSourceScenarioId(), persistedAncestorEntry.getSourceScenarioId());
        assertNull(persistedAncestorEntry.getSourceAccountId());
        assertEquals(MutationOperator.SET, persistedAncestorEntry.getOperator());
    }

    @Test
    void computeScore_SCORED() {
        when(studentScoreGateway.find(any(UUID.class), eq(studentId), eq(elementId), any(UUID.class)))
                .thenReturn(Flux.just(
                        studentScoreEntry(5d),
                        studentScoreEntry(7d),
                        studentScoreEntry(-3d),
                        studentScoreEntry(17d)
                ));

        Score result = studentScoreService.computeScore(deployment.getId(), studentId, elementId, UUID.randomUUID())
                .block();

        assertNotNull(result);
        assertEquals(Double.valueOf(26), result.getValue());
        assertEquals(ScoreReason.SCORED, result.getReason());
    }

    @Test
    void computeScore_UNSCORED() {
        when(studentScoreGateway.find(any(UUID.class), eq(studentId), eq(elementId), any(UUID.class)))
                .thenReturn(Flux.empty());

        Score result = studentScoreService.computeScore(deployment.getId(), studentId, elementId, UUID.randomUUID())
                .block();

        assertNotNull(result);
        assertEquals(Double.valueOf(0), result.getValue());
        assertEquals(ScoreReason.UNSCORED, result.getReason());
    }


    @Test
    void computeScore_NOT_ATTEMPTED() {
        TestPublisher<Attempt> publisher = TestPublisher.create();
        publisher.error(new AttemptNotFoundFault("attempt not found"));

        when(attemptService.findLatestAttempt(deployment.getId(), elementId, studentId)).thenReturn(publisher.mono());
        when(studentScoreGateway.find(any(UUID.class), eq(studentId), eq(elementId), any(UUID.class)))
                .thenReturn(Flux.empty());

        Score result = studentScoreService.computeScore(deployment.getId(), studentId, elementId, null)
                .block();

        assertNotNull(result);
        assertEquals(Double.valueOf(0), result.getValue());
        assertEquals(ScoreReason.NOT_ATTEMPTED, result.getReason());
    }

    @Test
    void rollUpScoreEntries() {
        List<CoursewareElement> ancestry = Lists.newArrayList(
                build(CoursewareElementType.INTERACTIVE),
                build(CoursewareElementType.PATHWAY)
        );

        StudentScoreEntry originalEntry = buildStudentScoreEntry(10d, MutationOperator.ADD, deployment, studentId);

        when(attemptService.findLatestAttempt(eq(deployment.getId()), any(UUID.class), eq(originalEntry.getStudentId())))
                .thenReturn(Mono.just(new Attempt().setId(UUID.randomUUID())));

        when(studentScoreGateway.persist(any(StudentScoreEntry.class))).thenReturn(Mono.just(new StudentScoreEntry()));
        List<StudentScoreEntry> rolledUp = studentScoreService.rollUpScoreEntries(originalEntry, ancestry)
                .collectList()
                .block();

        assertNotNull(rolledUp);
        assertEquals(ancestry.size(), rolledUp.size());

        verify(studentScoreGateway, times(2)).persist(any(StudentScoreEntry.class));
    }

    @Test
    void createFromManualGrade_deploymentNotFound() {
        TestPublisher<DeployedActivity> publisher = TestPublisher.create();
        publisher.error(new DeploymentNotFoundException(null, deployment.getId()));

        when(deploymentService.findDeployment(deployment.getId())).thenReturn(publisher.mono());

        ManualGradeEntry manualGradeEntry = createManualGradeEntry(deployment.getId());

        NotFoundFault f = assertThrows(NotFoundFault.class, () -> studentScoreService.create(manualGradeEntry)
                .block());

        assertNotNull(f);
        assertEquals("deployment not found", f.getMessage());
    }

    @Test
    void createFromManualGrade() {
        ArgumentCaptor<StudentScoreEntry> captor = ArgumentCaptor.forClass(StudentScoreEntry.class);
        UUID cohortId = UUID.randomUUID();
        when(deploymentService.findDeployment(deployment.getId())).thenReturn(Mono.just(new DeployedActivity()
                .setCohortId(cohortId)));

        when(studentScoreGateway.persist(any(StudentScoreEntry.class))).thenReturn(Mono.just(new StudentScoreEntry()));

        ManualGradeEntry manualGradeEntry = createManualGradeEntry(deployment.getId());

        studentScoreService.create(manualGradeEntry)
                .block();

        verify(studentScoreGateway).persist(captor.capture());

        StudentScoreEntry persisted = captor.getValue();

        assertNotNull(persisted);
        assertEquals(cohortId, persisted.getCohortId());
        assertEquals(manualGradeEntry.getDeploymentId(), persisted.getDeploymentId());
        assertEquals(manualGradeEntry.getChangeId(), persisted.getChangeId());
        assertEquals(manualGradeEntry.getStudentId(), persisted.getStudentId());
        assertEquals(manualGradeEntry.getComponentId(), persisted.getElementId());
        assertEquals(CoursewareElementType.COMPONENT, persisted.getElementType());
        assertEquals(manualGradeEntry.getAttemptId(), persisted.getAttemptId());
        assertNotNull(persisted.getId());
        assertNull(persisted.getEvaluationId());
        assertEquals(manualGradeEntry.getScore(), persisted.getValue());
        assertEquals(manualGradeEntry.getScore(), persisted.getAdjustmentValue());
        assertNull(persisted.getSourceElementId());
        assertNull(persisted.getSourceScenarioId());
        assertEquals(manualGradeEntry.getInstructorId(), persisted.getSourceAccountId());
        assertEquals(manualGradeEntry.getOperator(), persisted.getOperator());
    }

    @Test
    void createManualGrade() {
        ArgumentCaptor<StudentScoreEntry> captor = ArgumentCaptor.forClass(StudentScoreEntry.class);
        ManualGradeEntry manualGradeEntry = createManualGradeEntry(deployment.getId());

        when(deploymentService.findDeployment(deployment.getId())).thenReturn(Mono.just(new DeployedActivity()
                .setCohortId(UUID.randomUUID())
                .setId(deployment.getId())));

        List<CoursewareElement> ancestry = Lists.newArrayList(
                new CoursewareElement()
                        .setElementId(manualGradeEntry.getComponentId())
                        .setElementType(CoursewareElementType.COMPONENT),
                build(CoursewareElementType.INTERACTIVE),
                build(CoursewareElementType.PATHWAY)
        );

        when(manualGradeService.createManualGrade(deployment.getId(),
                manualGradeEntry.getComponentId(),
                manualGradeEntry.getStudentId(),
                manualGradeEntry.getAttemptId(),
                manualGradeEntry.getScore(),
                manualGradeEntry.getOperator(),
                manualGradeEntry.getInstructorId())).thenReturn(Mono.just(manualGradeEntry));

        when(learnerCoursewareService.getAncestry(deployment.getId(), manualGradeEntry.getComponentId(), CoursewareElementType.COMPONENT))
                .thenReturn(Mono.just(ancestry));

        when(attemptService.findLatestAttempt(eq(deployment.getId()), any(UUID.class), eq(manualGradeEntry.getStudentId())))
                .thenReturn(Mono.just(new Attempt().setId(UUID.randomUUID())));

        when(studentScoreGateway.persist(any(StudentScoreEntry.class))).thenReturn(Mono.just(new StudentScoreEntry()
                .setStudentId(manualGradeEntry.getStudentId())
                .setDeploymentId(deployment.getId())
                .setOperator(manualGradeEntry.getOperator())
                .setValue(manualGradeEntry.getScore())));

        StudentManualGrade created = studentScoreService.createStudentManualGrade(deployment.getId(),
                manualGradeEntry.getComponentId(),
                manualGradeEntry.getStudentId(),
                manualGradeEntry.getAttemptId(),
                manualGradeEntry.getScore(),
                manualGradeEntry.getOperator(),
                manualGradeEntry.getInstructorId())
                .block();

        assertNotNull(created);
        assertNotNull(created.getId());
        assertNotNull(created.getCreatedAt());
        assertEquals(manualGradeEntry.getInstructorId(), created.getInstructorId());
        assertEquals(manualGradeEntry.getScore(), created.getScore());
        assertEquals(manualGradeEntry.getOperator(), created.getOperator());

        verify(studentScoreGateway, times(3)).persist(captor.capture());

        List<StudentScoreEntry> entries = captor.getAllValues();

        assertNotNull(entries);
        assertEquals(3, entries.size());
    }

    @Test
    void getAdjustmentValue_ADD() {
        Double adjustmentValue = studentScoreService
                .getAdjustmentValue(MutationOperator.ADD, 10.d, deployment.getId(), elementId, studentId, null)
                .block();

        assertEquals(Double.valueOf(10), adjustmentValue);
    }

    @Test
    void getAdjustmentValue_REMOVE() {
        Double adjustmentValue = studentScoreService
                .getAdjustmentValue(MutationOperator.REMOVE, 10.d, deployment.getId(), elementId, studentId, null)
                .block();

        assertEquals(Double.valueOf(-10), adjustmentValue);
    }

    @Test
    void getAdjustmentValue_SET() {
        UUID attemptId = UUID.randomUUID();
        when(studentScoreGateway.find(deployment.getId(), studentId, elementId, attemptId))
                .thenReturn(Flux.just(buildStudentScoreEntry(20d, MutationOperator.SET, deployment, studentId)));
        Double adjustmentValue = studentScoreService
                .getAdjustmentValue(MutationOperator.SET, 10.d, deployment.getId(), elementId, studentId, attemptId)
                .block();

        assertEquals(Double.valueOf(-10d), adjustmentValue);
    }

    @Test
    void getAdjustmentValue_SET_withExistingAdd() {
        UUID attemptId = UUID.randomUUID();
        when(studentScoreGateway.find(deployment.getId(), studentId, elementId, attemptId))
                .thenReturn(Flux.just(
                        buildStudentScoreEntry(20d, MutationOperator.ADD, deployment, studentId),
                        buildStudentScoreEntry(20d, MutationOperator.ADD, deployment, studentId)
                ));
        Double adjustmentValue = studentScoreService
                .getAdjustmentValue(MutationOperator.SET, 10.d, deployment.getId(), elementId, studentId, attemptId)
                .block();

        assertEquals(Double.valueOf(-30d), adjustmentValue);
    }
}