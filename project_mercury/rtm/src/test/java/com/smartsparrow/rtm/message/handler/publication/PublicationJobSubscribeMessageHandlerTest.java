package com.smartsparrow.rtm.message.handler.publication;

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
import com.smartsparrow.rtm.message.handler.courseware.publication.PublicationJobSubscribeMessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublicationJobGenericMessage;
import com.smartsparrow.rtm.subscription.courseware.publication.PublicationJobRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.rtm.subscription.courseware.publication.PublicationJobRTMSubscription.PublicationJobRTMSubscriptionFactory;

import reactor.core.publisher.Mono;

class PublicationJobSubscribeMessageHandlerTest {

    private PublicationJobSubscribeMessageHandler handler;

    @Mock
    private Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;

    @Mock
    private PublicationJobRTMSubscription rtmSubscription;

    @Mock
    private RTMSubscriptionManager rtmSubscriptionManager;

    @Mock
    private PublicationJobGenericMessage message;

    @Mock
    private PublicationJobRTMSubscriptionFactory publicationJobRTMSubscriptionFactory;

    private static final UUID publicationId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(message.getPublicationId()).thenReturn(publicationId);
        when(rtmSubscriptionManagerProvider.get()).thenReturn(rtmSubscriptionManager);

        when(publicationJobRTMSubscriptionFactory.create(publicationId)).thenReturn(rtmSubscription);
        handler = new PublicationJobSubscribeMessageHandler(rtmSubscriptionManagerProvider, publicationJobRTMSubscriptionFactory);
    }

    @Test
    void validate_nullPublicationId() {
        when(message.getPublicationId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("publicationId is required", f.getMessage());
    }

    @Test
    void validate() {
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_error() throws WriteResponseException {
        when(rtmSubscriptionManager.add(any(PublicationJobRTMSubscription.class))).thenReturn(Mono.error(new RuntimeException("err!")));

        handler.handle(session, message);

        final String expected = "{\"type\":\"publication.job.subscribe.error\",\"code\":400,\"message\":\"err!\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmSubscriptionManager).add(any(PublicationJobRTMSubscription.class));

    }

    @Test
    void handle_success() throws WriteResponseException {
        when(rtmSubscriptionManager.add(any(PublicationJobRTMSubscription.class))).thenReturn(Mono.just(1));

        handler.handle(session, message);

        final String expected = "{\"type\":\"publication.job.subscribe.ok\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmSubscriptionManager).add(rtmSubscription);
    }

}