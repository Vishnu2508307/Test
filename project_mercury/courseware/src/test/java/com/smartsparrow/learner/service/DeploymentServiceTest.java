package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.pathway.LearnerPathwayMock.mockLearnerPathway;
import static com.smartsparrow.learner.service.DeploymentLogServiceStub.mockLogMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.cache.service.CacheService;
import com.smartsparrow.courseware.data.ActivityChange;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.lang.ActivityChangeNotFoundException;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.DeploymentGateway;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerComponent;
import com.smartsparrow.learner.data.LearnerFeedback;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.LearnerScenario;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.lang.PublishActivityException;
import com.smartsparrow.learner.lang.PublishComponentException;
import com.smartsparrow.learner.lang.PublishCoursewareException;
import com.smartsparrow.learner.lang.PublishFeedbackException;
import com.smartsparrow.learner.lang.PublishInteractiveException;
import com.smartsparrow.learner.lang.PublishPathwayException;
import com.smartsparrow.learner.lang.PublishScenarioException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class DeploymentServiceTest {

    @InjectMocks
    private DeploymentService deploymentService;

    @Mock
    private DeploymentGateway deploymentGateway;

    @Mock
    private ActivityService activityService;

    @Mock
    private PathwayService pathwayService;

    @Mock
    private LearnerActivityService learnerActivityService;

    @Mock
    private LearnerScenarioService learnerScenarioService;

    @Mock
    private LearnerComponentService learnerComponentService;

    @Mock
    private LearnerPathwayService learnerPathwayService;

    @Mock
    private LearnerInteractiveService learnerInteractiveService;

    @Mock
    private LearnerFeedbackService learnerFeedbackService;

    @Mock
    private DeploymentLogService deploymentLogService;

    @Mock
    private LearnerSearchableDocumentService learnerSearchableDocumentService;

    @Mock
    private CacheService cacheService;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private static final String productId = UUID.randomUUID().toString();
    private static final boolean lockPluginVersionEnabled = true;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        ActivityChange activityChange = mock(ActivityChange.class);

        when(activityChange.getActivityId()).thenReturn(activityId);
        when(activityChange.getChangeId()).thenReturn(changeId);

        when(activityService.fetchLatestChange(activityId)).thenReturn(Mono.just(activityChange));

        when(deploymentGateway.persist(any(DeployedActivity.class))).thenReturn(Flux.just(new Void[]{}));
        // mock the top level activity deploy
        when(learnerActivityService.publish(eq(activityId), any(DeployedActivity.class), eq(null), anyBoolean()))
                .thenReturn(Mono.just(new LearnerActivity().setId(activityId)));
        when(learnerScenarioService.publish(eq(activityId), any(DeployedActivity.class)))
                .thenReturn(Flux.empty());
        when(learnerComponentService.publish(eq(activityId), any(DeployedActivity.class), eq(CoursewareElementType.ACTIVITY),
                                             anyBoolean())).thenReturn(Flux.empty());
        when(learnerPathwayService.publish(eq(activityId), any(DeployedActivity.class)))
                .thenReturn(Flux.empty());
        when(learnerSearchableDocumentService.pruneIndex(any(UUID.class)))
                .thenReturn(Mono.empty());
        when(cacheService.clearIfPresent(any())).thenReturn(Mono.just(true));

        mockLogMethods(deploymentLogService);
    }

    @Test
    void findLatestDeployment_deploymentNotFound() {
        TestPublisher<DeployedActivity> publisher = TestPublisher.create();
        publisher.error(new NoSuchElementException("not found"));
        when(deploymentGateway.findLatest(deploymentId, activityId)).thenReturn(publisher.mono());

        assertThrows(DeploymentNotFoundException.class,
                () -> deploymentService.findLatestDeployment(activityId, deploymentId).block());
    }

    @Test
    void findLatestDeploymentOrEmpty_deploymentNotFound() {
        when(deploymentGateway.findLatest(deploymentId, activityId)).thenReturn(Mono.empty());

        assertNull(deploymentService.findLatestDeploymentOrEmpty(activityId, deploymentId).block());
    }

    @Test
    void deploy_nullActivity() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> deploymentService.deploy(null, cohortId, deploymentId, lockPluginVersionEnabled));

        assertEquals("activityId is required", e.getMessage());
    }

    @Test
    @DisplayName("it should throw an exception when the supplied cohortId is null")
    void deploy_nullCohort() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                ()-> deploymentService.deploy(activityId, null, null, lockPluginVersionEnabled));

        assertEquals("cohortId is required", e.getMessage());
    }

    @Test
    void deploy_activityChangeNotFound() {
        TestPublisher<ActivityChange> publisher = TestPublisher.create();
        publisher.error(new ActivityChangeNotFoundException(activityId));

        when(activityService.fetchLatestChange(activityId)).thenReturn(publisher.mono());

        PublishCoursewareException e = assertThrows(PublishCoursewareException.class,
                () -> deploymentService.deploy(activityId, cohortId,null, lockPluginVersionEnabled).block());

        assertTrue(e.getMessage().contains("activity change not found"));
    }

    @Test
    void deploy_publishActivityException() {
        TestPublisher<LearnerActivity> publisher = TestPublisher.create();
        publisher.error(new PublishActivityException(activityId, "fubar"));
        when(learnerActivityService.publish(eq(activityId), any(DeployedActivity.class), eq(null), anyBoolean()))
                .thenReturn(publisher.mono());

        PublishCoursewareException e = assertThrows(PublishCoursewareException.class,
                () -> deploymentService.deploy(activityId, cohortId,null, lockPluginVersionEnabled).block());

        assertTrue(e.getMessage().contains("error publishing activity"));
        verify(deploymentLogService, times(2))
                .logFailedStep(any(Deployment.class), eq(activityId), eq(CoursewareElementType.ACTIVITY), anyString());
    }

    @Test
    void deploy_publishComponentException() {
        TestPublisher<LearnerComponent> publisher = TestPublisher.create();
        publisher.error(new PublishComponentException(activityId, "fubar"));
        when(learnerComponentService.publish(eq(activityId), any(DeployedActivity.class), eq(CoursewareElementType.ACTIVITY),
                                             anyBoolean())).thenReturn(publisher.flux());

        PublishCoursewareException e = assertThrows(PublishCoursewareException.class,
                () -> deploymentService.deploy(activityId, cohortId,null, lockPluginVersionEnabled).block());

        assertTrue(e.getMessage().contains("error publishing components"));
        verify(deploymentLogService, times(2))
                .logFailedStep(any(Deployment.class), eq(activityId), eq(CoursewareElementType.ACTIVITY), anyString());
    }

    @Test
    void deploy_publishScenarioException() {
        TestPublisher<LearnerScenario> publisher = TestPublisher.create();
        publisher.error(new PublishScenarioException(activityId, "fubar"));
        when(learnerScenarioService.publish(eq(activityId), any(DeployedActivity.class)))
                .thenReturn(publisher.flux());

        PublishCoursewareException e = assertThrows(PublishCoursewareException.class,
                () -> deploymentService.deploy(activityId, cohortId,null, lockPluginVersionEnabled).block());

        assertTrue(e.getMessage().contains("error publishing scenario"));
        verify(deploymentLogService, times(2))
                .logFailedStep(any(Deployment.class), eq(activityId), eq(CoursewareElementType.ACTIVITY), anyString());
    }

    @Test
    void deploy_publishPathwayException() {
        TestPublisher<LearnerPathway> publisher = TestPublisher.create();
        publisher.error(new PublishPathwayException(activityId, "fubar"));
        when(learnerPathwayService.publish(eq(activityId), any(DeployedActivity.class)))
                .thenReturn(publisher.flux());

        PublishCoursewareException e = assertThrows(PublishCoursewareException.class,
                () -> deploymentService.deploy(activityId, cohortId, null, lockPluginVersionEnabled).block());

        assertTrue(e.getMessage().contains("error publishing pathway"));
        verify(deploymentLogService, times(2))
                .logFailedStep(any(Deployment.class), eq(activityId), eq(CoursewareElementType.ACTIVITY), anyString());
    }

    @Test
    void deploy_publishInteractiveException() {
        final UUID pathwayId = UUID.randomUUID();
        final UUID interactiveId = UUID.randomUUID();

        final WalkableChild child = new WalkableChild()
                .setElementId(interactiveId)
                .setElementType(CoursewareElementType.INTERACTIVE);

        LearnerPathway learnerPathway = mockLearnerPathway(pathwayId);
        when(learnerPathwayService.publish(eq(activityId), any(DeployedActivity.class)))
                .thenReturn(Flux.just(learnerPathway));

        when(pathwayService.getOrderedWalkableChildren(pathwayId)).thenReturn(Mono.just(Lists.newArrayList(child)));

        TestPublisher<LearnerInteractive> publisher = TestPublisher.create();
        publisher.error(new PublishInteractiveException(interactiveId, "fubar"));

        when(learnerInteractiveService.publish(eq(pathwayId), eq(interactiveId), any(DeployedActivity.class),
                                               anyBoolean())).thenReturn(publisher.mono());

        PublishCoursewareException e = assertThrows(PublishCoursewareException.class,
                () -> deploymentService.deploy(activityId, cohortId,null, lockPluginVersionEnabled).block());

        assertTrue(e.getMessage().contains("error publishing interactive"));
        verify(deploymentLogService, times(2))
                .logFailedStep(any(Deployment.class), eq(activityId), eq(CoursewareElementType.ACTIVITY), anyString());
    }

    @Test
    void deploy_publishFeedbackException() {
        final UUID pathwayId = UUID.randomUUID();
        final UUID interactiveId = UUID.randomUUID();

        final WalkableChild child = new WalkableChild()
                .setElementId(interactiveId)
                .setElementType(CoursewareElementType.INTERACTIVE);

        LearnerPathway learnerPathway = mockLearnerPathway(pathwayId);
        when(learnerPathwayService.publish(eq(activityId), any(DeployedActivity.class)))
                .thenReturn(Flux.just(learnerPathway));

        when(pathwayService.getOrderedWalkableChildren(pathwayId)).thenReturn(Mono.just(Lists.newArrayList(child)));

        when(learnerInteractiveService.publish(eq(pathwayId), eq(interactiveId), any(DeployedActivity.class), anyBoolean()))
                .thenReturn(Mono.just(new LearnerInteractive().setId(interactiveId)));

        TestPublisher<LearnerFeedback> publisher = TestPublisher.create();
        publisher.error(new PublishFeedbackException(interactiveId, "fubar"));

        when(learnerFeedbackService.publish(eq(interactiveId), any(DeployedActivity.class), anyBoolean())).thenReturn(publisher.flux());

        when(learnerComponentService.publish(eq(interactiveId), any(DeployedActivity.class), eq(CoursewareElementType.INTERACTIVE),
                                             anyBoolean())).thenReturn(Flux.empty());

        PublishCoursewareException e = assertThrows(PublishCoursewareException.class,
                () -> deploymentService.deploy(activityId, cohortId,null, false).block());

        assertTrue(e.getMessage().contains("error publishing feedback"));
        verify(deploymentLogService, times(2))
                .logFailedStep(any(Deployment.class), eq(activityId), eq(CoursewareElementType.ACTIVITY), anyString());
    }

    @Test
    void deploy_infiniteRecursion() {
        final UUID pathwayId = UUID.randomUUID();

        final WalkableChild child = new WalkableChild()
                .setElementId(activityId)
                .setElementType(CoursewareElementType.ACTIVITY);

        LearnerPathway learnerPathway = mockLearnerPathway(pathwayId);
        when(learnerPathwayService.publish(eq(activityId), any(DeployedActivity.class)))
                .thenReturn(Flux.just(learnerPathway));

        when(pathwayService.getOrderedWalkableChildren(pathwayId)).thenReturn(Mono.just(Lists.newArrayList(child)));

        when(learnerActivityService.publish(eq(activityId), any(DeployedActivity.class), eq(pathwayId), anyBoolean()))
                .thenReturn(Mono.empty());

        assertThrows(StackOverflowError.class, () -> deploymentService.deploy(activityId, cohortId, null, false).block());
    }

    @Test
    void deploy_success() {
        final UUID pathwayId = UUID.randomUUID();
        final UUID interactiveId = UUID.randomUUID();

        final WalkableChild child = new WalkableChild()
                .setElementId(interactiveId)
                .setElementType(CoursewareElementType.INTERACTIVE);

        LearnerPathway learnerPathway = mockLearnerPathway(pathwayId);
        when(learnerPathwayService.publish(eq(activityId), any(DeployedActivity.class)))
                .thenReturn(Flux.just(learnerPathway));

        when(pathwayService.getOrderedWalkableChildren(pathwayId)).thenReturn(Mono.just(Lists.newArrayList(child)));

        when(learnerInteractiveService.publish(eq(pathwayId), eq(interactiveId), any(DeployedActivity.class), anyBoolean()))
                .thenReturn(Mono.just(new LearnerInteractive().setId(interactiveId)));

        when(learnerFeedbackService.publish(eq(interactiveId), any(DeployedActivity.class), anyBoolean())).thenReturn(Flux.empty());

        when(learnerComponentService.publish(eq(interactiveId), any(DeployedActivity.class), eq(CoursewareElementType.INTERACTIVE),
                                             anyBoolean())).thenReturn(Flux.empty());

        when(learnerScenarioService.publish(eq(interactiveId), any(DeployedActivity.class)))
                .thenReturn(Flux.empty());

        DeployedActivity deployment = deploymentService.deploy(activityId, cohortId, null, lockPluginVersionEnabled).block();

        assertNotNull(deployment);
        assertEquals(activityId, deployment.getActivityId());
        assertEquals(changeId, deployment.getChangeId());

        verify(deploymentLogService, times(1))
                .logStartedStep(any(Deployment.class), eq(activityId), eq(CoursewareElementType.ACTIVITY), anyString());
        verify(deploymentLogService, times(1))
                .logCompletedStep(any(Deployment.class), eq(activityId), eq(CoursewareElementType.ACTIVITY), anyString());
    }

    @Test
    void deploy_updateDeploymentSuccess() {
        final UUID pathwayId = UUID.randomUUID();
        final UUID interactiveId = UUID.randomUUID();

        final WalkableChild child = new WalkableChild()
                .setElementId(interactiveId)
                .setElementType(CoursewareElementType.INTERACTIVE);

        LearnerPathway learnerPathway = mockLearnerPathway(pathwayId);
        when(learnerPathwayService.publish(eq(activityId), any(DeployedActivity.class)))
                .thenReturn(Flux.just(learnerPathway));

        when(pathwayService.getOrderedWalkableChildren(pathwayId)).thenReturn(Mono.just(Lists.newArrayList(child)));

        when(learnerInteractiveService.publish(eq(pathwayId), eq(interactiveId), any(DeployedActivity.class), anyBoolean()))
                .thenReturn(Mono.just(new LearnerInteractive().setId(interactiveId)));

        when(learnerFeedbackService.publish(eq(interactiveId), any(DeployedActivity.class), anyBoolean())).thenReturn(Flux.empty());

        when(learnerComponentService.publish(eq(interactiveId), any(DeployedActivity.class), eq(CoursewareElementType.INTERACTIVE),
                                             anyBoolean())).thenReturn(Flux.empty());

        when(learnerScenarioService.publish(eq(interactiveId), any(DeployedActivity.class)))
                .thenReturn(Flux.empty());

        DeployedActivity deployment = deploymentService.deploy(activityId, cohortId, deploymentId, lockPluginVersionEnabled).block();

        assertNotNull(deployment);
        assertEquals(deploymentId, deployment.getId());
        assertEquals(activityId, deployment.getActivityId());
        assertEquals(changeId, deployment.getChangeId());
        assertEquals(cohortId, deployment.getCohortId());

        verify(deploymentLogService, times(1))
                .logStartedStep(any(Deployment.class), eq(activityId), eq(CoursewareElementType.ACTIVITY), anyString());
        verify(deploymentLogService, times(1))
                .logCompletedStep(any(Deployment.class), eq(activityId), eq(CoursewareElementType.ACTIVITY), anyString());
    }

    @Test
    void findLatestChangeId() {
        when(deploymentGateway.findLatestChangeId(any())).thenReturn(Mono.empty());
        deploymentService.findLatestChangeId(deploymentId).block();
        verify(deploymentGateway).findLatestChangeId(eq(deploymentId));
    }

    @Test
    void findLatestChangeIds() {
        when(deploymentGateway.findLatestChangeIds(any(), anyInt())).thenReturn(Flux.empty());
        deploymentService.findLatestChangeIds(deploymentId, 574).blockLast();
        verify(deploymentGateway).findLatestChangeIds(eq(deploymentId), eq(574));
    }

    @Test
    void findProductDeploymentId() {
        when(deploymentGateway.findDeploymentId(any())).thenReturn(Mono.empty());
        deploymentService.findProductDeploymentId(productId).block();
        verify(deploymentGateway).findDeploymentId(eq(productId));
    }

}
