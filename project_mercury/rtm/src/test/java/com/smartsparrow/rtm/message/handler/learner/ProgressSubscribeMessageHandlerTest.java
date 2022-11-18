package com.smartsparrow.rtm.message.handler.learner;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.learner.ProgressSubscribeMessageHandler.LEARNER_PROGRESS_SUBSCRIBE_OK;
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
import com.smartsparrow.rtm.message.recv.learner.ProgressSubscribeMessage;
import com.smartsparrow.rtm.subscription.SubscriptionAlreadyExists;
import com.smartsparrow.rtm.subscription.SubscriptionLimitExceeded;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.learner.studentprogress.StudentProgressRTMSubscription;
import com.smartsparrow.rtm.subscription.learner.studentprogress.StudentProgressRTMSubscription.StudentProgressRTMSubscriptionFactory;

import reactor.core.publisher.Mono;

class ProgressSubscribeMessageHandlerTest {

    @InjectMocks
    private ProgressSubscribeMessageHandler handler;
    @Mock
    private ProgressSubscribeMessage message;
    @Mock
    private Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    @Mock
    private RTMSubscriptionManager rtmSubscriptionManager;
    @Mock
    private StudentProgressRTMSubscription studentProgressRTMSubscription;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private StudentProgressRTMSubscriptionFactory studentProgressRTMSubscriptionFactory;

    private Session session;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID coursewareElementId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        when(message.getCoursewareElementId()).thenReturn(coursewareElementId);
        when(message.getDeploymentId()).thenReturn(deploymentId);
        when(rtmSubscriptionManagerProvider.get()).thenReturn(rtmSubscriptionManager);
        when(rtmSubscriptionManager.add(any(RTMSubscription.class))).thenReturn(Mono.just(1));

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));
        when(studentProgressRTMSubscriptionFactory.create(deploymentId,coursewareElementId,accountId)).thenReturn(studentProgressRTMSubscription);
        when(studentProgressRTMSubscription.getId()).thenReturn(subscriptionId);
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
    void handle() throws WriteResponseException, SubscriptionLimitExceeded, SubscriptionAlreadyExists {
        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + LEARNER_PROGRESS_SUBSCRIBE_OK + "\"," +
                "\"response\":{\"rtmSubscriptionId\":\"" + subscriptionId + "\"}}");
        verify(studentProgressRTMSubscriptionFactory).create(deploymentId, coursewareElementId, accountId);
        verify(rtmSubscriptionManager).add(studentProgressRTMSubscription);
    }

    @Test
    void handle_subscriptionLimitError() {
        when(rtmSubscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionLimitExceeded()));
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.handle(session, message));
        assertEquals("Maximum number of subscriptions reached", t.getMessage());
    }

    @Test
    void handle_subscriptionExistsError() {
        when(rtmSubscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionAlreadyExists()));
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.handle(session, message));
        assertEquals("Subscription already exists", t.getMessage());
    }
}
