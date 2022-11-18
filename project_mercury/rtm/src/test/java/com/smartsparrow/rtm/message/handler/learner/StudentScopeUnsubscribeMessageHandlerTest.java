package com.smartsparrow.rtm.message.handler.learner;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.learner.StudentScopeUnsubscribeMessageHandler.LEARNER_STUDENT_SCOPE_UNSUBSCRIBE_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.smartsparrow.rtm.message.recv.learner.StudentScopeSubscribeMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.learner.studentscope.StudentScopeRTMSubscription;

class StudentScopeUnsubscribeMessageHandlerTest {

    @InjectMocks
    private StudentScopeUnsubscribeMessageHandler handler;

    @Mock
    private StudentScopeRTMSubscription studentScopeRTMSubscription;

    @Mock
    private StudentScopeRTMSubscription.StudentScopeRTMSubscriptionFactory studentScopeRTMSubscriptionFactory;

    @Mock
    private StudentScopeSubscribeMessage message;
    @Mock
    private Provider<RTMSubscriptionManager> subscriptionManagerProvider;
    @Mock
    private RTMSubscriptionManager subscriptionManager;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    private Session session;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID studentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        AuthenticationContext authContext = RTMWebSocketTestUtils.mockAuthenticationContext(studentId);
        when(authenticationContextProvider.get()).thenReturn(authContext);

        when(message.getDeploymentId()).thenReturn(deploymentId);

        when(subscriptionManagerProvider.get()).thenReturn(subscriptionManager);

        studentScopeRTMSubscription = new StudentScopeRTMSubscription(studentId,deploymentId);
        when(studentScopeRTMSubscriptionFactory.create(studentId,deploymentId)).thenReturn(studentScopeRTMSubscription);
        handler = new StudentScopeUnsubscribeMessageHandler(authenticationContextProvider, subscriptionManagerProvider, studentScopeRTMSubscriptionFactory);
    }

    @Test
    void validate_noDeploymentId() {
        when(message.getDeploymentId()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("deploymentId is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void handle() throws WriteResponseException, SubscriptionNotFound {
        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + LEARNER_STUDENT_SCOPE_UNSUBSCRIBE_OK + "\"}");
        verify(subscriptionManager).unsubscribe(StudentScopeRTMSubscription.NAME(studentId, deploymentId));
    }

    @Test
    void handle_error() throws SubscriptionNotFound {
        doThrow(SubscriptionNotFound.class).when(subscriptionManager)
                .unsubscribe(StudentScopeRTMSubscription.NAME(studentId, deploymentId));

        Fault t = assertThrows(Fault.class, () -> handler.handle(session, message));

        assertEquals("Subscription not found", t.getMessage());
        assertEquals(404, t.getResponseStatusCode());
    }
}
