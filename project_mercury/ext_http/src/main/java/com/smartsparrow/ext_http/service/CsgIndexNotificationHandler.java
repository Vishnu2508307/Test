package com.smartsparrow.ext_http.service;

import java.util.UUID;

import org.slf4j.Logger;

import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

public class CsgIndexNotificationHandler implements NotificationHandler {

    private static final Logger log = MercuryLoggerFactory.getLogger(CsgIndexNotificationHandler.class);

        @Override
    public Mono<Void> handleResultNotification(final ResultNotification resultNotification, final UUID resultId) {
        // TODO: store received requestID reply in dedicated table.
        // Not urgent because it is already being stored in ext_http.result_notification
        // nothing to do.
        log.warn("received submit notification: {}", resultNotification);
        return Mono.empty();
    }

    @Override
    public Mono<Void> handleRetryNotification(final RetryNotification retryNotification) {
        // // TODO: add retries.
        log.warn("not retrying this notification: {}", retryNotification);
        return Mono.empty();
    }

    @Override
    public Mono<Void> handleErrorNotification(final ErrorNotification errorNotification) {
        // TODO: will hook in publishing notifications here when available
        log.error("received an error notification: {}", errorNotification);
        return Mono.empty();
    }
}
