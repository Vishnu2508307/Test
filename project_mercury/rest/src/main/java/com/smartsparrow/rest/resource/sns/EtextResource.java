package com.smartsparrow.rest.resource.sns;

import com.amazonaws.services.sns.message.SnsMessage;
import com.amazonaws.services.sns.message.SnsMessageManager;
import com.amazonaws.services.sns.message.SnsNotification;
import com.amazonaws.services.sns.message.SnsSubscriptionConfirmation;
import com.amazonaws.services.sns.message.SnsUnsubscribeConfirmation;
import com.amazonaws.services.sns.message.HttpException;
import com.amazonaws.services.sns.model.ConfirmSubscriptionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartsparrow.courseware.data.PublicationJobStatus;
import com.smartsparrow.courseware.eventmessage.PublicationJobBroadcastMessage;
import com.smartsparrow.courseware.eventmessage.PublicationJobEventMessage;
import com.smartsparrow.exception.BadRequestException;
import com.smartsparrow.publication.data.EtextNotification;
import com.smartsparrow.publication.job.data.JobSummary;
import com.smartsparrow.publication.job.data.Notification;
import com.smartsparrow.publication.job.enums.ArtifactType;
import com.smartsparrow.publication.job.enums.JobStatus;
import com.smartsparrow.publication.job.enums.JobType;
import com.smartsparrow.publication.job.enums.NotificationType;
import com.smartsparrow.publication.job.enums.NotificationStatus;
import com.smartsparrow.publication.service.PublicationBroker;
import com.smartsparrow.publication.service.PublicationService;
import com.smartsparrow.util.UUIDs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Path("/etext")
public class EtextResource {

    private static final Logger log = LoggerFactory.getLogger(EtextResource.class);

    private final Provider<SnsMessageManager> snsMessageManagerProvider;
    private final PublicationService publicationService;
    private final PublicationBroker publicationBroker;

    @Inject
    public EtextResource(final Provider<SnsMessageManager> snsMessageManagerProvider,
                         final PublicationService publicationService,
                         final PublicationBroker publicationBroker) {
        this.snsMessageManagerProvider = snsMessageManagerProvider;
        this.publicationService = publicationService;
        this.publicationBroker = publicationBroker;
    }

    @POST
    @Path("/notification")
    public Response resultHandler(final String body) {
        // verify and parse the message.
        SnsMessage snsMessage = parseMessage(body);

        // process the message
        if (snsMessage instanceof SnsNotification) {

            SnsNotification notification = (SnsNotification) snsMessage;
            // extract the payload
            String payload = notification.getMessage();

            // deserialize it.
            EtextNotification etextNotification = readValue(payload, EtextNotification.class);

            // save the job and notification information.
            publicationService.fetchPublicationByExport(UUID.fromString(etextNotification.getExportId()))
                    .flatMap(publicationByExport -> {
                        UUID publicationId = publicationByExport.getPublicationId();
                        UUID jobId = publicationByExport.getExportId();
                        String etextStatus = etextNotification.getStatus();

                        //Broadcast publication status
                        publicationBroker.broadcast(new PublicationJobEventMessage().setContent(
                                new PublicationJobBroadcastMessage(publicationId, PublicationJobStatus.valueOf(etextStatus),
                                                                   jobId, etextNotification.getMessage(),etextNotification.getBookId(),
                                                                   etextNotification.getEtextVersion())));

                        if (etextStatus.equalsIgnoreCase("started")) {
                            return publicationService.saveJobAndArtifact(new JobSummary()
                                            .setId(jobId).setJobType(JobType.EPUB_PUBLISH)
                                            .setStatus(JobStatus.valueOf(etextNotification.getStatus())).setStatusDesc(etextNotification.getMessage()),
                                                publicationId,
                                                ArtifactType.EPUB_PUBLISH)
                                    .thenMany(publicationService.saveEtextNotification(new Notification()
                                            .setId(UUIDs.timeBased())
                                            .setNotificationType(NotificationType.valueOf(etextNotification.getType()))
                                            .setNotificationStatus(NotificationStatus.valueOf(etextNotification.getStatus()))
                                            .setMessage(etextNotification.getMessage()), jobId)).singleOrEmpty();
                        } else if (etextStatus.equalsIgnoreCase("completed")){
                            return publicationService.saveJobAndPublication(new JobSummary()
                                            .setId(jobId).setJobType(JobType.EPUB_PUBLISH)
                                            .setStatus(JobStatus.valueOf(etextNotification.getStatus())).setStatusDesc(etextNotification.getMessage()), publicationId)
                                    .thenMany(publicationService.saveEtextNotification(new Notification()
                                            .setId(UUIDs.timeBased())
                                            .setNotificationType(NotificationType.valueOf(etextNotification.getType()))
                                            .setNotificationStatus(NotificationStatus.valueOf(etextNotification.getStatus()))
                                            .setMessage(etextNotification.getMessage()), jobId))
                                    .thenMany(publicationService.updatePublicationMetadata(publicationByExport.getExportId(),
                                                                                           UUIDs.fromString(etextNotification.getActivityId()),
                                                                                           etextNotification.getEtextVersion(), etextNotification.getBookId())).singleOrEmpty();
                        } else {
                            return publicationService.saveJobAndPublication(new JobSummary()
                                            .setId(jobId).setJobType(JobType.EPUB_PUBLISH)
                                            .setStatus(JobStatus.valueOf(etextNotification.getStatus())).setStatusDesc(etextNotification.getMessage()), publicationId)
                                    .thenMany(publicationService.saveEtextNotification(new Notification()
                                            .setId(UUIDs.timeBased())
                                            .setNotificationType(NotificationType.valueOf(etextNotification.getType()))
                                            .setNotificationStatus(NotificationStatus.valueOf(etextNotification.getStatus()))
                                            .setMessage(etextNotification.getMessage()), jobId)).singleOrEmpty();
                        }
                    }).block();

            // return ok.
            return ok();
        } else if (snsMessage instanceof SnsSubscriptionConfirmation) {
            // handle subscription confirmation
            handleSubscriptionConfirmation((SnsSubscriptionConfirmation) snsMessage);
            return ok();
        } else if (snsMessage instanceof SnsUnsubscribeConfirmation) {
            //handle unsubscription confirmation
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
    private SnsMessage parseMessage(final String body) {
        SnsMessageManager snsMessageManager = snsMessageManagerProvider.get();

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
     * Processs a subscribe confirmation message from SNS. This uses the SDK to confirm the subscription to start
     * receiving notification events.
     *
     * @param message the Subscription confirmation message.
     */
    private void handleSubscriptionConfirmation(final SnsSubscriptionConfirmation message) {
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
    private void handleUnsubscriptionConfirmation(SnsUnsubscribeConfirmation message) {
        // do nothing besides log the event.
        log.warn("Unsubscribe event received; message:{} url:{}", message.getMessage(), message.getSubscribeUrl());
    }

    /**
     * Just return an HTTP Response of ok; here for readability in the handlers.
     *
     * @return an OK response
     */
    private Response ok() {
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
            log.error("Unable to deserialize etext notification payload", e);
            throw new BadRequestException("Unable to parse message content");
        }
    }
}
