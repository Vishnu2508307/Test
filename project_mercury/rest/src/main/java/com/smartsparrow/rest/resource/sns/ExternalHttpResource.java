package com.smartsparrow.rest.resource.sns;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.GET;
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
import com.pearson.autobahn.common.exception.AutobahnIdentityProviderException;
import com.smartsparrow.exception.BadRequestException;
import com.smartsparrow.ext_http.service.ErrorNotification;
import com.smartsparrow.ext_http.service.ExternalHttpRequestService;
import com.smartsparrow.ext_http.service.ResultNotification;
import com.smartsparrow.ext_http.service.RetryNotification;
import com.smartsparrow.learner.service.LearnerSearchableDocumentService;


@Path("/ext_http")
public class ExternalHttpResource {

    private static final Logger log = LoggerFactory.getLogger(ExternalHttpResource.class);

    // not sure if this is thread-safe, so wrapping in a Provider.
    private final Provider<SnsMessageManager> snsMessageManagerProvider;
    private final ExternalHttpRequestService externalHttpRequestService;

    @Inject
    public ExternalHttpResource(Provider<SnsMessageManager> snsMessageManagerProvider,
                                ExternalHttpRequestService externalHttpRequestService) {
        this.snsMessageManagerProvider = snsMessageManagerProvider;
        this.externalHttpRequestService = externalHttpRequestService;
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
            ResultNotification resultNotification = readValue(payload, ResultNotification.class);
            // process it.
            externalHttpRequestService.processResultNotification(resultNotification).block();
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
            ErrorNotification errorNotification = readValue(payload, ErrorNotification.class);
            // process it.
            externalHttpRequestService.processErrorNotification(errorNotification).block();
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
            RetryNotification retryNotification = readValue(payload, RetryNotification.class);
            // process it.
            externalHttpRequestService.processRetryNotification(retryNotification).block();
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
    @Path("/submit/dead-letters")
    public Response submitDeadLettersHandler(final String body) {
        SnsMessage snsMessage = parseMessage(body);
        // process the message
        if (snsMessage instanceof SnsNotification) {
            //
            log.error("received a dead-letter to /submit/dead-letters; body={}", body);
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
    @Path("/retry/dead-letters")
    public Response retryDeadLettersHandler(final String body) {
        SnsMessage snsMessage = parseMessage(body);
        //
        if (snsMessage instanceof SnsNotification) {
            //
            log.error("received a dead-letter to /retry/dead-letters; body={}", body);
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
