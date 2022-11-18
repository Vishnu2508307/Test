package com.smartsparrow.rtm.message.handler.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.workspace.ProjectGenericMessage;
import com.smartsparrow.rtm.subscription.SubscriptionAlreadyExists;
import com.smartsparrow.rtm.subscription.SubscriptionLimitExceeded;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.project.ProjectEventRTMSubscription;

import reactor.core.publisher.Mono;

class ProjectSubscribeMessageHandlerTest {

    @Mock
    private Provider<RTMSubscriptionManager> subscriptionManagerProvider;

    @Mock
    private RTMSubscriptionManager subscriptionManager;

    @Mock
    private ProjectEventRTMSubscription projectEventSubscription;

    @Mock
    private ProjectEventRTMSubscription.ProjectEventRTMSubscriptionFactory projectEventRTMSubscriptionFactory;

    @InjectMocks
    private ProjectSubscribeMessageHandler handler;
    private Session session;
    private ProjectGenericMessage message;
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        message = mock(ProjectGenericMessage.class);
        when(subscriptionManagerProvider.get()).thenReturn(subscriptionManager);
        session = RTMWebSocketTestUtils.mockSession();
        when(projectEventSubscription.getId()).thenReturn(subscriptionId);
        when(subscriptionManager.add(any(RTMSubscription.class))).thenReturn(Mono.just(1));
        when(message.getProjectId()).thenReturn(projectId);
        handler = new ProjectSubscribeMessageHandler(subscriptionManagerProvider,projectEventRTMSubscriptionFactory);
        projectEventSubscription = new ProjectEventRTMSubscription(projectId);
        when(projectEventRTMSubscriptionFactory.create(projectId)).thenReturn(projectEventSubscription);
        }

    @Test
    void validate_noProjectId() {
        when(message.getProjectId()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("projectId is required", e.getMessage());
    }

    @Test
    void handle_success() throws Exception {
        ProjectGenericMessage message = mock(ProjectGenericMessage.class);
        when(message.getProjectId()).thenReturn(projectId);

        handler.handle(session, message);

        verify(subscriptionManager).add(projectEventSubscription);


        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"workspace.project.subscribe.ok\"," +
                "\"response\":{\"rtmSubscriptionId\":\"" + projectEventSubscription.getId() + "\"}}");
    }

    @Test
    void handle_subscriptionLimitError() {
        when(subscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionLimitExceeded()));

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () ->
                handler.handle(session, mock(ProjectGenericMessage.class)));

        assertEquals("Maximum number of subscriptions reached", t.getMessage());
    }

    @Test
    void handle_subscriptionExistsError() {
        when(subscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionAlreadyExists()));

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () ->
                handler.handle(session, mock(ProjectGenericMessage.class)));

        assertEquals("Subscription already exists", t.getMessage());
    }
}
