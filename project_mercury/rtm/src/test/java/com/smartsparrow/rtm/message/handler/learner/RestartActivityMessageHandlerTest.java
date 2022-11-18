package com.smartsparrow.rtm.message.handler.learner;

import static com.smartsparrow.iam.IamTestUtils.mockAuthenticationContextProvider;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.learner.RestartActivityMessageHandler.LEARNER_ACTIVITY_RESTART_ERROR;
import static com.smartsparrow.rtm.message.handler.learner.RestartActivityMessageHandler.LEARNER_ACTIVITY_RESTART_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.progress.ActivityProgress;
import com.smartsparrow.learner.service.RestartActivityService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.learner.RestartActivityMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class RestartActivityMessageHandlerTest {

    @InjectMocks
    private RestartActivityMessageHandler handler;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private RestartActivityService restartActivityService;

    @Mock
    private RestartActivityMessage message;
    private Session session;

    private static final UUID activityId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        message = mock(RestartActivityMessage.class);
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getDeploymentId()).thenReturn(deploymentId);

        mockAuthenticationContextProvider(authenticationContextProvider, accountId);
    }

    @Test
    void validate_noActivityId() {
        when(message.getActivityId()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("activityId is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void validate_noDeploymentId() {
        when(message.getDeploymentId()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("deploymentId is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void handle() {
        when(restartActivityService.restartActivity(deploymentId, activityId, accountId)).thenReturn(Mono.just(new ActivityProgress()));
        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + LEARNER_ACTIVITY_RESTART_OK + "\"}");
    }

    @Test
    void handle_error() {
        TestPublisher<ActivityProgress> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("some exception"));
        when(restartActivityService.restartActivity(deploymentId, activityId, accountId)).thenReturn(publisher.mono());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + LEARNER_ACTIVITY_RESTART_ERROR + "\",\"code\":500," +
                "\"message\":\"unhandled error occurred to message processing\"}");
    }
}
