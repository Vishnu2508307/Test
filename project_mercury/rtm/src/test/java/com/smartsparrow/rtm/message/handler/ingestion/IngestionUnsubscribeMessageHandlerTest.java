package com.smartsparrow.rtm.message.handler.ingestion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
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
import com.smartsparrow.rtm.message.recv.ingestion.IngestionGenericMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.ingestion.IngestionRTMSubscription;

class IngestionUnsubscribeMessageHandlerTest {

    @InjectMocks
    private IngestionUnsubscribeMessageHandler handler;

    @Mock
    private Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;

    @Mock
    private RTMSubscriptionManager subscriptionManager;

    @Mock
    private IngestionRTMSubscription ingestionSubscription;

    @Mock
    private IngestionRTMSubscription.IngestionRTMSubscriptionFactory ingestionRTMSubscriptionFactory;

    @Mock
    private IngestionGenericMessage message;

    private Session session;
    private static final UUID ingestionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        when(rtmSubscriptionManagerProvider.get()).thenReturn(subscriptionManager);
        when(message.getIngestionId()).thenReturn(ingestionId);


        when(ingestionSubscription.getName()).thenReturn(IngestionRTMSubscription.NAME(ingestionId));
        ingestionSubscription = new IngestionRTMSubscription(ingestionId);
        when(ingestionRTMSubscriptionFactory.create(ingestionId)).thenReturn(ingestionSubscription);
        handler = new IngestionUnsubscribeMessageHandler(rtmSubscriptionManagerProvider,
                                                         ingestionRTMSubscriptionFactory);
    }

    @Test
    void validate_missingIngestionId() {
        when(message.getIngestionId()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("ingestionId is required", e.getMessage());
    }

    @Test
    void handle_subscriptionNotFound() throws SubscriptionNotFound, WriteResponseException {
        doThrow(SubscriptionNotFound.class).when(subscriptionManager).unsubscribe(anyString());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"project.ingest.unsubscribe.error\"," +
                "\"code\":" + HttpStatus.SC_NOT_FOUND + "," +
                "\"message\":\"" + String.format("Subscription for ingestion %s not found",
                                                 message.getIngestionId()) +
                "\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle() throws WriteResponseException, SubscriptionNotFound {
        handler.handle(session, message);
        String expected = "{\"type\":\"project.ingest.unsubscribe.ok\"}";

        verify(subscriptionManager, atLeastOnce()).unsubscribe(anyString());
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
