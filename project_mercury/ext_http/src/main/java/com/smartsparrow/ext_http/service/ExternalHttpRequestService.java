package com.smartsparrow.ext_http.service;

import static com.smartsparrow.ext_http.service.ExternalHttpRequestLogRecord.Event.ERROR;
import static com.smartsparrow.ext_http.service.ExternalHttpRequestLogRecord.Event.REQUEST_SUBMITTED;
import static com.smartsparrow.ext_http.service.ExternalHttpRequestLogRecord.Event.RESULT_RECEIVED;
import static com.smartsparrow.ext_http.service.ExternalHttpRequestLogRecord.Event.RETRY_DELAY_SUBMITTED;
import static com.smartsparrow.ext_http.service.ExternalHttpRequestLogRecord.Event.RETRY_RECEIVED;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.time.Duration;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.Header;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.ext_http.data.ExternalHttpRequestGateway;
import com.smartsparrow.ext_http.route.ExternalHttpRoute;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 *
 */
@Singleton
public class ExternalHttpRequestService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ExternalHttpRequestService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final ExternalHttpRequestGateway externalHttpRequestGateway;
    private final NotificationHandlerBroker notificationHandlerBroker;
    private final ProducerTemplate producerTemplate;

    @Inject
    public ExternalHttpRequestService(final ExternalHttpRequestGateway externalHttpRequestGateway,
                                      final NotificationHandlerBroker notificationHandlerBroker,
                                      final ProducerTemplate producerTemplate) {
        this.externalHttpRequestGateway = externalHttpRequestGateway;
        this.notificationHandlerBroker = notificationHandlerBroker;
        this.producerTemplate = producerTemplate;
    }

    @Handler
    public void submit(@Header("requestPurpose") RequestPurpose requestPurpose,
                       @Header("referenceId") UUID referenceId,
                       @Body Request request) {
        submit(requestPurpose, request, referenceId).subscribe();
    }

    /**
     * Submit a request for processing
     *
     * @param purpose the request purpose / the response handler
     * @param request the request to persist and submit for processing
     * @param referenceId the external event id that triggered this submit(), e.g. a grade report id
     *
     * @return a Mono of the supplied RequestNotification argument
     */
    public Mono<RequestNotification> submit(final RequestPurpose purpose,
                                            final Request request,
                                            @Nullable final UUID referenceId) {
        //
        affirmNotNull(purpose, "purpose is required");
        affirmNotNull(request, "request is required");
        affirmArgument(request.getParams().has("uri"), "request missing uri");
        affirmArgumentNotNullOrEmpty(request.getParams().get("uri").textValue(), "request missing uri");

        return Mono.just(new RequestNotification() //
                                 .setState(new NotificationState() //
                                                   .setNotificationId(UUIDs.timeBased()) //
                                                   .setReferenceId(referenceId) //
                                                   .setPurpose(purpose)) //
                                 .setParams(request.getParams().deepCopy()))
                // use elastic scheduler
                .publishOn(Schedulers.elastic())
                // record the request
                .doOnNext(notification -> externalHttpRequestGateway.persist(notification).subscribe())
                // send the request notification to the SNS topic
                .doOnNext(notification -> {
                    Exchange response = producerTemplate
                            .request(ExternalHttpRoute.SUBMIT_REQUEST, exchange -> {
                                Message m = exchange.getIn();
                                m.setBody(mapper.writeValueAsString(notification));
                            });
                    log.info("submit():: response :: " + response);
                    log.info("submit():: response message:: " + response.getMessage());
                    if (response.isFailed()) {
                        Exception exception = (Exception) response.getProperty(Exchange.EXCEPTION_CAUGHT);
                        logEvent(notification, ERROR).subscribe();
                        log.error("submit():: Error sending event to SNS; message=" + exception.getMessage());
                        // TODO: consider propagating the event to the error handler.
                        throw new IllegalStateFault("Error sending event to SNS; message=" + exception.getMessage());
                    }
                })
                // log that the request was submitted
                .doOnNext(notification -> logEvent(notification, REQUEST_SUBMITTED).subscribe());
    }

    /**
     * Schedule a retry operation on this notification. This will call back the retry handler of the notification purpose in the time specified.
     *
     * @param n the original notification, e.g. the ResponseNotification
     * @param delay the amount to delay the message, e.g. Duration.of(5, ChronoUnit.MINUTES)
     * @return the created retry notification
     */
    public Mono<RetryNotification> scheduleRetry(final Notification n, final Duration delay) {
        affirmNotNull(n, "notification is required");
        affirmNotNull(n.getState(), "notification.state is required");
        affirmNotNull(n.getState().getPurpose(), "notification.state.purpose is required");
        affirmNotNull(n.getState().getNotificationId(), "notification.state.id is required");
        affirmNotNull(delay, "delay is required");
        affirmArgument(!delay.isNegative(), "delay must be positive");
        affirmArgument(!delay.isZero(), "delay must be positive");

        return Mono.just(new RetryNotification() //
                        .setState(new NotificationState() //
                                          .setNotificationId(UUIDs.timeBased()) // new id.
                                          .setReferenceId(n.getState().getReferenceId())
                                          .setPurpose(n.getState().getPurpose()))
                                 .setDelaySec(delay.getSeconds())
                                 .setSourceNotificationId(n.getState().getNotificationId()))
                // record the notification
                .doOnNext(notification -> externalHttpRequestGateway.persist(notification).subscribe())
                // send to the delay queue.
                .doOnNext(notification -> {
                    Exchange response = producerTemplate
                            .request(ExternalHttpRoute.RETRY_QUEUE, exchange -> {
                                Message m = exchange.getIn();
                                m.setBody(mapper.writeValueAsString(notification));
                                if (notification.getDelaySec() != null) {
                                    m.setHeader("CamelAwsSqsDelaySeconds", notification.getDelaySec());
                                }
                            });
                    if (response.isFailed()) {
                        Exception exception = (Exception) response.getProperty(Exchange.EXCEPTION_CAUGHT);
                        logEvent(notification, ERROR).subscribe();
                        // TODO: consider propagating the event to the error handler.
                        throw new IllegalStateFault("Error sending event to SQS; message=" + exception.getMessage());
                    }
                })
                // log the retry was submitted.
                .doOnNext(notification -> logEvent(notification, RETRY_DELAY_SUBMITTED).subscribe());
    }

    /**
     * Persist and process the a result, brokers to the proper handler based on the purpose.
     *
     * @param resultNotification the notification to process
     * @return a Mono of the supplied ResultNotification argument
     */
    public Mono<ResultNotification> processResultNotification(final ResultNotification resultNotification) {
        affirmNotNull(resultNotification, "resultNotification is required");

        // record the result using the supplied state within the notification
        final UUID resultId = UUIDs.timeBased();
        return Mono.just(resultNotification)
                // log the notification was received.
                .doOnNext(notification -> logEvent(notification, RESULT_RECEIVED).subscribe())
                // persist the records. (this has potential to be pushed deeper into the gateway)
                .doOnNext(notification -> Flux.fromIterable(notification.getResult())
                        // ensure the order stays in the same as the list.
                        .concatMap(httpEvent -> Flux.just(new HttpResult()
                                                                  .setId(resultId)
                                                                  .setNotificationId(notification.getState().getNotificationId())
                                                                  .setSequenceId(UUIDs.timeBased())
                                                                  .setEvent(httpEvent)))
                        .doOnNext(httpResult -> externalHttpRequestGateway.persist(httpResult).subscribe())
                        .subscribe())
                // broker the result to the proper handler
                .doOnNext(notification -> notificationHandlerBroker.brokerResultNotification(notification,
                                                                                             resultId).subscribe());
    }

    /**
     * Process a retry notification, brokers to the proper handler based on the purpose.
     *
     * @param retryNotification the notification
     * @return a Mono of the supplied RetryNotification argument
     */
    public Mono<RetryNotification> processRetryNotification(final RetryNotification retryNotification) {
        affirmNotNull(retryNotification, "retryNotification is required");

        return Mono.just(retryNotification)
                // log an event
                .doOnNext(notification -> logEvent(notification, RETRY_RECEIVED).subscribe())
                // broker the result to the proper handler
                .doOnNext(notification -> notificationHandlerBroker.brokerRetryNotification(notification).subscribe());
    }

    /**
     * Process an error notification. These are errors which are caught within the external processing.
     *
     * @param errorNotification the error notification
     * @return a Mono of the supplied ErrorNotification argument
     */
    public Mono<ErrorNotification> processErrorNotification(final ErrorNotification errorNotification) {
        affirmNotNull(errorNotification, "errorNotification is required");

        return Mono.just(errorNotification)
                // log notification as application error.
                .doOnNext(notification -> logEvent(notification, ERROR).subscribe())
                // broker the result to the proper handler
                .doOnNext(notification -> notificationHandlerBroker.brokerErrorNotification(notification).subscribe());
    }

    /**
     * Log an event step against this notification
     *
     * @param notification the source notification
     * @param event the event
     * @return a Mono of the generated log record
     */
    Mono<ExternalHttpRequestLogRecord> logEvent(final Notification notification,
                                                final ExternalHttpRequestLogRecord.Event event) {
        //
        return Mono.just(new ExternalHttpRequestLogRecord()
                                 .setId(UUIDs.timeBased())
                                 .setNotificationId(notification.getState().getNotificationId())
                                 .setEvent(event))
                .doOnNext(log -> externalHttpRequestGateway.persist(log).subscribe());

    }
}
