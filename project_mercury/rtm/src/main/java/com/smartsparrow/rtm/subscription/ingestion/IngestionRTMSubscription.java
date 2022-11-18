package com.smartsparrow.rtm.subscription.ingestion;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.smartsparrow.rtm.subscription.data.AbstractRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;

/**
 * Defines an Ingestion RTM subscription
 */
public class IngestionRTMSubscription extends AbstractRTMSubscription {

    private static final long serialVersionUID = 8204736412418508775L;

    public interface IngestionRTMSubscriptionFactory {
        /**
         * Create a new instance of IngestionRTMSubscription with a given ingestionId
         *
         * @param ingestionId the ingestion id
         * @return the IngestionRTMSubscription created instance
         */
        IngestionRTMSubscription create(final UUID ingestionId);
    }
    /**
     * Provides the name of the IngestionRTMSubscription
     *
     * @param ingestionId the ingestion id
     * @return the subscription name
     */
    public static String NAME(final UUID ingestionId) {
        return String.format("project.ingest/%s", ingestionId);
    }

    private UUID ingestionId;

    @Inject
    public IngestionRTMSubscription(@Assisted final UUID ingestionId) {
        this.ingestionId = ingestionId;
    }

    @Override
    public Class<? extends RTMSubscription> getSubscriptionType() {
        return IngestionRTMSubscription.class;
    }

    @Override
    public String getName() {
        return NAME(ingestionId);
    }

    @Override
    public String getBroadcastType() {
        return "project.ingest.broadcast";
    }

    public UUID getIngestionId() {
        return ingestionId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngestionRTMSubscription that = (IngestionRTMSubscription) o;
        return Objects.equals(ingestionId, that.ingestionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingestionId);
    }

    @Override
    public String toString() {
        return "IngestionRTMSubscription{" +
                "ingestionId=" + ingestionId +
                '}';
    }
}
