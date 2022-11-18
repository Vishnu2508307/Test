package com.smartsparrow.rest.resource.sns;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sns.message.HttpException;
import com.amazonaws.services.sns.message.SnsMessage;
import com.amazonaws.services.sns.message.SnsMessageManager;
import com.amazonaws.services.sns.message.SnsNotification;
import com.amazonaws.services.sns.message.SnsSubscriptionConfirmation;
import com.amazonaws.services.sns.message.SnsUnsubscribeConfirmation;
import com.amazonaws.services.sns.model.ConfirmSubscriptionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.exception.BadRequestException;
import com.smartsparrow.export.data.ExportErrorNotification;
import com.smartsparrow.export.data.ExportRequestNotification;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.export.data.ExportRetryNotification;
import com.smartsparrow.export.service.ExportResultBroker;
import com.smartsparrow.export.service.ExportService;

import reactor.core.scheduler.Schedulers;

@Path("/courseware-element-to-ambrosia")
public class ExportResource {

    private static final Logger log = LoggerFactory.getLogger(ExportResource.class);

    // not sure if this is thread-safe, so wrapping in a Provider.
    private final Provider<SnsMessageManager> snsMessageManagerProvider;
    private final ExportService exportService;
    private final ExportResultBroker exportResultBroker;

    @Inject
    public ExportResource(Provider<SnsMessageManager> snsMessageManagerProvider,
                          final ExportService exportService,
                          final ExportResultBroker exportResultBroker) {
        this.snsMessageManagerProvider = snsMessageManagerProvider;
        this.exportService = exportService;
        this.exportResultBroker = exportResultBroker;
    }

