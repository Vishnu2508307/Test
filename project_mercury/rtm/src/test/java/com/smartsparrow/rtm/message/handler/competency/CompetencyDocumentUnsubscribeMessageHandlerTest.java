package com.smartsparrow.rtm.message.handler.competency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
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
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.competency.CompetencyDocumentMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.competency.CompetencyDocumentEventRTMSubscription;
import com.smartsparrow.rtm.subscription.competency.CompetencyDocumentEventRTMSubscription.CompetencyDocumentEventRTMSubscriptionFactory;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;

class CompetencyDocumentUnsubscribeMessageHandlerTest {

    @InjectMocks
    private CompetencyDocumentUnsubscribeMessageHandler handler;

    @Mock
    private Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;

    @Mock
    private CompetencyDocumentEventRTMSubscription competencyDocumentEventRTMSubscription;

    @Mock
    private CompetencyDocumentEventRTMSubscriptionFactory competencyDocumentEventRTMSubscriptionFactory;

    @Mock
    private CompetencyDocumentMessage message;

    @Mock
    private RTMSubscriptionManager rtmSubscriptionManager;

    private static final UUID documentId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getDocumentId()).thenReturn(documentId);

        when(rtmSubscriptionManagerProvider.get()).thenReturn(rtmSubscriptionManager);
        competencyDocumentEventRTMSubscription = new CompetencyDocumentEventRTMSubscription(documentId);
        when(competencyDocumentEventRTMSubscriptionFactory.create(documentId)).thenReturn(competencyDocumentEventRTMSubscription);
        handler = new CompetencyDocumentUnsubscribeMessageHandler(rtmSubscriptionManagerProvider,competencyDocumentEventRTMSubscriptionFactory);
    }

    @Test
    void validate_nullDocumentId() {
        when(message.getDocumentId()).thenReturn(null);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, ()-> handler.validate(message));

        assertEquals("documentId is required", e.getMessage());
    }

    @Test
    void handle_subscriptionNotFound() throws SubscriptionNotFound, WriteResponseException {
        doThrow(new SubscriptionNotFound("face_palm")).when(rtmSubscriptionManager).unsubscribe(anyString());

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"workspace.competency.document.unsubscribe.error\"," +
                            "\"code\":404," +
                            "\"message\":\"Subscription with name 'face_palm' is not found\"}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

    }

    @Test
    void handler_success() throws WriteResponseException {
        handler.handle(session, message);

        String expected = "{\"type\":\"workspace.competency.document.unsubscribe.ok\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
