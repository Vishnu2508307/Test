package com.smartsparrow.learner.service;

import static com.smartsparrow.learner.service.DeploymentLogServiceStub.mockLogMethods;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Feedback;
import com.smartsparrow.courseware.lang.FeedbackNotFoundException;
import com.smartsparrow.courseware.service.FeedbackService;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerFeedback;
import com.smartsparrow.learner.data.LearnerFeedbackGateway;
import com.smartsparrow.learner.data.LearnerInteractiveGateway;
import com.smartsparrow.learner.data.LearnerParentElement;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.lang.PublishFeedbackException;
import com.smartsparrow.plugin.service.PluginService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class LearnerFeedbackServiceTest {

    @InjectMocks
    private LearnerFeedbackService learnerFeedbackService;

    @Mock
    private LearnerFeedbackGateway learnerFeedbackGateway;

    @Mock
    private LearnerInteractiveGateway learnerInteractiveGateway;

    @Mock
    private FeedbackService feedbackService;

    @Mock
    private LearnerAssetService learnerAssetService;

    @Mock
    private DeploymentLogService deploymentLogService;

    @Mock
    private PluginService pluginService;

    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID feedbackId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final String pluginVersionExpr = "*.1";
    private static final String resolvedPluginVersion = "0.1";
    private static final boolean lockPluginVersionEnabled = true;
    private DeployedActivity deployment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        deployment = mock(DeployedActivity.class);

        when(deployment.getId()).thenReturn(deploymentId);
        when(deployment.getChangeId()).thenReturn(changeId);

        when(pluginService.resolvePluginVersion(pluginId, pluginVersionExpr, lockPluginVersionEnabled)).thenReturn(resolvedPluginVersion);

        when(feedbackService.findIdsByInteractive(interactiveId))
                .thenReturn(Mono.just(Lists.newArrayList(feedbackId)));

        when(feedbackService.findById(feedbackId)).thenReturn(Mono.just(new Feedback()
                .setId(feedbackId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr)));

        when(feedbackService.findLatestConfig(feedbackId)).thenReturn(Mono.just("config"));

        when(learnerAssetService.publishAssetsFor(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class)))
                .thenReturn(Flux.just(new Void[]{}));

        when(learnerAssetService.publishMathAssetsFor(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class)))
                .thenReturn(Flux.just(new Void[]{}));

        mockLogMethods(deploymentLogService);
    }

    @Test
    void publish_interactiveIdNull() {
        PublishFeedbackException e = assertThrows(PublishFeedbackException.class,
                () -> learnerFeedbackService.publish(null, deployment, false));

        assertTrue(e.getMessage().contains("interactiveId is required"));
    }

    @Test
    void publish_deploymentNull() {
        PublishFeedbackException e = assertThrows(PublishFeedbackException.class,
                () -> learnerFeedbackService.publish(interactiveId, null, false));

        assertTrue(e.getMessage().contains("deployment is required"));
    }

    @Test
    void publish_noFeedback() {
        when(feedbackService.findIdsByInteractive(interactiveId))
                .thenReturn(Mono.just(new ArrayList<>()));

        List<LearnerFeedback> learnerFeedbacks = learnerFeedbackService.publish(interactiveId, deployment, false)
                .collectList()
                .block();

        assertNotNull(learnerFeedbacks);
        assertEquals(0, learnerFeedbacks.size());
    }

    @Test
    void publish_feedbackNotFound() {
        TestPublisher<Feedback> publisher = TestPublisher.create();
        publisher.error(new FeedbackNotFoundException(feedbackId));

        when(feedbackService.findById(feedbackId)).thenReturn(publisher.mono());

        assertThrows(PublishFeedbackException.class,
                () -> learnerFeedbackService.publish(interactiveId, deployment, false).blockLast());

        verify(deploymentLogService)
                .logFailedStep(any(Deployment.class), eq(feedbackId), eq(CoursewareElementType.FEEDBACK), anyString());
    }

    @Test
    void publish_feedbackConfigNotFound() {
        when(learnerFeedbackGateway.persist(any(LearnerFeedback.class))).thenReturn(Flux.just(new Void[]{}));
        when(learnerFeedbackGateway.persistParentInteractive(any(LearnerParentElement.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerInteractiveGateway.persistChildFeedback(feedbackId, interactiveId, deploymentId, changeId))
                .thenReturn(Flux.just(new Void[]{}));

        when(feedbackService.findLatestConfig(feedbackId)).thenReturn(Mono.empty());

        List<LearnerFeedback> feedbacks = learnerFeedbackService.publish(interactiveId, deployment, false)
                .collectList()
                .block();

        assertNotNull(feedbacks);
        assertEquals(1, feedbacks.size());

        verify(deploymentLogService, never())
                .logFailedStep(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class), anyString());
        verify(deploymentLogService, never())
                .logStartedStep(any(Deployment.class), eq(feedbackId), eq(CoursewareElementType.FEEDBACK), anyString());
        verify(deploymentLogService, times(4))
                .logProgressStep(any(Deployment.class), eq(feedbackId), eq(CoursewareElementType.FEEDBACK), anyString());
        verify(deploymentLogService, never())
                .logCompletedStep(any(Deployment.class), eq(feedbackId), eq(CoursewareElementType.FEEDBACK), anyString());
    }

    @Test
    void publish() {
        when(learnerFeedbackGateway.persist(any(LearnerFeedback.class))).thenReturn(Flux.just(new Void[]{}));
        when(learnerFeedbackGateway.persistParentInteractive(any(LearnerParentElement.class)))
                .thenReturn(Flux.just(new Void[]{}));
        when(learnerInteractiveGateway.persistChildFeedback(feedbackId, interactiveId, deploymentId, changeId))
                .thenReturn(Flux.just(new Void[]{}));


        ArgumentCaptor<LearnerFeedback> feedbackCaptor = ArgumentCaptor.forClass(LearnerFeedback.class);
        ArgumentCaptor<LearnerParentElement> parentCaptor = ArgumentCaptor.forClass(LearnerParentElement.class);

        List<LearnerFeedback> learnerFeedbacks = learnerFeedbackService.publish(interactiveId, deployment, lockPluginVersionEnabled)
                .collectList()
                .block();

        assertNotNull(learnerFeedbacks);
        assertEquals(1, learnerFeedbacks.size());

        verify(learnerFeedbackGateway).persist(feedbackCaptor.capture());
        verify(learnerFeedbackGateway).persistParentInteractive(parentCaptor.capture());
        verify(learnerInteractiveGateway).persistChildFeedback(feedbackId, interactiveId, deploymentId, changeId);

        LearnerFeedback learnerFeedback = feedbackCaptor.getValue();

        assertEquals(feedbackId, learnerFeedback.getId());
        assertEquals(deploymentId, learnerFeedback.getDeploymentId());
        assertEquals(changeId, learnerFeedback.getChangeId());
        assertEquals(pluginId, learnerFeedback.getPluginId());
        assertEquals("config", learnerFeedback.getConfig());
        assertEquals(resolvedPluginVersion, learnerFeedback.getPluginVersionExpr());

        LearnerParentElement parent = parentCaptor.getValue();

        assertEquals(feedbackId, parent.getElementId());
        assertEquals(interactiveId, parent.getParentId());
        assertEquals(deploymentId, parent.getDeploymentId());
        assertEquals(changeId, parent.getChangeId());

        verify(deploymentLogService, never())
                .logFailedStep(any(Deployment.class), any(UUID.class), any(CoursewareElementType.class), anyString());
        verify(deploymentLogService, never())
                .logStartedStep(any(Deployment.class), eq(feedbackId), eq(CoursewareElementType.FEEDBACK), anyString());
        verify(deploymentLogService, times(4))
                .logProgressStep(any(Deployment.class), eq(feedbackId), eq(CoursewareElementType.FEEDBACK), anyString());
        verify(deploymentLogService, never())
                .logCompletedStep(any(Deployment.class), eq(feedbackId), eq(CoursewareElementType.FEEDBACK), anyString());
    }
}
