package com.smartsparrow.rtm.ws;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.genericMock;
import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockReceivedMessage;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.RTMWebSocketHandlerException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.event.EventPublisher;
import com.smartsparrow.rtm.subscription.SubscriptionManager;
import com.smartsparrow.rtm.wiring.RTMScope;


class RTMWebSocketHandlerTest {

    @Mock
    private Provider<MutableAuthenticationContext> authenticationContextProvider;

    @Mock
    private RTMWebSocketAuthorizer rtmWebSocketAuthorizer;

    @Mock
    private Map<String, Collection<Provider<MessageHandler<? extends ReceivedMessage>>>> messageHandlers;

    @Mock
    private Map<String, Collection<Provider<EventPublisher<? extends BroadcastMessage>>>> publishers;

    @Mock
    private RTMScope rtmScope;

    private RTMWebSocketHandler rtmWebSocketHandler;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);

        when(authenticationContextProvider.get()).thenReturn(new MutableAuthenticationContext());

        rtmWebSocketHandler = new RTMWebSocketHandler(authenticationContextProvider,
                                                      rtmWebSocketAuthorizer, messageHandlers, publishers, rtmScope);
    }

    @Test
    void process_invalidMessage_noHandlers() {
        ReceivedMessage message = mockReceivedMessage();

        when(messageHandlers.containsKey(message.getType())).thenReturn(false);

        assertThrows(RTMWebSocketHandlerException.class, () -> rtmWebSocketHandler.submit(message));
    }

    @Test
    void process_invalidMessage_noAuthorizers() {
        ReceivedMessage message = mockReceivedMessage();

        when(messageHandlers.containsKey(message.getType())).thenReturn(true);
        when(rtmWebSocketAuthorizer.hasAuthorizer(message)).thenReturn(false);

        assertThrows(RTMWebSocketHandlerException.class, () -> rtmWebSocketHandler.submit(message));
    }

    @SuppressWarnings("unchecked")
    @Test
    void process_invalidMessage_customValidationFails() throws RTMValidationException {
        ReceivedMessage message = mockReceivedMessage();

        Provider<MessageHandler<? extends ReceivedMessage>> handlerProvider = genericMock(Provider.class);
        List<Provider<MessageHandler<? extends ReceivedMessage>>> handlerProviders = Lists.newArrayList(handlerProvider);
        MessageHandler messageHandler = mock(MessageHandler.class);
        doThrow(RTMValidationException.class).when(messageHandler).validate(message);

        when(messageHandlers.containsKey(message.getType())).thenReturn(true);
        when(rtmWebSocketAuthorizer.hasAuthorizer(message)).thenReturn(true);
        when(messageHandlers.get(message.getType())).thenReturn(handlerProviders);
        when(handlerProvider.get()).thenReturn(messageHandler);

        assertThrows(RTMWebSocketHandlerException.class, () -> rtmWebSocketHandler.submit(message));
    }

    @SuppressWarnings("unchecked")
    @Test
    void process_invalidMessage_unexpectedErrorInCustomValidation() throws RTMValidationException {
        ReceivedMessage message = mockReceivedMessage();

        Provider<MessageHandler<? extends ReceivedMessage>> handlerProvider = genericMock(Provider.class);
        List<Provider<MessageHandler<? extends ReceivedMessage>>> handlerProviders = Lists.newArrayList(handlerProvider);
        MessageHandler messageHandler = mock(MessageHandler.class);
        doThrow(RuntimeException.class).when(messageHandler).validate(message);

        when(messageHandlers.containsKey(message.getType())).thenReturn(true);
        when(rtmWebSocketAuthorizer.hasAuthorizer(message)).thenReturn(true);
        when(messageHandlers.get(message.getType())).thenReturn(handlerProviders);
        when(handlerProvider.get()).thenReturn(messageHandler);

        RTMWebSocketHandlerException t = assertThrows(RTMWebSocketHandlerException.class, () -> rtmWebSocketHandler.submit(message));
        assertEquals(500, t.getStatusCode());
        assertEquals("unhandled error occurred to message validating", t.getErrorMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void process_unauthorizedMessage() throws Exception {
        ReceivedMessage message = mockReceivedMessage();

        Provider<MessageHandler<? extends ReceivedMessage>> handlerProvider = genericMock(Provider.class);
        MessageHandler messageHandler = genericMock(MessageHandler.class);
        doThrow(RTMValidationException.class).when(messageHandler).validate(message);
        when(handlerProvider.get()).thenReturn(messageHandler);
        List<Provider<MessageHandler<? extends ReceivedMessage>>> handlerProviders = Lists.newArrayList(handlerProvider);

        rtmWebSocketHandler.initialise("client-id", RTMWebSocketTestUtils.mockSession(), RTMWebSocketContext.SOCKET);

        when(messageHandlers.containsKey(message.getType())).thenReturn(true);
        when(messageHandlers.get(message.getType())).thenReturn(handlerProviders);
        when(rtmWebSocketAuthorizer.hasAuthorizer(message)).thenReturn(true);

        assertThrows(RTMWebSocketHandlerException.class, () -> rtmWebSocketHandler.submit(message));
    }

    @SuppressWarnings("unchecked")
    @Test
    void process_success() throws Exception {
        ReceivedMessage message = mockReceivedMessage();
        MessageHandler handler = mock(MessageHandler.class);
        Provider<MessageHandler<? extends ReceivedMessage>> handlerProvider = genericMock(Provider.class);
        Session session = RTMWebSocketTestUtils.mockSession();

        rtmWebSocketHandler.initialise("client-id", session, RTMWebSocketContext.SOCKET);

        when(messageHandlers.containsKey(message.getType())).thenReturn(true);
        when(rtmWebSocketAuthorizer.hasAuthorizer(message)).thenReturn(true);

        when(messageHandlers.get(message.getType())).thenReturn(Lists.newArrayList(handlerProvider));
        when(handlerProvider.get()).thenReturn(handler);
        rtmWebSocketHandler.submit(message);

        // verifies handlers have been called
        verify(handler, times(1)).handle(session, message);
    }


    @Test
    void initialise() {
        Session session = RTMWebSocketTestUtils.mockSession();
        String clientId = "client-id";

        assertAll("contexts",
                () -> assertNull(rtmWebSocketHandler.getRtmClient()),
                () -> assertNull(rtmWebSocketHandler.getSubscriptionManager())
        );

        rtmWebSocketHandler.initialise(clientId, session, RTMWebSocketContext.SOCKET);

        assertAll("contexts",
                () -> assertNotNull(rtmWebSocketHandler.getRtmClient()),
                () -> assertNotNull(rtmWebSocketHandler.getRtmClient().getRtmClientContext()),
                () -> assertNotNull(rtmWebSocketHandler.getRtmClient().getSession()),
                () -> assertEquals(RTMWebSocketContext.SOCKET, rtmWebSocketHandler.getRtmClient().getRtmClientContext().getRtmWebSocketContext()),
                () -> assertNotNull(rtmWebSocketHandler.getAuthenticationContext()),
                () -> assertNotNull(rtmWebSocketHandler.getSubscriptionManager())
        );
    }

    @Test
    void cleanup() {
        Session session = RTMWebSocketTestUtils.mockSession();
        String clientId = "client-id";

        // Not able to mock the managers initialize() creates, so using spy
        RTMWebSocketHandler spyHandler = spy(rtmWebSocketHandler);
        SubscriptionManager subscriptionManager = mock(SubscriptionManager.class);
        spyHandler.initialise(clientId, session, RTMWebSocketContext.SOCKET);

        when(spyHandler.getSubscriptionManager()).thenReturn(subscriptionManager);
        spyHandler.cleanup();

        verify(subscriptionManager).unsubscribeAll();
    }
}
