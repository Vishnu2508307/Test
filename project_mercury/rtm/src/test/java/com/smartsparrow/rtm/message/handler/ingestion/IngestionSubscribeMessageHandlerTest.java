package com.smartsparrow.rtm.message.handler.ingestion;

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
import com.smartsparrow.rtm.message.recv.ingestion.IngestionGenericMessage;
import com.smartsparrow.rtm.subscription.SubscriptionAlreadyExists;
import com.smartsparrow.rtm.subscription.SubscriptionLimitExceeded;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.ingestion.IngestionRTMSubscription;

import reactor.core.publisher.Mono;

class IngestionSubscribeMessageHandlerTest {

    @Mock
    private Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;

    @Mock
    private RTMSubscriptionManager subscriptionManager;

    @Mock
    private IngestionRTMSubscription ingestionSubscription;

    @Mock
    private IngestionRTMSubscription.IngestionRTMSubscriptionFactory ingestionRTMSubscriptionFactory;

    @InjectMocks
    private IngestionSubscribeMessageHandler handler;
    private Session session;
    private IngestionGenericMessage message;
    private static final UUID ingestionId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        message = mock(IngestionGenericMessage.class);
        when(rtmSubscriptionManagerProvider.get()).thenReturn(subscriptionManager);
        session = RTMWebSocketTestUtils.mockSession();
        when(ingestionSubscription.getId()).thenReturn(subscriptionId);
        when(subscriptionManager.add(any(RTMSubscription.class))).thenReturn(Mono.just(1));
        when(message.getIngestionId()).thenReturn(ingestionId);
        ingestionSubscription = new IngestionRTMSubscription(ingestionId);
        when(ingestionRTMSubscriptionFactory.create(ingestionId)).thenReturn(ingestionSubscription);
        handler = new IngestionSubscribeMessageHandler(rtmSubscriptionManagerProvider,ingestionRTMSubscriptionFactory);
    }

    @Test
    void validate_noIngestionId() {
        when(message.getIngestionId()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("ingestionId is required", e.getMessage());
    }

    @Test
    void handle_success() throws Exception {
        IngestionGenericMessage message = mock(IngestionGenericMessage.class);
        when(message.getIngestionId()).thenReturn(ingestionId);

        handler.handle(session, message);

        verify(subscriptionManager).add(ingestionSubscription);


        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"project.ingest.subscribe.ok\"," +
                "\"response\":{\"rtmSubscriptionId\":\"" + ingestionSubscription.getId() + "\"}}");
    }

    @Test
    void handle_subscriptionLimitError() {
        when(subscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionLimitExceeded()));

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () ->
                handler.handle(session, mock(IngestionGenericMessage.class)));

        assertEquals("Maximum number of subscriptions reached", t.getMessage());
    }

    @Test
    void handle_subscriptionExistsError() {
        when(subscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionAlreadyExists()));

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () ->
                handler.handle(session, mock(IngestionGenericMessage.class)));

        assertEquals("Subscription already exists", t.getMessage());
    }
}
