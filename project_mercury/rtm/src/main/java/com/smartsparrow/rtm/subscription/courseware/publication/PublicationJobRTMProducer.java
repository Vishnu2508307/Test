package com.smartsparrow.rtm.subscription.courseware.publication;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.PublicationJobStatus;
import com.smartsparrow.courseware.eventmessage.PublicationJobBroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

/**
 * This RTM producer produces an RTM event for a publication job status
 */
public class PublicationJobRTMProducer extends AbstractProducer<PublicationJobRTMConsumable> {

    private PublicationJobRTMConsumable publicationJobRTMConsumable;

    @Inject
    public PublicationJobRTMProducer() {
    }

    public PublicationJobRTMProducer buildPublicationJobRTMConsumable(RTMClientContext rtmClientContext,
                                                                      UUID publicationId,
                                                                      final PublicationJobStatus publicationJobStatus,
                                                                      UUID jobId,
                                                                      String statusMessage,
                                                                      String bookId,
                                                                      String etextVersion) {
        this.publicationJobRTMConsumable = new PublicationJobRTMConsumable(rtmClientContext,
                                                                           new PublicationJobBroadcastMessage(
                                                                                   publicationId,
                                                                                   publicationJobStatus,
                                                                                   jobId, statusMessage,
                                                                                   bookId,
                                                                                   etextVersion));
        return this;
    }


    @Override
    public PublicationJobRTMConsumable getEventConsumable() {
        return publicationJobRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationJobRTMProducer that = (PublicationJobRTMProducer) o;
        return Objects.equals(publicationJobRTMConsumable, that.publicationJobRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicationJobRTMConsumable);
    }

    @Override
    public String toString() {
        return "PublicationJobRTMProducer{" +
                "publicationJobRTMConsumable=" + publicationJobRTMConsumable +
                '}';
    }
}
