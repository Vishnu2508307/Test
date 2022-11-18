package com.smartsparrow.ext_http.service;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.util.UUID;

import static com.smartsparrow.dataevent.RouteUri.LEARNER_GRADE_PASSBACK_ERROR_HANDLER;
import static com.smartsparrow.dataevent.RouteUri.LEARNER_GRADE_PASSBACK_RESULT_HANDLER;

public class GradePassbackNotificationHandler implements NotificationHandler {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GradePassbackNotificationHandler.class);

    private final CamelReactiveStreamsService camelReactiveStreamsService;

    @Inject
    public GradePassbackNotificationHandler(final CamelReactiveStreamsService camelReactiveStreamsService) {
        this.camelReactiveStreamsService = camelReactiveStreamsService;
    }

    @Override
    public Mono<Void> handleResultNotification(final ResultNotification resultNotification, final UUID resultId) {
        // send a camel event to handle the notification result
        return Mono.just(resultNotification) //
                .doOnEach(log.reactiveInfo("handling learner grade passback result notification"))
                .map(event -> camelReactiveStreamsService.toStream(LEARNER_GRADE_PASSBACK_RESULT_HANDLER, event, ResultNotification.class)) //
                .flatMap(Mono::from)
                .doOnEach(log.reactiveInfo("grade learner passback result handling completed"))
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
                .doOnEach(log.reactiveInfo("handling learner grade passback error notification"))
                .map(event -> camelReactiveStreamsService.toStream(LEARNER_GRADE_PASSBACK_ERROR_HANDLER, event, ErrorNotification.class)) //
                .flatMap(Mono::from)
                .doOnEach(log.reactiveInfo("grade learner passback error handling completed"))
                .then();
    }
}
