package com.smartsparrow.ext_http.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;

public class GeneralNotificationHandler implements NotificationHandler {

    private static final Logger log = LoggerFactory.getLogger(GeneralNotificationHandler.class);

    @Override
    public Mono<Void> handleResultNotification(final ResultNotification resultNotification, final UUID resultId) {
        // nothing to do.
        return Mono.empty();
    }

    @Override
    public Mono<Void> handleRetryNotification(final RetryNotification retryNotification) {
        // nothing to do. this handler doesn't kick off retries.
        log.warn("not retrying this notification: {}", retryNotification);
        return Mono.empty();
    }

    @Override
    public Mono<Void> handleErrorNotification(final ErrorNotification errorNotification) {
        // nothing to do. this handler doesn't handle errors
        log.error("received an error notification: {}", errorNotification);
        return Mono.empty();
    }
}
