package com.smartsparrow.rtm.message.handler.courseware.export;

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
import com.smartsparrow.rtm.message.recv.courseware.export.ExportGenericMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.export.ExportEventRTMSubscription;

class ActivityExportUnsubscribeMessageHandlerTest {

    @InjectMocks
    private ActivityExportUnsubscribeMessageHandler handler;

    @Mock
    private Provider<RTMSubscriptionManager> subscriptionManagerProvider;

    @Mock
    private RTMSubscriptionManager subscriptionManager;

    @Mock
    private ExportGenericMessage message;

    @Mock
    private ExportEventRTMSubscription.ExportEventRTMSubscriptionFactory exportEventRTMSubscriptionFactory;

    private static final UUID exportId = UUID.randomUUID();
    private static final String messageId = "MV Agusta Brutale 675";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        message = mock(ExportGenericMessage.class);
        subscriptionManager = mock(RTMSubscriptionManager.class);

        when(message.getExportId()).thenReturn(exportId);
        when(message.getId()).thenReturn(messageId);
        when(subscriptionManagerProvider.get()).thenReturn(subscriptionManager);
        when(exportEventRTMSubscriptionFactory.create(exportId)).thenReturn(new ExportEventRTMSubscription(exportId));
        handler = new ActivityExportUnsubscribeMessageHandler(subscriptionManagerProvider,exportEventRTMSubscriptionFactory);
    }

    @Test
    void validate_missingExportId() {
        when(message.getExportId()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("exportId is required", e.getMessage());
    }

    @Test
    void handle_subscriptionNotFound() throws SubscriptionNotFound, WriteResponseException {
        doThrow(SubscriptionNotFound.class).when(subscriptionManager).unsubscribe(anyString());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.export.unsubscribe.error\"," +
                "\"code\":" + HttpStatus.SC_NOT_FOUND + "," +
                "\"message\":\"" + String.format("Subscription for export %s not found", message.getExportId()) + "\"," +
                "\"replyTo\":\"" + messageId + "\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle() throws WriteResponseException, SubscriptionNotFound {
        handler.handle(session, message);
        String expected = "{\"type\":\"author.export.unsubscribe.ok\",\"replyTo\":\"" + messageId + "\"}";

        verify(subscriptionManager, atLeastOnce()).unsubscribe(anyString());
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
