package com.smartsparrow.rtm.message.handler.publication;

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
import com.smartsparrow.rtm.message.handler.courseware.publication.PublicationJobUnsubscribeMessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublicationJobGenericMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.courseware.publication.PublicationJobRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.publication.PublicationJobRTMSubscription.PublicationJobRTMSubscriptionFactory;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.util.UUIDs;

class PublicationJobUnsubscribeMessageHandlerTest {

    private PublicationJobUnsubscribeMessageHandler handler;

    @Mock
    private RTMSubscriptionManager rtmSubscriptionManager;

    @Mock
    private Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;

    @Mock
    private PublicationJobRTMSubscription publicationJobRTMSubscription;

    @Mock
    private PublicationJobGenericMessage message;

    @Mock
    private PublicationJobRTMSubscriptionFactory publicationJobRTMSubscriptionFactory;

    private static final UUID publicationId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmSubscriptionManagerProvider.get()).thenReturn(rtmSubscriptionManager);
        when(message.getPublicationId()).thenReturn(publicationId);
        when(publicationJobRTMSubscription.getName()).thenReturn(PublicationJobRTMSubscription.NAME(publicationId));

        when(publicationJobRTMSubscriptionFactory.create(publicationId)).thenReturn(publicationJobRTMSubscription);
        handler = new PublicationJobUnsubscribeMessageHandler(rtmSubscriptionManagerProvider, publicationJobRTMSubscriptionFactory);
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
    void handle_success() throws WriteResponseException, SubscriptionNotFound {
        handler.handle(session, message);

        final String expected = "{\"type\":\"publication.job.unsubscribe.ok\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmSubscriptionManager).unsubscribe(PublicationJobRTMSubscription.NAME(publicationId));
    }

    @Test
    void handle_error() throws SubscriptionNotFound, WriteResponseException {
        doThrow(new RuntimeException("error")).when(rtmSubscriptionManager).unsubscribe(anyString());

        handler.handle(session, message);

        final String expected = "{\"type\":\"publication.job.unsubscribe.error\",\"code\":422,\"message\":\"error unsubscribing from publication job subscription\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmSubscriptionManager).unsubscribe(PublicationJobRTMSubscription.NAME(publicationId));
    }

}