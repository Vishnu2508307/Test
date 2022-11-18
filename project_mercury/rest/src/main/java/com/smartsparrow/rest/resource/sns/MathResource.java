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

import com.amazonaws.services.sns.message.HttpException;
import com.amazonaws.services.sns.message.SnsMessage;
import com.amazonaws.services.sns.message.SnsMessageManager;
import com.amazonaws.services.sns.message.SnsNotification;
import com.amazonaws.services.sns.message.SnsSubscriptionConfirmation;
import com.amazonaws.services.sns.message.SnsUnsubscribeConfirmation;
import com.amazonaws.services.sns.model.ConfirmSubscriptionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.exception.BadRequestException;
import com.smartsparrow.math.data.MathAssetErrorNotification;
import com.smartsparrow.math.data.MathAssetResultNotification;
import com.smartsparrow.math.data.MathAssetRetryNotification;
import com.smartsparrow.math.service.MathAssetService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

@Path("/math-asset-resolver")
public class MathResource {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MathResource.class);

    // not sure if this is thread-safe, so wrapping in a Provider.
    private final Provider<SnsMessageManager> snsMessageManagerProvider;
    private final MathAssetService mathAssetService;

    @Inject
    public MathResource(Provider<SnsMessageManager> snsMessageManagerProvider,
                        MathAssetService mathAssetService) {
        this.snsMessageManagerProvider = snsMessageManagerProvider;
        this.mathAssetService = mathAssetService;
    }


    @POST
    @Path("/result")
    public Response resultHandler(final String body) {
        // verify and parse the message.
        SnsMessage snsMessage = parseMessage(body);

        // process the message
        if (snsMessage instanceof SnsNotification) {
            SnsNotification notification = (SnsNotification) snsMessage;
            // extract the payload
            String payload = notification.getMessage();

            // deserialize it.
            MathAssetResultNotification resultNotification = readValue(payload, MathAssetResultNotification.class);

            // process the notification.
            mathAssetService.processResultNotification(resultNotification)
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
    @Path("/error")
    public Response errorHandler(final String body) {
        // verify and parse the message.
        SnsMessage snsMessage = parseMessage(body);

        // process the message
        if (snsMessage instanceof SnsNotification) {
            SnsNotification notification = (SnsNotification) snsMessage;
            // extract the payload
            String payload = notification.getMessage();

            // deserialize it.
            MathAssetErrorNotification errorNotification = readValue(payload, MathAssetErrorNotification.class);

            // process the error notification
            mathAssetService.processErrorNotification(errorNotification)
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
            MathAssetRetryNotification retryNotification = readValue(payload, MathAssetRetryNotification.class);
            // process it.
            mathAssetService.processRetryNotification(retryNotification)
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
            log.error("received a math asset dead-letter to /submit/dead-letters; body={}", body);
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
            log.error("received a math asset dead-letter to /retry/dead-letters; body={}", body);
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
            log.error("Unable to deserialize math asset payload", e);
            throw new BadRequestException("Unable to parse message content");
        }
    }
}
