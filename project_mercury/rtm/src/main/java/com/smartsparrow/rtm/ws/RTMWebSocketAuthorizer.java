package com.smartsparrow.rtm.ws;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.jetty.http.HttpStatus;

import com.google.inject.Provider;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMWebSocketHandlerException;
import com.smartsparrow.rtm.message.AuthorizationPredicate;
import com.smartsparrow.rtm.message.MessageType;
import com.smartsparrow.rtm.message.ReceivedMessage;

class RTMWebSocketAuthorizer {

    // the message authorizers, as lazy-Providers.
    private final Map<String, Collection<Provider<AuthorizationPredicate<? extends MessageType>>>> authorizerPredicates;

    @Inject
    RTMWebSocketAuthorizer(Map<String, Collection<Provider<AuthorizationPredicate<? extends MessageType>>>> authorizerPredicates) {
        this.authorizerPredicates = authorizerPredicates;
    }

    /**
     * Checks if the MessageType has authorizers
     *
     * @param receivedMessage the message to validate
     * @return a boolean value describing if the message has authorizers or not
     */
    boolean hasAuthorizer(ReceivedMessage receivedMessage) {
        return authorizerPredicates.containsKey(receivedMessage.getType());
    }

    /**
     * Invoke all the message authorize predicates
     *
     * @param receivedMessage the message to authorize
     * @throws RTMWebSocketHandlerException with an error message
     */
    @SuppressWarnings("unchecked")
    void authorize(ReceivedMessage receivedMessage, AuthenticationContext authenticationContext)
            throws RTMWebSocketHandlerException {
        Collection<Provider<AuthorizationPredicate<? extends MessageType>>> authorizerProviders = authorizerPredicates.get(
                receivedMessage.getType());

        List<AuthorizationPredicate<ReceivedMessage>> predicateInstances = authorizerProviders.stream()
                .map(one-> (AuthorizationPredicate<ReceivedMessage>) one.get())
                .collect(Collectors.toList());

        invokeAuthorizers(predicateInstances, receivedMessage, authenticationContext);
    }

    /**
     * Invoke all authorize predicates.
     *
     * @param authorizers a collection of all the message authorizers
     * @param receivedMessage the message to authorize
     * @throws RTMWebSocketHandlerException with status code 401 if any of the authorizer fails
     */
    private void invokeAuthorizers(List<AuthorizationPredicate<ReceivedMessage>> authorizers, ReceivedMessage receivedMessage,
                                   AuthenticationContext authenticationContext) throws RTMWebSocketHandlerException {
        for (AuthorizationPredicate<ReceivedMessage> authorizer : authorizers) {
            if (!authorizer.test(authenticationContext, receivedMessage)) {
                throw new RTMWebSocketHandlerException(String.format("Unauthorized: %s", authorizer.getErrorMessage()),
                        receivedMessage.getId(), HttpStatus.UNAUTHORIZED_401, receivedMessage.getType());
            }
        }
    }
}
