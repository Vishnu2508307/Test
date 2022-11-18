package com.smartsparrow.rest.resource.sns;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

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
import com.smartsparrow.asset.data.AssetErrorNotification;
import com.smartsparrow.asset.data.AssetResultNotification;
import com.smartsparrow.asset.data.AssetRetryNotification;
import com.smartsparrow.asset.service.BronteImageAssetOptimizer;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.CoursewareAssetService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.BadRequestException;
import com.smartsparrow.pubsub.subscriptions.assetoptimized.AssetOptimizedRTMProducer;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

@Path("/courseware-asset-resize")
public class AssetResizeResource {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AssetResizeResource.class);

    // not sure if this is thread-safe, so wrapping in a Provider.
    private final Provider<SnsMessageManager> snsMessageManagerProvider;
    private final CoursewareAssetService coursewareAssetService;
    private final CoursewareService coursewareService;
    private final BronteImageAssetOptimizer bronteImageAssetOptimizer;
    private final AssetOptimizedRTMProducer assetOptimizedRTMProducer;

    @Inject
    public AssetResizeResource(Provider<SnsMessageManager> snsMessageManagerProvider,
                               CoursewareAssetService coursewareAssetService,
                               CoursewareService coursewareService,
                               BronteImageAssetOptimizer bronteImageAssetOptimizer,
                               AssetOptimizedRTMProducer assetOptimizedRTMProducer) {
        this.snsMessageManagerProvider = snsMessageManagerProvider;
        this.coursewareAssetService = coursewareAssetService;
        this.coursewareService = coursewareService;
        this.bronteImageAssetOptimizer = bronteImageAssetOptimizer;
        this.assetOptimizedRTMProducer = assetOptimizedRTMProducer;
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
            AssetResultNotification resultNotification = readValue(payload, AssetResultNotification.class);

            // process the notification.
            bronteImageAssetOptimizer.processResultNotification(resultNotification, payload)
                    .block();

            // broadcast the result has been received
            CoursewareElement element = coursewareAssetService.getCoursewareElementsByAsset(resultNotification.getAssetId())
                    // In theory we can have multiple elements for an asset, but at the moment there is only one, so we just take the first response.
                    .singleOrEmpty()
                    .flatMap(coursewareElement -> coursewareService.getRootElementId(coursewareElement.getElementId(), coursewareElement.getElementType()))
                    .flatMap(coursewareService::findCoursewareElement)
                    .block();
            if (element != null) {
                emitEvent(new CoursewareElementBroadcastMessage()
                                  .setElement(element)
                                  .setAction(CoursewareAction.ASSET_OPTIMIZED)
                                  .setAssetId(resultNotification.getAssetId())
                                  .setAssetUrl(resultNotification.getUrl()), element.getElementId());
            }

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
            AssetErrorNotification errorNotification = readValue(payload, AssetErrorNotification.class);

            // process the error notification
            bronteImageAssetOptimizer.processErrorNotification(errorNotification, payload)
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
            AssetRetryNotification retryNotification = readValue(payload, AssetRetryNotification.class);
            // process it.
            bronteImageAssetOptimizer.processRetryNotification(retryNotification, payload)
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
            log.error("received a asset resize dead-letter to /submit/dead-letters; body={}", body);
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
            log.error("received a courseware asset resize dead-letter to /retry/dead-letters; body={}", body);
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
            log.error("Unable to deserialize asset resize payload", e);
            throw new BadRequestException("Unable to parse message content");
        }
    }

    /**
     * Emit an activity event for all the parent activity
     *
     * @param broadcastMessage the message containing the event data
     * @param parentActivity the parent activity present in the tree
     */
    private void emitEvent(CoursewareElementBroadcastMessage broadcastMessage, UUID parentActivity) {
        if (log.isDebugEnabled()) {
            log.debug("AssetResizeResource ASSET_OPTIMIZED broadcast: {}", parentActivity);
        }
        assetOptimizedRTMProducer.buildAssetOptimizedRTMConsumable(parentActivity,
                                                                   broadcastMessage.getElement().getElementId(),
                                                                   broadcastMessage.getElement().getElementType(),
                                                                   broadcastMessage.getAssetId(),
                                                                   broadcastMessage.getAssetUrl()).produce();
    }
}
