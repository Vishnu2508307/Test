package com.smartsparrow.ext_http.data;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.Session;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.ext_http.service.ExternalHttpRequestLogRecord;
import com.smartsparrow.ext_http.service.HttpResult;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.ext_http.service.RetryNotification;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class ExternalHttpRequestGateway {

    //
    private final Session session;

    //
    private final RequestNotificationMutator requestNotificationMutator;
    private final RequestNotificationMaterializer requestNotificationMaterializer;
    private final RetryNotificationMutator retryNotificationMutator;
    private final ExternalHttpRequestLogRecordMutator requestLogRecordMutator;
    private final HttpResultMutator httpResultMutator;


    @Inject
    public ExternalHttpRequestGateway(final Session session,
                                      final RequestNotificationMutator requestNotificationMutator,
                                      final RequestNotificationMaterializer requestNotificationMaterializer,
                                      final RetryNotificationMutator retryNotificationMutator,
                                      final ExternalHttpRequestLogRecordMutator requestLogRecordMutator,
                                      final HttpResultMutator httpResultMutator) {
        this.session = session;
        this.requestNotificationMutator = requestNotificationMutator;
        this.requestNotificationMaterializer = requestNotificationMaterializer;
        this.retryNotificationMutator = retryNotificationMutator;
        this.requestLogRecordMutator = requestLogRecordMutator;
        this.httpResultMutator = httpResultMutator;
    }

    /**
     * Persist a request
     *
     * @param requestNotification the request notification to persist
     * @return Flux of {@link Void}
     */
    public Flux<Void> persist(final RequestNotification requestNotification) {
        return Mutators.execute(session, Flux.just(requestNotificationMutator.upsert(requestNotification)));
    }

    /**
     * Persist a retry notification
     *
     * @param retryNotification the retry notification to persist
     * @return Flux of {@link Void}
     */
    public Flux<Void> persist(final RetryNotification retryNotification) {
        return Mutators.execute(session, Flux.just(retryNotificationMutator.upsert(retryNotification)));
    }

    /**
     * Persist a Request Log record
     *
     * @param logRecord the log event to record
     * @return Flux of {@link Void}
     */
    public Flux<Void> persist(final ExternalHttpRequestLogRecord logRecord) {
        return Mutators.execute(session, Flux.just(requestLogRecordMutator.upsert(logRecord)));
    }

    /**
     * Persist an Http Result
     *
     * @param result the result to persist
     * @return Flux of {@link Void}
     */
    public Flux<Void> persist(final HttpResult result) {
        return Mutators.execute(session, Flux.just(httpResultMutator.upsert(result)));
    }

    /**
     * Find a request by id
     *
     * @param notificationId the notificationId
     * @return the request or empty if not found
     */
    public Mono<RequestNotification> findRequestById(final UUID notificationId) {
        return ResultSets.query(session, requestNotificationMaterializer.findById(notificationId))
                .flatMapIterable(row -> row)
                .map(requestNotificationMaterializer::fromRow)
                .singleOrEmpty();
    }

}
