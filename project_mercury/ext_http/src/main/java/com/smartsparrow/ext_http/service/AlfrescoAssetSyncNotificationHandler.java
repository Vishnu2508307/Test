package com.smartsparrow.ext_http.service;

import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_PULL_RESULT_HANDLER;
import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_PULL_ERROR_HANDLER;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

public class AlfrescoAssetSyncNotificationHandler implements NotificationHandler {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetSyncNotificationHandler.class);

    private final CamelReactiveStreamsService camelReactiveStreamsService;

    @Inject
    public AlfrescoAssetSyncNotificationHandler(final CamelReactiveStreamsService camelReactiveStreamsService) {
        this.camelReactiveStreamsService = camelReactiveStreamsService;
    }

    @Override
    public Mono<Void> handleResultNotification(final ResultNotification resultNotification, final UUID resultId) {
        // send a camel event to handle the notification result
        return Mono.just(resultNotification) //
                .doOnEach(log.reactiveInfo("handling alfresco node pull result"))
                .map(event -> camelReactiveStreamsService.toStream(ALFRESCO_NODE_PULL_RESULT_HANDLER, event, ResultNotification.class)) //
                .flatMap(Mono::from)
                .doOnEach(log.reactiveInfo("alfresco node pull result handling completed"))
                .then();
    }

    @Override
    public Mono<Void> handleRetryNotification(final RetryNotification retryNotification) {
        // nothing to do. this handler doesn't kick off retries.
        log.warn("not retrying this notification: {}", retryNotification);
        return Mono.empty();
    }

    @Override
    public Mono<Void> handleErrorNotification(final ErrorNotification errorNotification) {
        log.error("received an error notification: {}", errorNotification);
        return Mono.just(errorNotification) //
                .doOnEach(log.reactiveInfo("handling alfresco node pull error"))
                .map(event -> camelReactiveStreamsService.toStream(ALFRESCO_NODE_PULL_ERROR_HANDLER, event, ErrorNotification.class)) //
                .flatMap(Mono::from)
                .doOnEach(log.reactiveInfo("alfresco node pull error handling completed"))
                .then();
    }
}
