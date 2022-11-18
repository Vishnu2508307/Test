package com.smartsparrow.rtm.ws;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockReceivedMessage;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMWebSocketHandlerException;
import com.smartsparrow.rtm.lang.UnsupportedMessageType;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.ReceivedMessageDeserializer;

class RTMWebSocketBrokerTest {

    @Mock
    private RTMWebSocketHandler rtmWebSocketHandler;

    @Mock
    private RTMWebSocketExecutor rtmWebSocketExecutor;

    @Mock
    private ReceivedMessageDeserializer receivedMessageDeserializer;

    private static final RTMWebSocketContext rtmWebSocketContext = RTMWebSocketContext.SOCKET;
    private RTMWebSocketBroker rtmWebSocketBroker;

    @BeforeEach
    void init() throws IOException {
        MockitoAnnotations.initMocks(this);
        rtmWebSocketBroker = new RTMWebSocketBroker(
                rtmWebSocketHandler,
                rtmWebSocketExecutor,
                receivedMessageDeserializer,
                rtmWebSocketContext);

        when(receivedMessageDeserializer.deserialize(anyString())).thenReturn(mockReceivedMessage());
    }

    @Test
    void process_failsDeserializingMessage_IOException() throws IOException {
        WebSocketAdapter spy = Mockito.spy(rtmWebSocketBroker);
        Session session = RTMWebSocketTestUtils.mockSession();
        when(spy.getSession()).thenReturn(session);
        String message = "random";
        IOException e = new IOException("boom");
        doThrow(e).when(receivedMessageDeserializer).deserialize(message);

        spy.onWebSocketText(message);

        String errorMessage = "{\"type\":\"error\",\"code\":500,\"message\":\"Invalid message, could not be parsed: boom\"}";
        verify(session.getRemote(), times(1)).sendStringByFuture(errorMessage);
    }

    @Test
    void process_failsDeserialisingMessage_notSupported() throws Exception {
        WebSocketAdapter spy = Mockito.spy(rtmWebSocketBroker);
        Session session = RTMWebSocketTestUtils.mockSession();
        when(spy.getSession()).thenReturn(session);
        String message = "random";
        UnsupportedMessageType e = new UnsupportedMessageType("mate", null);
        doThrow(e).when(receivedMessageDeserializer).deserialize(message);

        spy.onWebSocketText(message);

        String errorMessage = "{\"type\":\"error\",\"code\":500,\"message\":\"Unsupported message type mate\"}";
        verify(session.getRemote(), times(1)).sendStringByFuture(errorMessage);
    }

    @Test
    void onWebSocketConnect() {
        Session session = RTMWebSocketTestUtils.mockSession();
        rtmWebSocketBroker.onWebSocketConnect(session);
        verify(rtmWebSocketHandler, atLeastOnce()).initialise(anyString(), any(Session.class), any(RTMWebSocketContext.class));
        assertTrue(RTMWebSocketManager.getInstance().hasConnections());
    }

    @Test
    void onWebSocketBinary() {
        WebSocketAdapter spy = Mockito.spy(rtmWebSocketBroker);
        Session session = RTMWebSocketTestUtils.mockSession();
        when(spy.getSession()).thenReturn(session);
        spy.onWebSocketBinary(null, 0, 0);

        String errorMessage = "{\"type\":\"error\",\"code\":400,\"message\":\"Unsupported message type\"}";
        verify(session.getRemote(), times(1)).sendStringByFuture(errorMessage);
    }

    @Test
    void onWebSocketText() {
        Session session = RTMWebSocketTestUtils.mockSession();
        WebSocketAdapter spy = Mockito.spy(rtmWebSocketBroker);
        when(spy.getSession()).thenReturn(session);

        String message = "a message";
        spy.onWebSocketText(message);
        verify(rtmWebSocketExecutor, times(1)).execute(any(Runnable.class));
    }

    @Test
    void onWebSocketText_error() throws RTMWebSocketHandlerException, InterruptedException {
        MutableAuthenticationContext mutableAuthenticationContext = mock(MutableAuthenticationContext.class);
        Session session = RTMWebSocketTestUtils.mockSession();
        WebSocketAdapter spy = Mockito.spy(rtmWebSocketBroker);
        RTMThreadPoolExecutor executor = new RTMThreadPoolExecutor(0, 5, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
                mutableAuthenticationContext);
        String message = "message";
        doCallRealMethod().when(rtmWebSocketExecutor).setExecutor(executor);
        rtmWebSocketExecutor.setExecutor(executor);
        when(rtmWebSocketExecutor.getExecutor()).thenReturn(executor);
        doCallRealMethod().when(rtmWebSocketExecutor).execute(any(Runnable.class));
        when(spy.getSession()).thenReturn(session);
        RTMWebSocketHandlerException e = mock(RTMWebSocketHandlerException.class);
        when(e.getType()).thenReturn("some.type.of.error");
        doThrow(e).when(rtmWebSocketHandler).submit(any(ReceivedMessage.class));

        spy.onWebSocketText(message);

        // force executor termination to test that error emitted on socket when exception is thrown
        rtmWebSocketExecutor.getExecutor().awaitTermination(500, TimeUnit.MILLISECONDS);
        String response = "{\"type\":\"some.type.of.error\"}";
        verify(session.getRemote(), times(1)).sendStringByFuture(response);
    }

    @Test
    void onWebSocketClose() {
        //initialize session before testing socket closing
        rtmWebSocketBroker.onWebSocketConnect(RTMWebSocketTestUtils.mockSession());

        rtmWebSocketBroker.onWebSocketClose(0, "");
        verify(rtmWebSocketHandler, atLeastOnce()).cleanup();
        verify(rtmWebSocketExecutor, times(1)).shutdownWebsocketExecutor(2, TimeUnit.SECONDS);
        assertFalse(RTMWebSocketManager.getInstance().hasConnections());
    }

    @Test
    void gracefulShutdown() {
        Session session = RTMWebSocketTestUtils.mockSession();
        rtmWebSocketBroker.onWebSocketConnect(session);
        rtmWebSocketBroker.gracefulShutdown(30, TimeUnit.SECONDS);
        verify(session, times(1)).close(eq(RTMWebSocketStatus.GOING_AWAY.getValue()), anyString());
    }

    @Test
    void onWebsocketError() {
        WebSocketAdapter spy = Mockito.spy(rtmWebSocketBroker);
        Session session = RTMWebSocketTestUtils.mockSession();

        when(spy.getSession()).thenReturn(session);

        Throwable throwable = new Throwable("Error while processing request body, socket connection could be discarded.");
        spy.onWebSocketError(throwable);

        String expectedErrorMessage = "{\"type\":\"error\",\"code\":400,\"message\":\"Error while processing request body, socket connection could be discarded.\"}";
        verify(session.getRemote(), times(1)).sendStringByFuture(expectedErrorMessage);
    }
}
