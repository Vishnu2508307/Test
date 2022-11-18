package com.smartsparrow.rtm.message.handler.learner;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.learner.ProgressUnsubscribeMessageHandler.LEARNER_PROGRESS_UNSUBSCRIBE_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.exception.Fault;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.learner.ProgressSubscribeMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.learner.studentprogress.StudentProgressRTMSubscription;
import com.smartsparrow.rtm.subscription.learner.studentprogress.StudentProgressRTMSubscription.StudentProgressRTMSubscriptionFactory;
class ProgressUnsubscribeMessageHandlerTest {

    @InjectMocks
    private ProgressUnsubscribeMessageHandler handler;

    @Mock
    private ProgressSubscribeMessage message;
    @Mock
    private Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    @Mock
    private RTMSubscriptionManager rtmSubscriptionManager;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    private Session session;
    @Mock
    private StudentProgressRTMSubscription studentProgressRTMSubscription;
    @Mock
    private StudentProgressRTMSubscriptionFactory studentProgressRTMSubscriptionFactory;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID coursewareElementId = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        AuthenticationContext authContext = RTMWebSocketTestUtils.mockAuthenticationContext(studentId);
        when(rtmSubscriptionManagerProvider.get()).thenReturn(rtmSubscriptionManager);
        when(message.getDeploymentId()).thenReturn(deploymentId);
        when(message.getCoursewareElementId()).thenReturn(coursewareElementId);
        when(authenticationContextProvider.get()).thenReturn(authContext);

        when(studentProgressRTMSubscriptionFactory.create(message.getDeploymentId(),message.getCoursewareElementId(),studentId)).thenReturn(studentProgressRTMSubscription);
        when(studentProgressRTMSubscription.getName()).thenReturn(StudentProgressRTMSubscription.NAME(studentId,deploymentId,coursewareElementId));
        handler = new ProgressUnsubscribeMessageHandler(rtmSubscriptionManagerProvider,authenticationContextProvider, studentProgressRTMSubscriptionFactory);
    }

    @Test
    void validate_noDeploymentId() {
        when(message.getDeploymentId()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("deploymentId is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void validate_noCoursewareElementId() {
        when(message.getCoursewareElementId()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("coursewareElementId is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void handle() throws WriteResponseException, SubscriptionNotFound {
        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + LEARNER_PROGRESS_UNSUBSCRIBE_OK + "\"}");
        verify(rtmSubscriptionManager).unsubscribe(studentProgressRTMSubscription.getName());
    }

    @Test
    void handle_error() throws SubscriptionNotFound {
        doThrow(SubscriptionNotFound.class).when(rtmSubscriptionManager).unsubscribe(any(String.class));

        Fault t = assertThrows(Fault.class, () -> handler.handle(session, message));

        assertEquals("Subscription not found", t.getMessage());
        assertEquals(404, t.getResponseStatusCode());
    }
}
