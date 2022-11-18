package com.smartsparrow.rest.resource.sns;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.exception.BadRequestException;
import com.smartsparrow.user_content.eventmessage.UserContentNotificationMessage;
import com.smartsparrow.user_content.service.UserContentService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;


@Path("/userContent")
public class UserContentResource {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(UserContentResource.class);
    private final Provider<SnsMessageManager> snsMessageManagerProvider;
    private final UserContentService userContentService;

    @Inject
    public UserContentResource(final Provider<SnsMessageManager> snsMessageManagerProvider,
                               final UserContentService userContentService) {
        this.snsMessageManagerProvider = snsMessageManagerProvider;
        this.userContentService = userContentService;
    }

    @POST
    @Path("/updateCache")
    public Response updateCache(final String body) throws JsonProcessingException {
        // verify and parse the message.
        SnsMessage snsMessage = parseMessage(body);

        // process the message
        if (snsMessage instanceof SnsNotification) {
            SnsNotification notification = (SnsNotification) snsMessage;
            // extract the payload
            String payload = notification.getMessage();
            // deserialize it.
            UserContentNotificationMessage userContentNotificationMessage = readValue(payload, UserContentNotificationMessage.class);
            //process deserialized message
            userContentService.writeUserContentToCache(userContentNotificationMessage)
                    .block();
            return Response.ok().build();
        } else if (snsMessage instanceof SnsSubscriptionConfirmation) {
            //
            handleSubscriptionConfirmation((SnsSubscriptionConfirmation) snsMessage);
            return Response.ok().build();
        } else if (snsMessage instanceof SnsUnsubscribeConfirmation) {
            //
            handleUnsubscriptionConfirmation((SnsUnsubscribeConfirmation) snsMessage);
            return Response.ok().build();
        }
        throw new BadRequestException("Invalid message");
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
     * Process an unsubscribe message event. Logs it.
     *
     * @param message the Unsubscribe message event
     */
    void handleUnsubscriptionConfirmation(SnsUnsubscribeConfirmation message) {
        // do nothing besides log the event.
        log.warn("Unsubscribe event received; message:{} url:{}", message.getMessage(), message.getSubscribeUrl());
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
}
