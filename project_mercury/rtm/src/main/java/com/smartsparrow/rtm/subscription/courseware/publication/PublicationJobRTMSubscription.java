package com.smartsparrow.rtm.subscription.courseware.publication;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.smartsparrow.rtm.subscription.data.AbstractRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;

/**
 * Defines a publication job RTM subscription
 */
public class PublicationJobRTMSubscription extends AbstractRTMSubscription {

    private static final long serialVersionUID = 8204736412418508775L;

    public interface PublicationJobRTMSubscriptionFactory {
        /**
         * Create a new instance of PublicationJobRTMSubscription with a given publicationId
         *
         * @param publicationId the publicationId
         * @return the PublicationJobRTMSubscription created instance
         */
        PublicationJobRTMSubscription create(final UUID publicationId);
    }

    /**
     * Provides the name of the PublicationJobRTMSubscription
     *
     * @param publicationId the publication id this subscription is for
     * @return the subscription name
     */
    public static String NAME(final UUID publicationId) {
        return String.format("publication/%s", publicationId);
    }

    private UUID publicationId;

    @Inject
    public PublicationJobRTMSubscription(@Assisted UUID publicationId) {
        this.publicationId = publicationId;
    }

    @Override
    public Class<? extends RTMSubscription> getSubscriptionType() {
        return PublicationJobRTMSubscription.class;
    }

    @Override
    public String getName() {
        return NAME(publicationId);
    }

    @Override
    public String getBroadcastType() {
        return "publication.job.broadcast";
    }

    public UUID getPublicationId() {
        return publicationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationJobRTMSubscription that = (PublicationJobRTMSubscription) o;
        return Objects.equals(publicationId, that.publicationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicationId);
    }

    @Override
    public String toString() {
        return "PublicationJobRTMSubscription{" +
                "publicationId=" + publicationId +
                '}';
    }
}
