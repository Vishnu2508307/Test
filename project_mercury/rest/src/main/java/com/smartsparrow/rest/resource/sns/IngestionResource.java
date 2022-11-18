package com.smartsparrow.rest.resource.sns;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.smartsparrow.exception.NotFoundException;
import com.smartsparrow.ingestion.data.UploadResult;

import org.json.JSONObject;

import com.amazonaws.services.sns.message.HttpException;
import com.amazonaws.services.sns.message.SnsMessage;
import com.amazonaws.services.sns.message.SnsMessageManager;
import com.amazonaws.services.sns.message.SnsNotification;
import com.amazonaws.services.sns.message.SnsSubscriptionConfirmation;
import com.amazonaws.services.sns.message.SnsUnsubscribeConfirmation;
import com.amazonaws.services.sns.model.ConfirmSubscriptionResult;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.exception.BadRequestException;
import com.smartsparrow.ingestion.data.IngestionEvent;
import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.ingestion.data.IngestionSummary;
import com.smartsparrow.ingestion.service.ActivityIngestionBroker;
import com.smartsparrow.ingestion.service.IngestionBroker;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.service.ProjectBroker;

import reactor.core.scheduler.Schedulers;


@Path("/ingestion")
public class IngestionResource {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(IngestionResource.class);

    // not sure if this is thread-safe, so wrapping in a Provider.
    private final Provider<SnsMessageManager> snsMessageManagerProvider;
    private final IngestionService ingestionService;
    private final IngestionBroker ingestionBroker;
    private final ProjectBroker projectBroker;
    private final ActivityIngestionBroker activityIngestionBroker;

    @Inject
    public IngestionResource(Provider<SnsMessageManager> snsMessageManagerProvider,
                             IngestionService ingestionService,
                             IngestionBroker ingestionBroker,
                             ProjectBroker projectBroker,
                             ActivityIngestionBroker activityIngestionBroker) {
        this.snsMessageManagerProvider = snsMessageManagerProvider;
        this.ingestionService = ingestionService;
        this.ingestionBroker = ingestionBroker;
        this.projectBroker = projectBroker;
        this.activityIngestionBroker = activityIngestionBroker;
    }

