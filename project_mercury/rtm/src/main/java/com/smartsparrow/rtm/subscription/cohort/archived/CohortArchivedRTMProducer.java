package com.smartsparrow.rtm.subscription.cohort.archived;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

/**
 * This RTM producer produces an RTM event for a archived cohort
 */
public class CohortArchivedRTMProducer extends AbstractProducer<CohortArchivedRTMConsumable> {

    private CohortArchivedRTMConsumable cohortArchivedRTMConsumable;

    @Inject
    public CohortArchivedRTMProducer() {
    }

    public CohortArchivedRTMProducer buildCohortArchivedRTMConsumable(RTMClientContext rtmClientContext, UUID cohortId) {
        this.cohortArchivedRTMConsumable = new CohortArchivedRTMConsumable(rtmClientContext, new CohortBroadcastMessage(cohortId));
        return this;
    }

    @Override
    public CohortArchivedRTMConsumable getEventConsumable() {
        return cohortArchivedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortArchivedRTMProducer that = (CohortArchivedRTMProducer) o;
        return Objects.equals(cohortArchivedRTMConsumable, that.cohortArchivedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cohortArchivedRTMConsumable);
    }

    @Override
    public String toString() {
        return "CohortArchivedRTMProducer{" +
                "cohortArchivedRTMConsumable=" + cohortArchivedRTMConsumable +
                '}';
    }
}
