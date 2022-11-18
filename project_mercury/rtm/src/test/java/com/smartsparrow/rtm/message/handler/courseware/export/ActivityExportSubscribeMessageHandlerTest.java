package com.smartsparrow.rtm.message.handler.courseware.export;

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
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.export.ExportGenericMessage;
import com.smartsparrow.rtm.subscription.SubscriptionAlreadyExists;
import com.smartsparrow.rtm.subscription.SubscriptionLimitExceeded;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.export.ExportEventRTMSubscription;

import reactor.core.publisher.Mono;

class ActivityExportSubscribeMessageHandlerTest {

    @Mock
    private Provider<RTMSubscriptionManager> subscriptionManagerProvider;

    @Mock
    private RTMSubscriptionManager subscriptionManager;

    @Mock
    private ExportEventRTMSubscription exportEventSubscription;

    @Mock
    private ExportEventRTMSubscription.ExportEventRTMSubscriptionFactory exportEventRTMSubscriptionFactory;

    @Mock
    private ExportGenericMessage message;

    @InjectMocks
    private ActivityExportSubscribeMessageHandler handler;

    private Session session;
    private final static UUID subscriptionId = UUID.randomUUID();
    private final static UUID exportId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(subscriptionManagerProvider.get()).thenReturn(subscriptionManager);
        session = RTMWebSocketTestUtils.mockSession();
        when(exportEventSubscription.getId()).thenReturn(subscriptionId);
        when(subscriptionManager.add(any(RTMSubscription.class))).thenReturn(Mono.just(1));

        when(message.getExportId()).thenReturn(exportId);
        handler = new ActivityExportSubscribeMessageHandler(subscriptionManagerProvider,exportEventRTMSubscriptionFactory);
        exportEventSubscription = new ExportEventRTMSubscription(exportId);
        when(exportEventRTMSubscriptionFactory.create(exportId)).thenReturn(exportEventSubscription);
    }

    @Test
    void validate_noExportId() {
        when(message.getExportId()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("exportId is required", e.getMessage());
    }

    @Test
    void handle_success() throws Exception {
        handler.handle(session, message);

        verify(subscriptionManager).add(exportEventSubscription);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"author.export.subscribe.ok\"," +
                "\"response\":{\"rtmSubscriptionId\":\"" + exportEventSubscription.getId() +"\"}}");
    }

    @Test
    void handle_subscriptionLimitError() {
        when(subscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionLimitExceeded()));

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () ->
                handler.handle(session, message));

        assertEquals("Maximum number of subscriptions reached", t.getMessage());
    }

    @Test
    void handle_subscriptionExistsError() {
        when(subscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionAlreadyExists()));

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () ->
                handler.handle(session, message));

        assertEquals("Subscription already exists", t.getMessage());
    }
}