    @POST
    @Path("/upload/result")
    public Response uploadResultHandler(final String body) {
        // verify and parse the message.
        SnsMessage snsMessage = parseMessage(body);

        // process the message
        if (snsMessage instanceof SnsNotification) {
            SnsNotification notification = (SnsNotification) snsMessage;
            // extract the payload
            String payload = notification.getMessage();
            // This is where we convert to the object
            UUID ingestionId;
            try {
                UploadResult s3Event = readValue(payload, UploadResult.class);
                S3EventNotificationRecord record = s3Event.getRecords().get(0);
                String s3Key = record.getS3().getObject().getKey();
                String ingestionString = s3Key.split("/")[0];
                ingestionId = UUID.fromString(ingestionString);
            } catch (Throwable throwable) {
                log.jsonError("error parsing ingestionId from message", new HashMap<String, Object>() {
                    {put("message", payload);}
                }, throwable);
                throw new NotFoundException(throwable.getMessage());
            }
            // process it.
            ingestionService.processS3UploadEvent(ingestionId)
                    // broadcast the notification
                    .publishOn(Schedulers.elastic())
                    .then(ingestionBroker.broadcast(ingestionId))
                    .then(projectBroker.broadcast(ingestionId))
                    .then(activityIngestionBroker.broadcast(ingestionId))
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

    @POST
    @Path("/adapter/result")
    public Response resultHandler(final String body) {
        // verify and parse the message.
        SnsMessage snsMessage = parseMessage(body);

        // process the message
        if (snsMessage instanceof SnsNotification) {
            SnsNotification notification = (SnsNotification) snsMessage;
            // extract the payload
            String payload = notification.getMessage();
            // deserialize it.
            IngestionSummary ingestionSummary = readValue(payload, IngestionSummary.class);
            // extract bearerToken from MessageAttributes
            String bearerToken = null;
            JSONObject message = new JSONObject(body);
            if (!message.isNull("MessageAttributes")) {
                bearerToken = message.getJSONObject("MessageAttributes").getJSONObject("bearerToken").getString("Value");
            }
            // process it.
            ingestionService.processResultNotification(ingestionSummary, bearerToken)
                    // broadcast the notification
                    .publishOn(Schedulers.elastic())
                    .then(ingestionBroker.broadcast(ingestionSummary.getId()))
                    .then(projectBroker.broadcast(ingestionSummary.getId()))
                    .then(activityIngestionBroker.broadcast(ingestionSummary.getId()))
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

    @POST
    @Path("/adapter/error")
    public Response errorHandler(final String body) {
        return processErrorNotificationAndBroadcastResult(body);
    }

    @POST
    @Path("/ambrosia/result")
    public Response ambrosiaResultHandler(final String body) {
        // verify and parse the message.
        SnsMessage snsMessage = parseMessage(body);

        // process the message
        if (snsMessage instanceof SnsNotification) {
            SnsNotification notification = (SnsNotification) snsMessage;
            // extract the payload
            String payload = notification.getMessage();
            // deserialize it.
            IngestionSummary ingestionSummary = readValue(payload, IngestionSummary.class);
            // Set the status to completed, as this is the last step in the process.
            ingestionSummary.setStatus(IngestionStatus.COMPLETED);
            // process it.
            ingestionService.processAmbrosiaResultNotification(ingestionSummary)
                    // broadcast the notification
                    .publishOn(Schedulers.elastic())
                    //for activity ingestion broadcast
                    .then(ingestionBroker.broadcast(ingestionSummary.getId()))
                    .then(projectBroker.broadcast(ingestionSummary.getId()))
                    .then(activityIngestionBroker.broadcast(ingestionSummary.getId()))
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

    @POST
    @Path("/ambrosia/error")
    public Response ambrosiaErrorHandler(final String body) {
        return processErrorNotificationAndBroadcastResult(body);
    }

    @POST
    @Path("/event-log/result")
    public Response eventLogResultHandler(final String body) {
        // verify and parse the message.
        SnsMessage snsMessage = parseMessage(body);

        // process the message
        if (snsMessage instanceof SnsNotification) {
            SnsNotification notification = (SnsNotification) snsMessage;
            // extract the payload
            String payload = notification.getMessage();
            // deserialize it.
            IngestionEvent ingestionEvent = readValue(payload, IngestionEvent.class);
            // process it.
            ingestionService.processEventLogResultNotification(ingestionEvent)
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

    @POST
    @Path("/event-log/error")
    public Response eventLogErrorHandler(final String body) {
        return processErrorNotificationAndBroadcastResult(body);
    }

    @POST
    @Path("/upload/error")
    public Response uploadErrorHandler(final String body) {
        return processErrorNotificationAndBroadcastResult(body);
    }

    /**
     * Store IngestionErrorLog message
     * Update the IngestionSummary with status 'FAILED'
     * Broadcast result
     *
     * @param body the supplied body
     * @return a Response
     */
    Response processErrorNotificationAndBroadcastResult(final String body) {
        // verify and parse the message.
        SnsMessage snsMessage = parseMessage(body);

        // process the message
        if (snsMessage instanceof SnsNotification) {
            SnsNotification notification = (SnsNotification) snsMessage;
            // extract the payload
            String payload = notification.getMessage();
            // deserialize it.
            IngestionSummary ingestionSummary = readValue(payload, IngestionSummary.class);
            // process it.
            ingestionService.processErrorNotification(ingestionSummary)
                    // broadcast the notification
                    .publishOn(Schedulers.elastic())
                    .then(ingestionBroker.broadcast(ingestionSummary.getId()))
                    .then(projectBroker.broadcast(ingestionSummary.getId()))
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
        //
        try (InputStream bodyStream = new ByteArrayInputStream(body.getBytes("UTF-8"))) {
            SnsMessage snsMessage = snsMessageManager.parseMessage(bodyStream);
            return snsMessage;
        } catch (IOException | RuntimeException re) {
            // The underlying "parseMessage" function looks to be able to throw IO exceptions, AWS SDK Execptions, etc.
            log.error("unable to parse message content:" + body, re);
            throw new BadRequestException(re.getMessage());
        }
    }

    /**
     * Helper to centrally deal with deserializing the SNS message bodies.
     *
     * @param content   the message content
     * @param valueType the type to convert to
     * @param <T>       the return class
     * @return a hydrated content object
     */
    <T> T readValue(String content, Class<T> valueType) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(content, valueType);
        } catch (IOException e) {
            log.error("Unable to deserialize payload", e);
            throw new BadRequestException("Unable to parse message content");
        }
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
}
