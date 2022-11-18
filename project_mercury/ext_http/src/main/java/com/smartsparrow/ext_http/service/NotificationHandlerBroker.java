package com.smartsparrow.ext_http.service;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;

/**
 * Broker the requests from the incoming request service and pass along to the respective handlers.
 */
@Singleton
class NotificationHandlerBroker {

    private static final Logger log = LoggerFactory.getLogger(NotificationHandlerBroker.class);

    private final Map<RequestPurpose, Provider<NotificationHandler>> requestHandlerProviders;

    @Inject
    NotificationHandlerBroker(final Map<RequestPurpose, Provider<NotificationHandler>> requestHandlerProviders) {
        this.requestHandlerProviders = requestHandlerProviders;
    }

    /**
     * Broker the result notification to the proper handler, based on the purpose.
     *
     * @param resultNotification the incoming notification to process
     * @param resultId the id of the persisted results
     * @return a {@link Mono<Void>} to chain on
     */
    Mono<Void> brokerResultNotification(final ResultNotification resultNotification, final UUID resultId) {
        //
        return Mono.just(resultNotification)
                // instantiate the handler.
                .flatMap(this::getHandler)
                // call the handler.
                .flatMap(handler -> {
                    if (log.isDebugEnabled()) {
                        log.debug("brokering result to handler {} resultId: {} resultNotification: {}",
                                  handler.getClass().getName(),
                                  resultId,
                                  resultNotification);
                    }
                    return handler.handleResultNotification(resultNotification, resultId);
                });
    }

    /**
     * Broker the retry notification to the proper handler, based on the purpose.
     *
     * @param retryNotification the incoming notification to process
     * @return a {@Link Mono<Void>} to chain on
     */
    Mono<Void> brokerRetryNotification(final RetryNotification retryNotification) {
        //
        return Mono.just(retryNotification)
                // instantiate the handler.
                .flatMap(this::getHandler)
                // call the handler
                .flatMap(handler -> {
                    if (log.isDebugEnabled()) {
                        log.debug("brokering retry to handler {} retryNotification: {}",
                                  handler.getClass().getName(),
                                  retryNotification);
                    }
                    return handler.handleRetryNotification(retryNotification);
                });
    }

    /**
     * Broker the error notification to the proper handler, based on the purpose.
     *
     * @param errorNotification the incoming notification to process
     * @return a {@Link Mono<Void>} to chain on
     */
    Mono<Void> brokerErrorNotification(final ErrorNotification errorNotification) {
        return Mono.just(errorNotification)
                 // instantiate the handler.
                .flatMap(this::getHandler)
                // call the handler
                .flatMap(handler -> {
                    if (log.isDebugEnabled()) {
                        log.debug("brokering retry to handler {} errorNotification: {}",
                                  handler.getClass().getName(),
                                  errorNotification);
                    }
                    return handler.handleErrorNotification(errorNotification);
                });
    }

    /**
     * Get the handler for the notification
     *
     * @param notification the specified notification
     * @return
     */
    Mono<NotificationHandler> getHandler(final Notification notification) {
        return Mono.just(notification)
                .map(n -> n.getState().getPurpose())
                .flatMap(purpose -> {
                    Provider<NotificationHandler> provider = requestHandlerProviders.get(purpose);
                    // no handler, don't continue.
                    if (provider == null) {
                        log.error("No handler for http request purpose: {}", purpose);
                        return Mono.empty();
                    }
                    NotificationHandler instance = provider.get();
                    return Mono.just(instance);
                });
    }

}
