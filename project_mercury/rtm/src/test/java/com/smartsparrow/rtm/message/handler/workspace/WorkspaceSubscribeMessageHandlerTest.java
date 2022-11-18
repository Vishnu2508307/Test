package com.smartsparrow.rtm.message.handler.workspace;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
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
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.workspace.WorkspaceRTMSubscription;
import com.smartsparrow.rtm.subscription.workspace.WorkspaceRTMSubscription.WorkspaceRTMSubscriptionFactory;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;

class WorkspaceSubscribeMessageHandlerTest {

    private WorkspaceSubscribeMessageHandler handler;

    @Mock
    private Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;

    @Mock
    private WorkspaceRTMSubscriptionFactory rtmSubscription;

    @Mock
    private RTMSubscriptionManager rtmSubscriptionManager;

    @Mock
    private WorkspaceGenericMessage message;

    private WorkspaceRTMSubscription workspaceRTMSubscription;

    private static final UUID workspaceId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getWorkspaceId()).thenReturn(workspaceId);
        when(rtmSubscriptionManagerProvider.get()).thenReturn(rtmSubscriptionManager);
        workspaceRTMSubscription = new WorkspaceRTMSubscription(workspaceId);
        when(rtmSubscription.create(workspaceId)).thenReturn(workspaceRTMSubscription);
        handler = new WorkspaceSubscribeMessageHandler(rtmSubscriptionManagerProvider, rtmSubscription);
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
    void handle_error() throws WriteResponseException {
        when(rtmSubscriptionManager.add(any(WorkspaceRTMSubscription.class))).thenReturn(Mono.error(new RuntimeException("err!")));

        handler.handle(session, message);

        final String expected = "{\"type\":\"workspace.subscribe.error\",\"code\":400,\"message\":\"err!\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmSubscriptionManager).add(any(WorkspaceRTMSubscription.class));

    }

    @Test
    void handle_success() throws WriteResponseException {
        when(rtmSubscriptionManager.add(rtmSubscription.create(workspaceId))).thenReturn(Mono.just(1));

        handler.handle(session, message);
        final String expected ="{\"type\":\"workspace.subscribe.ok\","+
                "\"response\":{" +
                "\"rtmSubscriptionId\":\"" + workspaceRTMSubscription.getId() +"\"" +
                "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmSubscriptionManager).add(rtmSubscription.create(workspaceId));
    }

}