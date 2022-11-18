package com.smartsparrow.rtm.message.handler.workspace;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.courseware.activity.WorkspaceGenericMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.workspace.WorkspaceRTMSubscription;
import com.smartsparrow.rtm.subscription.workspace.WorkspaceRTMSubscription.WorkspaceRTMSubscriptionFactory;
import com.smartsparrow.util.UUIDs;

class WorkspaceUnsubscribeMessageHandlerTest {

    private WorkspaceUnsubscribeMessageHandler handler;

    @Mock
    private RTMSubscriptionManager rtmSubscriptionManager;

    @Mock
    private Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;

    @Mock
    private WorkspaceRTMSubscriptionFactory workspaceRTMSubscriptionFactory;

    @Mock
    private WorkspaceGenericMessage message;

    private static final UUID workspaceId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmSubscriptionManagerProvider.get()).thenReturn(rtmSubscriptionManager);
        when(workspaceRTMSubscriptionFactory.create(workspaceId)).thenReturn(new WorkspaceRTMSubscription(workspaceId));

        when(message.getWorkspaceId()).thenReturn(workspaceId);

        handler = new WorkspaceUnsubscribeMessageHandler(rtmSubscriptionManagerProvider,
                                                         workspaceRTMSubscriptionFactory);
    }

    @Test
    void validate_nullWorkspaceId() {
        when(message.getWorkspaceId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("workspaceId is required", f.getMessage());
    }

    @Test
    void validate() {
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_success() throws WriteResponseException, SubscriptionNotFound {
        handler.handle(session, message);

        final String expected = "{\"type\":\"workspace.unsubscribe.ok\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmSubscriptionManager).unsubscribe(workspaceRTMSubscriptionFactory.create(workspaceId).getName());
    }

    @Test
    void handle_error() throws SubscriptionNotFound, WriteResponseException {
        doThrow(new RuntimeException("error")).when(rtmSubscriptionManager).unsubscribe(anyString());

        handler.handle(session, message);

        final String expected = "{\"type\":\"workspace.unsubscribe.error\",\"code\":422,\"message\":\"error unsubscribing from workspace subscription\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmSubscriptionManager).unsubscribe(workspaceRTMSubscriptionFactory.create(workspaceId).getName());
    }

}