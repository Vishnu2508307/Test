package com.smartsparrow.rtm.ws;

import javax.inject.Inject;
import javax.inject.Provider;

import com.smartsparrow.rtm.message.ReceivedMessageDeserializer;

public class RTMWebSocketBrokerFactoryImpl implements RTMWebSocketBrokerFactory {

    private final Provider<RTMWebSocketExecutor> rtmWebSocketExecutorProvider;
    private final Provider<RTMWebSocketHandler> rtmWebSocketHandlerProvider;
    private final Provider<ReceivedMessageDeserializer> receivedMessageDeserializerProvider;

    /**
     * Inject the required dependencies as providers
     */
    @Inject
    public RTMWebSocketBrokerFactoryImpl(final Provider<RTMWebSocketExecutor> rtmWebSocketExecutorProvider,
                                         final Provider<RTMWebSocketHandler> rtmWebSocketHandlerProvider,
                                         final Provider<ReceivedMessageDeserializer> receivedMessageDeserializerProvider) {
        this.rtmWebSocketExecutorProvider = rtmWebSocketExecutorProvider;
        this.rtmWebSocketHandlerProvider = rtmWebSocketHandlerProvider;
        this.receivedMessageDeserializerProvider = receivedMessageDeserializerProvider;
    }

    @Override
    public RTMWebSocketBroker create(final RTMWebSocketContext rtmWebSocketContext) {
        return new RTMWebSocketBroker(
                rtmWebSocketHandlerProvider.get(),
                rtmWebSocketExecutorProvider.get(),
                receivedMessageDeserializerProvider.get(),
                rtmWebSocketContext
        );
    }
}
