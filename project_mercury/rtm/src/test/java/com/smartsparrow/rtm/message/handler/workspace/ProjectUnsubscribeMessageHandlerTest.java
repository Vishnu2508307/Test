package com.smartsparrow.rtm.message.handler.workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.workspace.ProjectGenericMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.project.ProjectEventRTMSubscription;

class ProjectUnsubscribeMessageHandlerTest {

    @InjectMocks
    private ProjectUnsubscribeMessageHandler handler;

    @Mock
    private Provider<RTMSubscriptionManager> subscriptionManagerProvider;

    @Mock
    private ProjectEventRTMSubscription.ProjectEventRTMSubscriptionFactory projectEventRTMSubscriptionFactory;

    @Mock
    private ProjectEventRTMSubscription projectEventRTMSubscription;


    private ProjectGenericMessage message;
    private static final UUID projectId = UUID.randomUUID();
    private static final String messageId = "MV Agusta Brutale 675";
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private RTMSubscriptionManager subscriptionManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        message = mock(ProjectGenericMessage.class);
        subscriptionManager = mock(RTMSubscriptionManager.class);

        when(message.getProjectId()).thenReturn(projectId);
        when(message.getId()).thenReturn(messageId);
        when(subscriptionManagerProvider.get()).thenReturn(subscriptionManager);
        projectEventRTMSubscription = new ProjectEventRTMSubscription(projectId);
        when(projectEventRTMSubscriptionFactory.create(projectId)).thenReturn(projectEventRTMSubscription);
        handler = new ProjectUnsubscribeMessageHandler(subscriptionManagerProvider,projectEventRTMSubscriptionFactory);
    }

    @Test
    void validate_missingProjectId() {
        when(message.getProjectId()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("projectId is required", e.getMessage());
    }

    @Test
    void handle_subscriptionNotFound() throws SubscriptionNotFound, WriteResponseException {
        doThrow(SubscriptionNotFound.class).when(subscriptionManager).unsubscribe(anyString());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"workspace.project.unsubscribe.error\"," +
                "\"code\":" + HttpStatus.SC_NOT_FOUND + "," +
                "\"message\":\"" + String.format("Subscription for project %s not found",
                                                 message.getProjectId()) + "\"," +
                "\"replyTo\":\"" + messageId + "\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle() throws WriteResponseException, SubscriptionNotFound {
        handler.handle(session, message);
        String expected = "{\"type\":\"workspace.project.unsubscribe.ok\",\"replyTo\":\"" + messageId + "\"}";

        verify(subscriptionManager, atLeastOnce()).unsubscribe(anyString());
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
