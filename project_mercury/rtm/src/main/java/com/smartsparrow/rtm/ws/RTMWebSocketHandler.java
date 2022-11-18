package com.smartsparrow.rtm.ws;

import static com.smartsparrow.util.log.JsonLayout.REQUEST_CONTEXT;
import static com.smartsparrow.util.log.JsonLayout.TRACE_ID;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.MDC;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provider;
import com.smartsparrow.cache.diffsync.DiffSyncSubscriptionManager;
import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.exception.ErrorResponseType;
import com.smartsparrow.exception.Fault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.MutableAuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.RTMWebSocketHandlerException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.MessageType;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.ReceivedMessage;
import com.smartsparrow.rtm.message.event.EventPublisher;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.subscription.SubscriptionManager;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.wiring.RTMScope;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.data.Request;
import com.smartsparrow.util.log.data.RequestContext;

import reactor.core.Exceptions;

/**
 * The RTM WebSocket handler. This class responsibility is to apply business logic rules to the incoming message
 * This class also allows to facilitate testing and enable thread safety on message processing.
 */
class RTMWebSocketHandler {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RTMWebSocketHandler.class);

    // the message handlers, as lazy-Providers.
    private final Map<String, Collection<Provider<MessageHandler<? extends ReceivedMessage>>>> messageHandlers;

    // the event publishers
    private final Map<String, Collection<Provider<EventPublisher<? extends BroadcastMessage>>>> publishers;

    // scope and state related fields
    private final RTMScope rtmScope;

    private final RTMWebSocketAuthorizer rtmWebSocketAuthorizer;

    private Provider<MutableAuthenticationContext> authenticationContextProvider;
    private SubscriptionManager subscriptionManager;
    private RTMSubscriptionManager rtmSubscriptionManager;
    private DiffSyncSubscriptionManager diffSyncSubscriptionManager;
    private RTMClient rtmClient;
    private RTMEventBroker rtmEventBroker;

    @Inject
    RTMWebSocketHandler(Provider<MutableAuthenticationContext> authenticationContextProvider,
                        RTMWebSocketAuthorizer rtmWebSocketAuthorizer,
                        Map<String, Collection<Provider<MessageHandler<? extends ReceivedMessage>>>> messageHandlers,
                        Map<String, Collection<Provider<EventPublisher<? extends BroadcastMessage>>>> publishers,
                        RTMScope rtmScope) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmWebSocketAuthorizer = rtmWebSocketAuthorizer;
        this.messageHandlers = messageHandlers;
        this.publishers = publishers;
        this.rtmScope = rtmScope;
    }

    /**
     * Submit an incoming message for processing after it passes validation
     *
     * @param receivedMessage the deserialized received message
     */
    void submit(ReceivedMessage receivedMessage) throws RTMWebSocketHandlerException {
        // Error on messages that have an unsupported supplied type.
        validate(receivedMessage);
        // execute logic on message
        process(receivedMessage);
    }

    /**
     * Sets up the context for the current socket
     *
     * @param clientId a string value representing the client id
     * @param session the webSocket session
     */
    void initialise(String clientId, Session session, RTMWebSocketContext rtmWebSocketContext) {

        //
        // reset the authentication information on this thread
        // this is performed here because this thread could be reused without cleanup() called (eg. no close message from client)
        //
        MutableAuthenticationContextProvider.cleanup();

        //
        // sets the rtmClient
        //
        rtmClient = new RTMClient(session, new RTMClientContext(clientId, session.getRemoteAddress().getAddress(), rtmWebSocketContext));

        //
        // create a subscription manager
        //
        subscriptionManager = new SubscriptionManager(rtmClient);

        rtmSubscriptionManager = new RTMSubscriptionManager(rtmClient);
        diffSyncSubscriptionManager = new DiffSyncSubscriptionManager();

        //
        // create an rtm broker to handle event broadcasting
        //
        rtmEventBroker = new RTMEventBroker(publishers, rtmClient);

        //
        // Add the clientId to the authentication context so that it can be access in other modules as well
        // FIXME this is a temporary solution to allow to trigger subscription events from graphql/rest mutations api
        //
        authenticationContextProvider.get().setClientId(clientId);
    }

    /**
     * Executes cleanup up in the RTMScope held by this connection before closing the socket
     *
     */
    void cleanup() { //cannot call it finalize() without all sorts of code smell alerts
        rtmScope.enter();
        try {
            seedScope();

            // Unsubscribe all topics
            getSubscriptionManager().unsubscribeAll();

            rtmSubscriptionManager.unsubscribeAll();

            // empty request context
            MDC.put(REQUEST_CONTEXT, new RequestContext().toString());

        } finally {
            rtmScope.exit();
        }

        //
        // reset the authentication information on this thread (as this thread can be reused later)
        //
        MutableAuthenticationContextProvider.cleanup();
    }


    /**
     * Process the message inside the RTMScope.
     *
     * @param receivedMessage the received message to process
     */
    private void process(ReceivedMessage receivedMessage) throws RTMWebSocketHandlerException {
        rtmScope.enter();
        try {

            // seed the relevant values
            seedScope();

            // add request context information to MDC
            Account account = authenticationContextProvider.get().getAccount();

            RequestContext requestContext = new RequestContext()
                    .setAccountId(account != null ? account.getId().toString() : null)
                    .setRequest(new Request()
                            .setMethod("message")
                            .setType(Request.Type.WS)
                            .setUri(receivedMessage.getType())
                            .setMessageId(receivedMessage.getId()))
                    .setClientId(rtmClient.getRtmClientContext().getClientId());

            MDC.put(REQUEST_CONTEXT, requestContext.toString());
            if(receivedMessage.getTraceId() == null) {
                String traceId = UUIDs.timeBased().toString();
                MDC.put(TRACE_ID, traceId);
            }else {
                MDC.put(TRACE_ID, receivedMessage.getTraceId());
            }

            handle(receivedMessage);

        } finally {
            MDC.remove(TRACE_ID);
            rtmScope.exit();


        }
    }

    /**
     * Seed the scope after calling rtmScope.enter()
     */
    private void seedScope() {
        rtmScope.seed(RTMClientContext.class, rtmClient.getRtmClientContext());
        rtmScope.seed(SubscriptionManager.class, subscriptionManager);
        rtmScope.seed(RTMSubscriptionManager.class, rtmSubscriptionManager);
        rtmScope.seed(RTMEventBroker.class, rtmEventBroker);
    }

    /**
     * Handles a message applying business rules. Any type of logic performed in this method is scoped in RTMScope
     *
     * @param receivedMessage the received message to handle
     */
    private void handle(ReceivedMessage receivedMessage) throws RTMWebSocketHandlerException {
        authorize(receivedMessage);

        // Invoke all the message handlers
        invokeHandlersFor(receivedMessage);
    }

    /**
     * Check if the received message can be handled, has authorizers and the message validation passes
     *
     * @param receivedMessage the message to validate
     */
    private void validate(ReceivedMessage receivedMessage) throws RTMWebSocketHandlerException {
        // validates that the message has handlers and authorizers
        defaultValidation(receivedMessage);

        // perform custom validation
        customValidation(receivedMessage);
    }

    /**
     * Perform the custom validation implemented in the message handler. This is intended for validation of required
     * fields over an incoming message. When {@link MessageHandler#validate(MessageType)} is not overridden the default
     * implementation is invoked.
     *
     * @param receivedMessage the received message to validate
     * @throws RTMWebSocketHandlerException when the message validation fails
     */
    @SuppressWarnings("unchecked")
    private void customValidation(ReceivedMessage receivedMessage) throws RTMWebSocketHandlerException {
        Collection<Provider<MessageHandler<? extends ReceivedMessage>>> handlerProviders = messageHandlers.get(receivedMessage.getType());
        for (Provider<MessageHandler<? extends ReceivedMessage>> provider : handlerProviders) {
            MessageHandler<ReceivedMessage> handler = (MessageHandler<ReceivedMessage>) provider.get();
            try {
                handler.validate(receivedMessage);
            } catch (Exception e) {
                Throwable unwrapped = Exceptions.unwrap(e);
                if(unwrapped instanceof RTMValidationException) {
                    //the expected exception was thrown by validate method - just rethrow it
                    throw e;
                } else if(unwrapped instanceof Fault) {
                    throw new RTMWebSocketHandlerException(unwrapped.getMessage(),
                            receivedMessage.getId(),
                            ((ErrorResponseType)unwrapped).getResponseStatusCode(),
                            receivedMessage.getType());
                } else {
                    log.error("Validation exception", e);
                    // this block is executed if an exception is not caught in a handler.validate()
                    String errorMessage = "unhandled error occurred to message validating";

                    throw new RTMWebSocketHandlerException(errorMessage, receivedMessage.getId(),
                            HttpStatus.SC_INTERNAL_SERVER_ERROR, receivedMessage.getType());
                }
            }
        }
    }

    /**
     * Perform the default validation on an incoming message. All incoming messages are required to have at least one
     * {@link MessageHandler} and at least one {@link com.smartsparrow.rtm.message.AuthorizationPredicate}.
     *
     * @param receivedMessage the incoming message to validate
     * @throws RTMWebSocketHandlerException when the received message has neither {@link MessageHandler} nor
     * {@link com.smartsparrow.rtm.message.AuthorizationPredicate}
     */
    private void defaultValidation(ReceivedMessage receivedMessage) throws RTMWebSocketHandlerException {
        String errorStr;
        if (!messageHandlers.containsKey(receivedMessage.getType())) {
            errorStr = String.format("Handlers not supplied for message type %s", receivedMessage.getType());
            log.jsonWarn("Handlers not supplied for message type", new HashedMap<String, Object>(){
                {
                    put("type", receivedMessage.getType());
                }
            });
            throw new RTMWebSocketHandlerException(errorStr, receivedMessage.getId());
        }

        if (!rtmWebSocketAuthorizer.hasAuthorizer(receivedMessage)) {
            errorStr = String.format("Authorizer not supplied for message type %s", receivedMessage.getType());
            log.jsonWarn("Authorizer not supplied for message type", new HashedMap<String, Object>(){
                {
                    put("type", receivedMessage.getType());
                }
            });
            throw new RTMWebSocketHandlerException(errorStr, receivedMessage.getId());
        }
    }

    /**
     * Authorize the message
     *
     * @param receivedMessage the message to authorize
     * @throws RTMWebSocketHandlerException if the user is not authorized
     */
    private void authorize(ReceivedMessage receivedMessage) throws RTMWebSocketHandlerException {
        rtmWebSocketAuthorizer.authorize(receivedMessage, authenticationContextProvider.get());
    }

    /**
     * Invoke all message handlers
     *
     * @param receivedMessage the message to handle
     */
    @SuppressWarnings("unchecked")
    private void invokeHandlersFor(ReceivedMessage receivedMessage) throws RTMWebSocketHandlerException {
        Collection<Provider<MessageHandler<? extends ReceivedMessage>>> handlerProviders = messageHandlers.get(receivedMessage.getType());
        for (Provider<MessageHandler<? extends ReceivedMessage>> provider : handlerProviders) {
            try {
                MessageHandler<ReceivedMessage> handler = (MessageHandler<ReceivedMessage>) provider.get();
                handler.handle(rtmClient.getSession(), receivedMessage);
            } catch (Exception e) {

                // If the exception was propagated by a reactor chain, try and unwrap it.
                Throwable unwrapped = Exceptions.unwrap(e);

                // Deal with RTM level errors here
                if(unwrapped instanceof Fault) {
                    // For faults, skip logging errors
                    throw new RTMWebSocketHandlerException(unwrapped.getLocalizedMessage(),
                            receivedMessage.getId(),
                            ((ErrorResponseType)unwrapped).getResponseStatusCode(),
                            receivedMessage.getType());
                } else if(unwrapped instanceof WriteResponseException) {
                    log.error("unable to write the response", unwrapped);
                } else {
                    String errorMessage = "unhandled error occurred to message processing";
                    log.error(errorMessage, unwrapped);
                    //re-throw RTMWebSocketHandlerException so RTMWebSocketBroker will catch it and send error message to client
                    // this block is executed if the service layer throws a runtime exception and the exception is not caught in a message handler
                    throw new RTMWebSocketHandlerException(errorMessage, receivedMessage.getId(), HttpStatus.SC_INTERNAL_SERVER_ERROR, receivedMessage.getType());
                }
            }
        }
    }

    @VisibleForTesting
    RTMClient getRtmClient() {
        return this.rtmClient;
    }

    @VisibleForTesting
    AuthenticationContext getAuthenticationContext() {
        return authenticationContextProvider.get();
    }

    @VisibleForTesting
    SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }
}
