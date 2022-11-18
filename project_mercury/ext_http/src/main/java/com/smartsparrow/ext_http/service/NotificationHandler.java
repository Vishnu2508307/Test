package com.smartsparrow.ext_http.service;

import java.util.UUID;

import reactor.core.publisher.Mono;

/**
 * The interface to drive the purpose-specific result handlers.
 */
public interface NotificationHandler {

    /**
     * Process a result notification
     *
     * @param resultNotification the incoming result notification
     * @param resultId the id of the persisted result
     * @return a void Mono
     */
    Mono<Void> handleResultNotification(ResultNotification resultNotification, UUID resultId);

    /**
     * Process a retry notification
     *
     * @param retryNotification the incoming retry notification
     * @return a void Mono
     */
    Mono<Void> handleRetryNotification(RetryNotification retryNotification);

    /**
     * Process an error notification
     *
     * @param errorNotification the incoming error notificiation
     * @return a void Mono
     */
    Mono<Void> handleErrorNotification(ErrorNotification errorNotification);

}
