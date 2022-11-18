package com.smartsparrow.rtm.message.handler.learner;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.learner.StudentScopeSubscribeMessageHandler.LEARNER_STUDENT_SCOPE_SUBSCRIBE_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.learner.StudentScopeSubscribeMessage;
import com.smartsparrow.rtm.subscription.SubscriptionAlreadyExists;
import com.smartsparrow.rtm.subscription.SubscriptionLimitExceeded;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.learner.studentscope.StudentScopeRTMSubscription;

import reactor.core.publisher.Mono;

class StudentScopeSubscribeMessageHandlerTest {

    @InjectMocks
    private StudentScopeSubscribeMessageHandler handler;

    @Mock
    private StudentScopeSubscribeMessage message;
    @Mock
    private Provider<RTMSubscriptionManager> subscriptionManagerProvider;
    @Mock
    private RTMSubscriptionManager subscriptionManager;
    @Mock
    private StudentScopeRTMSubscription studentScopeSubscription;
    @Mock
    private StudentScopeRTMSubscription.StudentScopeRTMSubscriptionFactory studentScopeRTMSubscriptionFactory;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;

    private Session session;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        when(message.getDeploymentId()).thenReturn(deploymentId);
        when(subscriptionManagerProvider.get()).thenReturn(subscriptionManager);

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));

        when(studentScopeSubscription.getId()).thenReturn(subscriptionId);

        when(subscriptionManager.add(any(RTMSubscription.class))).thenReturn(Mono.just(1));

        handler = new StudentScopeSubscribeMessageHandler(authenticationContextProvider, subscriptionManagerProvider, studentScopeRTMSubscriptionFactory);
        studentScopeSubscription = new StudentScopeRTMSubscription(accountId,deploymentId);
        when(studentScopeRTMSubscriptionFactory.create(accountId,deploymentId)).thenReturn(studentScopeSubscription);
    }

    @Test
    void validate_noDeploymentId() {
        when(message.getDeploymentId()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("deploymentId is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void handle() throws WriteResponseException {
        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + LEARNER_STUDENT_SCOPE_SUBSCRIBE_OK + "\"," +
                "\"response\":{\"rtmSubscriptionId\":\"" + studentScopeSubscription.getId() + "\"}}");
        verify(subscriptionManager).add(studentScopeSubscription);

    }

    @Test
    void handle_subscriptionLimitError() {
        when(subscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionLimitExceeded()));
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.handle(session, message));
        assertEquals("Maximum number of subscriptions reached", t.getMessage());
    }

    @Test
    void handle_subscriptionExistsError() throws WriteResponseException {
        when(subscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionAlreadyExists()));
        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"learner.student.scope.subscribe.ok\"," +
                "\"response\":{\"rtmSubscriptionId\":\"" + studentScopeSubscription.getId() + "\"}}");
    }

}
