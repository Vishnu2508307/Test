package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.smartsparrow.rtm.message.handler.courseware.activity.PublishProjectActivityMessageHandler.PROJECT_ACTIVITY_PUBLISH;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.UUID;

import javax.inject.Provider;

import org.assertj.core.util.Lists;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.courseware.data.ActivityChange;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.PublishedActivityBroadcastMessage;
import com.smartsparrow.courseware.lang.ActivityChangeNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.activity.PublishActivityMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class PublishProjectActivityMessageHandlerTest {

    @InjectMocks
    private PublishProjectActivityMessageHandler handler;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private CohortService cohortService;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private RTMEventBroker rtmEventBroker;

    private PublishActivityMessage message;
    private DeployedActivity deployment;
    private ActivityChange activityChange;

    private static final String messageId = "priori";
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private static final String productId = UUID.randomUUID().toString();
    private static final boolean lockPluginVersionEnabled = true;
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(PublishActivityMessage.class);
        deployment = mock(DeployedActivity.class);
        activityChange = mock(ActivityChange.class);

        when(message.getId()).thenReturn(messageId);
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getElementType()).thenReturn(CoursewareElementType.ACTIVITY);
        when(message.getDeploymentId()).thenReturn(null);
        when(message.getCohortId()).thenReturn(cohortId);
        when(message.isLockPluginVersionEnabled()).thenReturn(lockPluginVersionEnabled);

        when(coursewareService.getParentActivityIds(activityId, CoursewareElementType.ACTIVITY))
                .thenReturn(Mono.just(Lists.newArrayList(activityId)));

        when(deploymentService.findLatestDeployment(activityId, deploymentId))
                .thenReturn(Mono.just(deployment));

        when(coursewareService.findLatestChange(activityId)).thenReturn(Mono.just(activityChange));

        when(cohortService.fetchCohortSummary(cohortId)).thenReturn(Mono.just(new CohortSummary()));
        when(cohortService.fetchCohortSettings(cohortId)).thenReturn(Mono.just(new CohortSettings().setProductId(productId)));

        when(deploymentService.saveProductDeploymentId(productId, deploymentId)).thenReturn(Mono.empty());

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
    }

    @Test
    void validate_activityIdNotSupplied() {
        when(message.getActivityId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("activityId is required", e.getErrorMessage());
        assertEquals(messageId, e.getReplyTo());
        assertEquals("project.activity.publish.error", e.getType());
    }

    @Test
    void validate_nullParentActivityIds() {
        when(coursewareService.getParentActivityIds(activityId, CoursewareElementType.ACTIVITY))
                .thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("could not verify activity", e.getErrorMessage());
        assertEquals(messageId, e.getReplyTo());
        assertEquals("project.activity.publish.error", e.getType());
    }

    @Test
    void validate_emptyParentActivityIds() {
        when(coursewareService.getParentActivityIds(activityId, CoursewareElementType.ACTIVITY))
                .thenReturn(Mono.just(new ArrayList<>()));

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("could not verify activity", e.getErrorMessage());
        assertEquals(messageId, e.getReplyTo());
        assertEquals("project.activity.publish.error", e.getType());
    }

    @Test
    void validate_notAtopLevelActivity() {
        when(coursewareService.getParentActivityIds(activityId, CoursewareElementType.ACTIVITY))
                .thenReturn(Mono.just(Lists.newArrayList(UUID.randomUUID(), activityId)));

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertTrue(e.getErrorMessage().contains("is not a top level activity"));
        assertEquals(messageId, e.getReplyTo());
        assertEquals("project.activity.publish.error", e.getType());
    }

    @Test
    void validate_deploymentNotFound() {
        when(message.getDeploymentId()).thenReturn(deploymentId);

        TestPublisher<DeployedActivity> publisher = TestPublisher.create();
        publisher.error(new DeploymentNotFoundException(activityId, deploymentId));

        when(deploymentService.findLatestDeployment(activityId, deploymentId)).thenReturn(publisher.mono());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertTrue(e.getErrorMessage().contains("deployment not found for activity"));
        assertEquals(messageId, e.getReplyTo());
        assertEquals("project.activity.publish.error", e.getType());
    }

    @Test
    void validate_deploymentNull() {
        when(message.getDeploymentId()).thenReturn(deploymentId);

        when(deploymentService.findLatestDeployment(activityId, deploymentId)).thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertTrue(e.getErrorMessage().contains("deployment not found"));
        assertEquals(messageId, e.getReplyTo());
        assertEquals("project.activity.publish.error", e.getType());
    }

    @Test
    void validate_activityChangeNotFound() {
        when(message.getDeploymentId()).thenReturn(deploymentId);

        TestPublisher<ActivityChange> publisher = TestPublisher.create();
        publisher.error(new ActivityChangeNotFoundException(activityId));

        when(coursewareService.findLatestChange(activityId)).thenReturn(publisher.mono());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertTrue(e.getErrorMessage().contains("activity change not found"));
        assertEquals(messageId, e.getReplyTo());
        assertEquals("project.activity.publish.error", e.getType());
    }

    @Test
    void validate_activityChangeNull() {
        when(message.getDeploymentId()).thenReturn(deploymentId);

        when(coursewareService.findLatestChange(activityId)).thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertTrue(e.getErrorMessage().contains("activity change is required"));
        assertEquals(messageId, e.getReplyTo());
        assertEquals("project.activity.publish.error", e.getType());
    }

    @Test
    void validate_sameChangeId() {
        when(message.getDeploymentId()).thenReturn(deploymentId);

        when(activityChange.getChangeId()).thenReturn(changeId);
        when(deployment.getChangeId()).thenReturn(changeId);

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals("no changes detected, upload action not required", e.getErrorMessage());
        assertEquals(messageId, e.getReplyTo());
        assertEquals("project.activity.publish.error", e.getType());
    }

    @Test
    @DisplayName("it should throw an exception when the cohortId field is not supplied")
    void validate_cohortNotSupplied() {
        when(message.getCohortId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals("cohortId is required", e.getErrorMessage());
        assertEquals(messageId, e.getReplyTo());
        assertEquals("project.activity.publish.error", e.getType());
    }

    @Test
    @DisplayName("it should throw an exception when the cohort is not found")
    void validate_cohortNotFound() {
        when(cohortService.fetchCohortSummary(cohortId)).thenReturn(Mono.empty());

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertTrue(e.getErrorMessage().contains("not found"));
        assertEquals(messageId, e.getReplyTo());
        assertEquals("project.activity.publish.error", e.getType());
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<DeployedActivity> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("foo"));

        when(deploymentService.deploy(activityId, cohortId, null, true))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"project.activity.publish.error\"," +
                "\"code\":422," +
                "\"message\":\"an error occurred while publishing the activity\"," +
                "\"replyTo\":\"priori\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
        verify(rtmEventBroker, never()).broadcast(any(String.class), any(PublishedActivityBroadcastMessage.class));
    }

    @Test
    void handle_success() throws WriteResponseException {
        ArgumentCaptor<PublishedActivityBroadcastMessage> captor = ArgumentCaptor.forClass(PublishedActivityBroadcastMessage.class);

        DeployedActivity deployment = new DeployedActivity()
                .setActivityId(activityId)
                .setChangeId(changeId)
                .setCohortId(cohortId)
                .setId(deploymentId);

        when(deploymentService.deploy(activityId, cohortId, null, true)).thenReturn(Mono.just(deployment));

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"project.activity.publish.ok\"," +
                "\"response\":{" +
                "\"deployment\":{" +
                "\"id\":\"" + deploymentId + "\"," +
                "\"changeId\":\"" + changeId + "\"," +
                "\"cohortId\":\"" + cohortId + "\"," +
                "\"activityId\":\"" + activityId + "\"" +
                "}" +
                "},\"replyTo\":\"priori\"}";

        verify(session.getRemote()).sendStringByFuture(expected);

        verify(rtmEventBroker).broadcast(eq(PROJECT_ACTIVITY_PUBLISH), captor.capture());

        final PublishedActivityBroadcastMessage broadcastMessage = captor.getValue();

        assertAll(() -> {
            assertNotNull(broadcastMessage);
            final DeployedActivity deployedActivity = broadcastMessage.getPublishedActivity();
            assertNotNull(deployedActivity);
            assertEquals(deploymentId, deployedActivity.getId());
            assertEquals(cohortId, deployedActivity.getCohortId());
        });
    }
}