    @POST
    @Path("/retry")
    public Response retryHandler(final String body) {
        // verify and parse the message.
        SnsMessage snsMessage = parseMessage(body);

        // process the message
        if (snsMessage instanceof SnsNotification) {
            SnsNotification notification = (SnsNotification) snsMessage;
            // extract the payload
            String payload = notification.getMessage();
            // deserialize it.
            ExportRetryNotification retryNotification = readValue(payload, ExportRetryNotification.class);
            // process it.
            exportService.processRetryNotification(retryNotification)
                    .block();
            // return ok.
            return ok();
        } else if (snsMessage instanceof SnsSubscriptionConfirmation) {
            //
            handleSubscriptionConfirmation((SnsSubscriptionConfirmation) snsMessage);
            return ok();
        } else if (snsMessage instanceof SnsUnsubscribeConfirmation) {
            //
            handleUnsubscriptionConfirmation((SnsUnsubscribeConfirmation) snsMessage);
            return ok();
        }

        // error case.
        throw new BadRequestException("Invalid message");
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("/submit/dead-letters")
    public Response submitDeadLettersHandler(final String body) {
        SnsMessage snsMessage = parseMessage(body);
        // process the message
        if (snsMessage instanceof SnsNotification) {
            //
            log.error("received a courseware export dead-letter to /submit/dead-letters; body={}", body);
            SnsNotification notification = (SnsNotification) snsMessage;
            // extract the payload
            String payload = notification.getMessage();
            // deserialize it.
            ExportRequestNotification requestNotification = readValue(payload, ExportRequestNotification.class);
            // process it.
            exportService.processSubmitDeadLetters(requestNotification, payload)
                    // broadcast the notification
                    .publishOn(Schedulers.elastic())
                    .then(exportResultBroker.broadcast(requestNotification.getExportId()))
                    .block();
            // return ok.
            return ok();
        } else if (snsMessage instanceof SnsSubscriptionConfirmation) {
            //
            handleSubscriptionConfirmation((SnsSubscriptionConfirmation) snsMessage);
            return ok();
        } else if (snsMessage instanceof SnsUnsubscribeConfirmation) {
            //
            handleUnsubscriptionConfirmation((SnsUnsubscribeConfirmation) snsMessage);
            return ok();
        }

        // error case.
        throw new BadRequestException("Invalid message");
    }

    @SuppressWarnings("unchecked")
    @POST
    @Path("/retry/dead-letters")
    public Response retryDeadLettersHandler(final String body) {
        SnsMessage snsMessage = parseMessage(body);
        //
        if (snsMessage instanceof SnsNotification) {
            //
            log.error("received a courseware export dead-letter to /retry/dead-letters; body={}", body);
            SnsNotification notification = (SnsNotification) snsMessage;
            // extract the payload
            String payload = notification.getMessage();
            // deserialize it.
            ExportRetryNotification retryNotification = readValue(payload, ExportRetryNotification.class);
            // process it.
            exportService.processRetryDeadLetters(retryNotification, payload)
                    .publishOn(Schedulers.elastic())
                    .then(exportService.findExportId(retryNotification.getNotificationId())
                                  .flatMap(exportId -> exportResultBroker.broadcast(exportId)))
                    .block();
            // return ok.
            return ok();
        } else if (snsMessage instanceof SnsSubscriptionConfirmation) {
            //
            handleSubscriptionConfirmation((SnsSubscriptionConfirmation) snsMessage);
            return ok();
        } else if (snsMessage instanceof SnsUnsubscribeConfirmation) {
            //
            handleUnsubscriptionConfirmation((SnsUnsubscribeConfirmation) snsMessage);
            return ok();
        }

        // error case.
        throw new BadRequestException("Invalid message");
    }

    /**
     * Convert the HTTP Request InputStream/String to a SNS message
     *
     * @param body the supplied body
     * @return a parsed SNS Message
     * @throws BadRequestException when the message fails to validate
     */
    SnsMessage parseMessage(final String body) {
        SnsMessageManager snsMessageManager = snsMessageManagerProvider.get();
        try (InputStream bodyStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8))) {
            return snsMessageManager.parseMessage(bodyStream);
        } catch (IOException | RuntimeException re) {
            // The underlying "parseMessage" function looks to be able to throw IO exceptions, AWS SDK Execptions, etc.
            log.error("unable to parse message content:" + body, re);
            throw new BadRequestException(re.getMessage());
        }
    }

    /**
     * Processs a subscribe confirmation message from SNS. This uses the SDK to confirm the subscription to start
     * receiving notification events.
     *
     * @param message the Subscription confirmation message.
     */
    void handleSubscriptionConfirmation(final SnsSubscriptionConfirmation message) {
        log.warn("Subscribe event received; message:{} url:{}", message.getMessage(), message.getSubscribeUrl());
        try {
            ConfirmSubscriptionResult confirmSubscriptionResult = message.confirmSubscription();
            log.warn("Subscription confirmed: {}", confirmSubscriptionResult);
        } catch (HttpException error) {
            log.error("Unable to confirm subscription to: {}", message.getSubscribeUrl());
            throw new BadRequestException("Unable to confirm subscription");
        }
    }

    /**
     * Process an unsubscribe message event. Logs it.
     *
     * @param message the Unsubscribe message event
     */
    void handleUnsubscriptionConfirmation(SnsUnsubscribeConfirmation message) {
        // do nothing besides log the event.
        log.warn("Unsubscribe event received; message:{} url:{}", message.getMessage(), message.getSubscribeUrl());
    }

    /**
     * Just return an HTTP Response of ok; here for readability in the handlers.
     *
     * @return an OK response
     */
    Response ok() {
        return Response.ok().build();
    }

    /**
     * Helper to centrally deal with deserializing the SNS message bodies.
     *
     * @param content the message content
     * @param valueType the type to convert to
     * @param <T> the return class
     * @return a hydrated content object
     */
    <T> T readValue(String content, Class<T> valueType) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(content, valueType);
        } catch (IOException e) {
            log.error("Unable to deserialize courseware export payload", e);
            throw new BadRequestException("Unable to parse message content");
        }
    }

}
