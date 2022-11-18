package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.service.CoursewareElementDataStub.build;
import static com.smartsparrow.learner.service.ManualGradingConfigurationDataStub.buildManualGradeEntry;
import static com.smartsparrow.learner.service.ManualGradingConfigurationDataStub.buildManualGradingConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.CompletedWalkable;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerManualGradingComponentByWalkable;
import com.smartsparrow.learner.data.ManualGradeEntry;
import com.smartsparrow.learner.data.ManualGradeEntryGateway;
import com.smartsparrow.learner.data.ScoreReason;
import com.smartsparrow.learner.data.StudentManualGrade;
import com.smartsparrow.learner.data.StudentManualGradeReport;
import com.smartsparrow.learner.lang.AttemptNotFoundFault;
import com.smartsparrow.learner.lang.ManualGradingConfigurationNotFoundFault;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ManualGradeServiceTest {

    @InjectMocks
    private ManualGradeService manualGradeService;

    @Mock
    private ManualGradeEntryGateway manualGradeEntryGateway;

    @Mock
    private ManualGradingConfigurationGateway manualGradingConfigurationGateway;

    @Mock
    private AttemptService attemptService;

    @Mock
    private CoursewareHistoryService coursewareHistoryService;

    @Mock
    private CoursewareService coursewareService;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();
    private static final UUID componentId = UUID.randomUUID();
    private final LearnerManualGradingConfiguration manualGradingConfiguration = buildManualGradingConfiguration(deploymentId, changeId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(manualGradingConfigurationGateway.persist(any(ManualGradingConfiguration.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(manualGradingConfigurationGateway.delete(any(ManualGradingConfiguration.class)))
                .thenReturn(Flux.just(new Void[]{}));
    }

    @Test
    void publishManualGradingConfiguration() {
        Deployment deployment = mock(Deployment.class);
        when(deployment.getId()).thenReturn(deploymentId);
        when(deployment.getChangeId()).thenReturn(changeId);

        when(manualGradingConfigurationGateway.persist(any(LearnerManualGradingConfiguration.class))).thenReturn(Flux.just(new Void[]{}));

        ArgumentCaptor<LearnerManualGradingConfiguration> configurationArgumentCaptor = ArgumentCaptor.forClass(LearnerManualGradingConfiguration.class);
        UUID parentId = UUID.randomUUID();
        ManualGradingConfiguration manualGradingConfiguration = new ManualGradingConfiguration()
                .setComponentId(UUID.randomUUID())
                .setMaxScore(10.5d);

        manualGradeService.publishManualGradingConfiguration(manualGradingConfiguration, deployment, parentId, CoursewareElementType.INTERACTIVE)
                .blockLast();

        verify(manualGradingConfigurationGateway).persist(configurationArgumentCaptor.capture());

        LearnerManualGradingConfiguration persisted = configurationArgumentCaptor.getValue();

        assertNotNull(persisted);
        assertEquals(deploymentId, persisted.getDeploymentId());
        assertEquals(changeId, persisted.getChangeId());
        assertEquals(parentId, persisted.getParentId());
        assertEquals(CoursewareElementType.INTERACTIVE, persisted.getParentType());
        assertEquals(manualGradingConfiguration.getComponentId(), persisted.getComponentId());
        assertEquals(manualGradingConfiguration.getMaxScore(), persisted.getMaxScore());
    }

    @Test
    void findManualGradingConfigurations_nullDeploymentId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> manualGradeService.findManualGradingConfigurations(null)
                .collectList()
                .block());

        assertNotNull(e);
        assertEquals("deploymentId is required", e.getMessage());
    }

    @Test
    void findManualGradingConfigurations() {
        when(manualGradingConfigurationGateway.findAll(deploymentId)).thenReturn(Flux.empty());

        List<LearnerManualGradingConfiguration> found = manualGradeService.findManualGradingConfigurations(deploymentId)
                .collectList()
                .block();


        assertNotNull(found);

        verify(manualGradingConfigurationGateway).findAll(deploymentId);
    }

    @Test
    void findLatestAttemptManualGradeReport_noAttempt() {
        TestPublisher<Attempt> attemptPublisher = TestPublisher.create();
        attemptPublisher.error(new AttemptNotFoundFault("attempt not found"));

        when(attemptService.findLatestAttempt(deploymentId, manualGradingConfiguration.getParentId(), studentId))
                .thenReturn(attemptPublisher.mono());

        StudentManualGradeReport report = manualGradeService.findLatestAttemptManualGradeReport(studentId,
                manualGradingConfiguration.getDeploymentId(),
                manualGradingConfiguration.getComponentId(),
                manualGradingConfiguration.getParentId(),
                manualGradingConfiguration.getParentType())
                .block();

        assertNotNull(report);
        assertEquals(deploymentId, report.getDeploymentId());
        assertNull(report.getAttemptId());
        assertTrue(report.getGrades().isEmpty());
        assertEquals(studentId, report.getStudentId());
        assertEquals(manualGradingConfiguration.getComponentId(), report.getComponentId());
        assertEquals(manualGradingConfiguration.getParentId(), report.getParentId());
        assertEquals(ScoreReason.NOT_ATTEMPTED, report.getState());
    }

    @Test
    void findLatestAttemptManualGradeReport_noEntries_withCompletedWalkables() {
        Attempt latestAttempt = new Attempt()
                .setId(UUID.randomUUID())
                .setCoursewareElementId(UUID.randomUUID());

        when(attemptService.findLatestAttempt(deploymentId, manualGradingConfiguration.getParentId(), studentId))
                .thenReturn(Mono.just(latestAttempt));

        when(manualGradeEntryGateway.findAll(deploymentId, studentId, manualGradingConfiguration.getComponentId(), latestAttempt.getId()))
                .thenReturn(Flux.empty());

        when(coursewareHistoryService.findCompletedWalkable(deploymentId, studentId, latestAttempt.getCoursewareElementId(), latestAttempt.getId()))
                .thenReturn(Mono.just(new CompletedWalkable()));

        StudentManualGradeReport report = manualGradeService.findLatestAttemptManualGradeReport(studentId,
                manualGradingConfiguration.getDeploymentId(),
                manualGradingConfiguration.getComponentId(),
                manualGradingConfiguration.getParentId(),
                manualGradingConfiguration.getParentType())
                .block();

        assertNotNull(report);
        assertEquals(deploymentId, report.getDeploymentId());
        assertEquals(studentId, report.getStudentId());
        assertEquals(latestAttempt.getId(), report.getAttemptId());
        assertTrue(report.getGrades().isEmpty());
        assertEquals(manualGradingConfiguration.getComponentId(), report.getComponentId());
        assertEquals(manualGradingConfiguration.getParentId(), report.getParentId());
        assertEquals(ScoreReason.INSTRUCTOR_UNSCORED, report.getState());
    }

    @Test
    void findLatestAttemptManualGradeReport_noEntries_noCompletedWalkables() {
        Attempt latestAttempt = new Attempt()
                .setId(UUID.randomUUID())
                .setCoursewareElementId(UUID.randomUUID());

        when(attemptService.findLatestAttempt(deploymentId, manualGradingConfiguration.getParentId(), studentId))
                .thenReturn(Mono.just(latestAttempt));

        when(manualGradeEntryGateway.findAll(deploymentId, studentId, manualGradingConfiguration.getComponentId(), latestAttempt.getId()))
                .thenReturn(Flux.empty());

        when(coursewareHistoryService.findCompletedWalkable(deploymentId, studentId, latestAttempt.getCoursewareElementId(), latestAttempt.getId()))
                .thenReturn(Mono.empty());

        StudentManualGradeReport report = manualGradeService.findLatestAttemptManualGradeReport(studentId,
                manualGradingConfiguration.getDeploymentId(),
                manualGradingConfiguration.getComponentId(),
                manualGradingConfiguration.getParentId(),
                manualGradingConfiguration.getParentType())
                .block();

        assertNotNull(report);
        assertEquals(deploymentId, report.getDeploymentId());
        assertEquals(studentId, report.getStudentId());
        assertEquals(latestAttempt.getId(), report.getAttemptId());
        assertTrue(report.getGrades().isEmpty());
        assertEquals(manualGradingConfiguration.getComponentId(), report.getComponentId());
        assertEquals(manualGradingConfiguration.getParentId(), report.getParentId());
        assertEquals(ScoreReason.INCOMPLETE_ATTEMPT, report.getState());
    }

    @Test
    void findLatestAttemptManualGradeReport() {
        Attempt latestAttempt = new Attempt()
                .setId(UUID.randomUUID());

        ManualGradeEntry manualGradeEntry = buildManualGradeEntry(manualGradingConfiguration, studentId, latestAttempt.getId());

        when(attemptService.findLatestAttempt(deploymentId, manualGradingConfiguration.getParentId(), studentId))
                .thenReturn(Mono.just(latestAttempt));

        when(manualGradeEntryGateway.findAll(deploymentId, studentId, manualGradingConfiguration.getComponentId(), latestAttempt.getId()))
                .thenReturn(Flux.just(manualGradeEntry));

        StudentManualGradeReport report = manualGradeService.findLatestAttemptManualGradeReport(studentId,
                manualGradingConfiguration.getDeploymentId(),
                manualGradingConfiguration.getComponentId(),
                manualGradingConfiguration.getParentId(),
                manualGradingConfiguration.getParentType())
                .block();

        assertNotNull(report);
        assertEquals(deploymentId, report.getDeploymentId());
        assertEquals(studentId, report.getStudentId());
        assertEquals(latestAttempt.getId(), report.getAttemptId());
        assertEquals(1, report.getGrades().size());
        assertEquals(manualGradingConfiguration.getComponentId(), report.getComponentId());
        assertEquals(manualGradingConfiguration.getParentId(), report.getParentId());
        assertEquals(ScoreReason.INSTRUCTOR_SCORED, report.getState());

        StudentManualGrade grade = report.getGrades().get(0);

        assertNotNull(grade);
        assertEquals(manualGradeEntry.getId(), grade.getId());
        assertEquals(manualGradeEntry.getScore(), grade.getScore());
        assertEquals(manualGradeEntry.getOperator(), grade.getOperator());
        assertNotNull(grade.getCreatedAt());
        assertNotNull(grade.getInstructorId());
    }

    @Test
    void findManualGradingConfiguration_nullDeploymentId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> manualGradeService
                .findManualGradingConfiguration(null, UUID.randomUUID()).block());

        assertNotNull(f);
        assertEquals("deploymentId is required", f.getMessage());
    }

    @Test
    void findManualGradingConfiguration_nullComponentId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> manualGradeService
                .findManualGradingConfiguration(UUID.randomUUID(), null).block());

        assertNotNull(f);
        assertEquals("componentId is required", f.getMessage());
    }

    @Test
    void findManualGradingConfiguration_notFound() {
        UUID deploymentId = UUID.randomUUID();
        UUID componentId= UUID.randomUUID();

        when(manualGradingConfigurationGateway.find(deploymentId, componentId))
                .thenReturn(Mono.empty());

        ManualGradingConfigurationNotFoundFault f = assertThrows(ManualGradingConfigurationNotFoundFault.class, () -> manualGradeService
                .findManualGradingConfiguration(deploymentId, componentId).block());

        assertNotNull(f);
        assertEquals("configuration not found for deployment " + deploymentId + ", component "+componentId,
                f.getMessage());
    }

    @Test
    void findManualGradingConfiguration() {
        when(manualGradingConfigurationGateway.find(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(new LearnerManualGradingConfiguration()));

        LearnerManualGradingConfiguration found = manualGradeService
                .findManualGradingConfiguration(UUID.randomUUID(), UUID.randomUUID())
                .block();

        assertNotNull(found);
    }

    @Test
    void createManualGrade_configurationNotFound() {
        when(manualGradingConfigurationGateway.find(any(UUID.class), any(UUID.class))).thenReturn(Mono.empty());

        assertThrows(ManualGradingConfigurationNotFoundFault.class, () ->
                manualGradeService.createManualGrade(UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        10d,
                        MutationOperator.SET,
                        UUID.randomUUID())
                        .block());
    }

    @Test
    void createManualGrade_scoreGreaterThanMaxScore() {
        LearnerManualGradingConfiguration conf = buildManualGradingConfiguration(UUID.randomUUID(), UUID.randomUUID());

        when(manualGradingConfigurationGateway.find(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(conf));

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> manualGradeService.createManualGrade(conf.getDeploymentId(),
                conf.getComponentId(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                100.50d,
                MutationOperator.SET,
                UUID.randomUUID())
                .block());

        assertNotNull(f);
        assertEquals("score cannot be greater than 10.0", f.getMessage());
    }

    @Test
    void createManualGrade_scoreEqualToMaxScore() {
        LearnerManualGradingConfiguration conf = buildManualGradingConfiguration(UUID.randomUUID(), UUID.randomUUID());

        when(manualGradingConfigurationGateway.find(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(conf));
        when(manualGradeEntryGateway.persist(any(ManualGradeEntry.class))).thenReturn(Flux.just(new Void[]{}));

        manualGradeService.createManualGrade(conf.getDeploymentId(),
                conf.getComponentId(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                10d,
                MutationOperator.SET,
                UUID.randomUUID())
                .block();
    }

    @Test
    void createManualGrade_scoreEqualToZero() {
        LearnerManualGradingConfiguration conf = buildManualGradingConfiguration(UUID.randomUUID(), UUID.randomUUID());

        when(manualGradingConfigurationGateway.find(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(conf));
        when(manualGradeEntryGateway.persist(any(ManualGradeEntry.class))).thenReturn(Flux.just(new Void[]{}));

        manualGradeService.createManualGrade(conf.getDeploymentId(),
                conf.getComponentId(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                0d,
                MutationOperator.SET,
                UUID.randomUUID())
                .block();
    }

    @Test
    @DisplayName("It should create a manual grade when the configuration has a null maxScore value")
    void createManualGrade_maxScoreIsNull() {
        UUID studentId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();
        ArgumentCaptor<ManualGradeEntry> captor = ArgumentCaptor.forClass(ManualGradeEntry.class);
        LearnerManualGradingConfiguration conf = buildManualGradingConfiguration(UUID.randomUUID(), UUID.randomUUID());
        // setting the max score to null
        conf.setMaxScore(null);
        when(manualGradingConfigurationGateway.find(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(conf));
        when(manualGradeEntryGateway.persist(any(ManualGradeEntry.class))).thenReturn(Flux.just(new Void[]{}));

        ManualGradeEntry created = manualGradeService.createManualGrade(conf.getDeploymentId(),
                conf.getComponentId(),
                studentId,
                attemptId,
                5.5,
                MutationOperator.SET,
                instructorId)
                .block();

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(instructorId, created.getInstructorId());
        assertEquals(MutationOperator.SET, created.getOperator());
        assertEquals(Double.valueOf(5.5), created.getScore());
        assertEquals(conf.getDeploymentId(), created.getDeploymentId());
        assertEquals(conf.getComponentId(), created.getComponentId());
        assertEquals(studentId, created.getStudentId());
        assertEquals(attemptId, created.getAttemptId());
        assertEquals(conf.getMaxScore(), created.getMaxScore());
        assertEquals(conf.getChangeId(), created.getChangeId());
        assertEquals(conf.getParentId(), created.getParentId());
        assertEquals(conf.getParentType(), created.getParentType());
        assertEquals(instructorId, created.getInstructorId());

        verify(manualGradeEntryGateway).persist(captor.capture());
    }

    @Test
    void createManualGrade_scoreNegativeNumber() {
        LearnerManualGradingConfiguration conf = buildManualGradingConfiguration(UUID.randomUUID(), UUID.randomUUID());

        when(manualGradingConfigurationGateway.find(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(conf));

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> manualGradeService.createManualGrade(conf.getDeploymentId(),
                conf.getComponentId(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                -100.50d,
                MutationOperator.SET,
                UUID.randomUUID())
                .block());

        assertNotNull(f);
        assertEquals("score cannot be a negative number", f.getMessage());
    }

    @Test
    void createManualGrade() {
        UUID studentId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        UUID instructorId = UUID.randomUUID();
        ArgumentCaptor<ManualGradeEntry> captor = ArgumentCaptor.forClass(ManualGradeEntry.class);
        LearnerManualGradingConfiguration conf = buildManualGradingConfiguration(UUID.randomUUID(), UUID.randomUUID());

        when(manualGradingConfigurationGateway.find(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(conf));
        when(manualGradeEntryGateway.persist(any(ManualGradeEntry.class))).thenReturn(Flux.just(new Void[]{}));

        ManualGradeEntry created = manualGradeService.createManualGrade(conf.getDeploymentId(),
                conf.getComponentId(),
                studentId,
                attemptId,
                5.5,
                MutationOperator.SET,
                instructorId)
                .block();

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals(instructorId, created.getInstructorId());
        assertEquals(MutationOperator.SET, created.getOperator());
        assertEquals(Double.valueOf(5.5), created.getScore());
        assertEquals(conf.getDeploymentId(), created.getDeploymentId());
        assertEquals(conf.getComponentId(), created.getComponentId());
        assertEquals(studentId, created.getStudentId());
        assertEquals(attemptId, created.getAttemptId());
        assertEquals(conf.getMaxScore(), created.getMaxScore());
        assertEquals(conf.getChangeId(), created.getChangeId());
        assertEquals(conf.getParentId(), created.getParentId());
        assertEquals(conf.getParentType(), created.getParentType());
        assertEquals(instructorId, created.getInstructorId());

        verify(manualGradeEntryGateway).persist(captor.capture());
    }

    @Test
    void createManualgradingConfiguration_nullComponentId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> manualGradeService.createManualGradingConfiguration(null, null));

        assertEquals("componentId is required", f.getMessage());
    }

    @Test
    void createManualGradingconfiguration_emptyAncestry() {
        when(coursewareService.getPath(componentId, CoursewareElementType.COMPONENT))
                .thenReturn(Mono.just(new ArrayList<>()));

        IllegalStateFault f = assertThrows(IllegalStateFault.class,
                () -> manualGradeService.createManualGradingConfiguration(componentId, null)
                        .block());

        assertEquals("the ancestry should have at least 2 elements", f.getMessage());
    }

    @Test
    void createManualGradingConfiguration() {
        CoursewareElement interactive = build(CoursewareElementType.INTERACTIVE);
        CoursewareElement activity = build(CoursewareElementType.ACTIVITY);
        ArgumentCaptor<ManualGradingComponentByWalkable> captor = ArgumentCaptor.forClass(ManualGradingComponentByWalkable.class);
        List<CoursewareElement> path = Lists.newArrayList(
                activity,
                build(CoursewareElementType.PATHWAY),
                interactive,
                build(CoursewareElementType.COMPONENT)
        );

        when(coursewareService.getPath(componentId, CoursewareElementType.COMPONENT))
                .thenReturn(Mono.just(path));

        when(manualGradingConfigurationGateway.persist(any(ManualGradingComponentByWalkable.class)))
                .thenReturn(Flux.just(new Void[]{}));

        ManualGradingConfiguration created = manualGradeService.createManualGradingConfiguration(componentId, null)
                .block();

        assertNotNull(created);
        assertEquals(componentId, created.getComponentId());
        verify(manualGradingConfigurationGateway, times(2)).persist(captor.capture());

        List<ManualGradingComponentByWalkable> persisted = captor.getAllValues();

        assertNotNull(persisted);
        assertEquals(2, persisted.size());

        ManualGradingComponentByWalkable byInteractive = persisted.get(0);
        assertNotNull(byInteractive);
        assertEquals(componentId, byInteractive.getComponentId());
        assertEquals(interactive.getElementId(), byInteractive.getWalkableId());
        assertEquals(interactive.getElementType(), byInteractive.getWalkableType());
        assertEquals(interactive.getElementId(), byInteractive.getComponentParentId());
        assertEquals(interactive.getElementType(), byInteractive.getParentComponentType());

        ManualGradingComponentByWalkable byActivity = persisted.get(1);
        assertNotNull(byActivity);
        assertEquals(componentId, byActivity.getComponentId());
        assertEquals(activity.getElementId(), byActivity.getWalkableId());
        assertEquals(activity.getElementType(), byActivity.getWalkableType());
        assertEquals(interactive.getElementId(), byActivity.getComponentParentId());
        assertEquals(interactive.getElementType(), byActivity.getParentComponentType());
    }
    @Test
    void deleteManualGradingconfiguration_nullComponentId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> manualGradeService.deleteManualGradingConfiguration(null));

        assertEquals("componentId is required", f.getMessage());
    }

    @Test
    void deleteManualGradingconfiguration_emptyAncestry() {
        when(coursewareService.getPath(componentId, CoursewareElementType.COMPONENT))
                .thenReturn(Mono.just(new ArrayList<>()));

        IllegalStateFault f = assertThrows(IllegalStateFault.class,
                () -> manualGradeService.deleteManualGradingConfiguration(componentId)
                        .blockLast());

        assertEquals("the ancestry should have at least 2 elements", f.getMessage());
    }

    @Test
    void deleteManualGradingconfiguration() {
        CoursewareElement interactive = build(CoursewareElementType.INTERACTIVE);
        CoursewareElement activity = build(CoursewareElementType.ACTIVITY);
        ArgumentCaptor<ManualGradingComponentByWalkable> captor = ArgumentCaptor.forClass(ManualGradingComponentByWalkable.class);
        List<CoursewareElement> path = Lists.newArrayList(
                activity,
                build(CoursewareElementType.PATHWAY),
                interactive,
                build(CoursewareElementType.COMPONENT)
        );

        when(coursewareService.getPath(componentId, CoursewareElementType.COMPONENT))
                .thenReturn(Mono.just(path));

        when(manualGradingConfigurationGateway.delete(any(ManualGradingComponentByWalkable.class)))
                .thenReturn(Flux.just(new Void[]{}));

        manualGradeService.deleteManualGradingConfiguration(componentId)
                .blockLast();

        verify(manualGradingConfigurationGateway, times(2)).delete(captor.capture());

        List<ManualGradingComponentByWalkable> persisted = captor.getAllValues();

        assertNotNull(persisted);
        assertEquals(2, persisted.size());

        ManualGradingComponentByWalkable byInteractive = persisted.get(0);
        assertNotNull(byInteractive);
        assertEquals(componentId, byInteractive.getComponentId());
        assertEquals(interactive.getElementId(), byInteractive.getWalkableId());
        assertEquals(interactive.getElementType(), byInteractive.getWalkableType());
        assertEquals(interactive.getElementId(), byInteractive.getComponentParentId());
        assertEquals(interactive.getElementType(), byInteractive.getParentComponentType());

        ManualGradingComponentByWalkable byActivity = persisted.get(1);
        assertNotNull(byActivity);
        assertEquals(componentId, byActivity.getComponentId());
        assertEquals(activity.getElementId(), byActivity.getWalkableId());
        assertEquals(activity.getElementType(), byActivity.getWalkableType());
        assertEquals(interactive.getElementId(), byActivity.getComponentParentId());
        assertEquals(interactive.getElementType(), byActivity.getParentComponentType());
    }

    @Test
    void publishManualcomponentByWalkable() {
        Deployment deployment = mock(Deployment.class);
        when(deployment.getId()).thenReturn(deploymentId);
        when(deployment.getChangeId()).thenReturn(changeId);
        UUID walkableId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        ArgumentCaptor<LearnerManualGradingComponentByWalkable> captor = ArgumentCaptor
                .forClass(LearnerManualGradingComponentByWalkable.class);

        when(manualGradingConfigurationGateway.findManualComponentsByWalkable(walkableId))
                .thenReturn(Flux.just(new ManualGradingComponentByWalkable()
                .setWalkableId(walkableId)
                .setComponentId(componentId)
                .setWalkableType(CoursewareElementType.ACTIVITY)
                .setComponentParentId(parentId)
                .setParentComponentType(CoursewareElementType.INTERACTIVE)));

        when(manualGradingConfigurationGateway.persist(any(LearnerManualGradingComponentByWalkable.class)))
                .thenReturn(Flux.just(new Void[]{}));

        List<LearnerManualGradingComponentByWalkable> published = manualGradeService.publishManualComponentByWalkable(walkableId, deployment)
                .collectList()
                .block();

        assertNotNull(published);
        assertEquals(1, published.size());

        verify(manualGradingConfigurationGateway).persist(captor.capture());

        LearnerManualGradingComponentByWalkable persisted = captor.getValue();
        assertNotNull(persisted);
        assertEquals(componentId, persisted.getComponentId());
        assertEquals(deploymentId, persisted.getDeploymentId());
        assertEquals(changeId, persisted.getChangeId());
        assertEquals(parentId, persisted.getComponentParentId());
        assertEquals(CoursewareElementType.ACTIVITY, persisted.getWalkableType());
        assertEquals(walkableId, persisted.getWalkableId());
        assertEquals(CoursewareElementType.INTERACTIVE, persisted.getComponentParentType());
    }

    @Test
    void findChildManualGradingConfigurationByWalkable() {
        final UUID walkableId = UUID.randomUUID();

        ManualGradingComponentByWalkable one = new ManualGradingComponentByWalkable()
                .setComponentParentId(walkableId)
                .setComponentId(UUID.randomUUID());

        ManualGradingComponentByWalkable two = new ManualGradingComponentByWalkable()
                .setComponentParentId(walkableId)
                .setComponentId(UUID.randomUUID());

        ManualGradingComponentByWalkable three = new ManualGradingComponentByWalkable()
                .setComponentParentId(UUID.randomUUID());

        when(manualGradingConfigurationGateway.findManualComponentsByWalkable(walkableId))
                .thenReturn(Flux.just(one, two, three));

        when(manualGradingConfigurationGateway.find(any(UUID.class)))
                .thenReturn(Mono.just(new ManualGradingConfiguration()));

        List<ManualGradingConfiguration> found = manualGradeService.findChildManualGradingConfigurationByWalkable(walkableId)
                .collectList()
                .block();

        assertNotNull(found);
        assertEquals(2, found.size());
    }
}
