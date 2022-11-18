package com.smartsparrow.rtm.subscription.cohort.unarchived;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

/**
 * This RTM producer produces an RTM event for a unarchived cohort
 */
public class CohortUnArchivedRTMProducer extends AbstractProducer<CohortUnArchivedRTMConsumable> {

    private CohortUnArchivedRTMConsumable cohortUnArchivedRTMConsumable;

    @Inject
    public CohortUnArchivedRTMProducer() {
    }

    public CohortUnArchivedRTMProducer buildCohortUnArchivedRTMConsumable(RTMClientContext rtmClientContext, UUID cohortId) {
        this.cohortUnArchivedRTMConsumable = new CohortUnArchivedRTMConsumable(rtmClientContext, new CohortBroadcastMessage(cohortId));
        return this;
    }

    @Override
    public CohortUnArchivedRTMConsumable getEventConsumable() {
        return cohortUnArchivedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortUnArchivedRTMProducer that = (CohortUnArchivedRTMProducer) o;
        return Objects.equals(cohortUnArchivedRTMConsumable, that.cohortUnArchivedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cohortUnArchivedRTMConsumable);
    }

    @Override
    public String toString() {
        return "CohortUnArchivedRTMProducer{" +
                "cohortUnArchivedRTMConsumable=" + cohortUnArchivedRTMConsumable +
                '}';
    }
}
