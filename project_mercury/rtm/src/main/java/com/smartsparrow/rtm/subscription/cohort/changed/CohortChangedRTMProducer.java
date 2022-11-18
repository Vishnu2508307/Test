package com.smartsparrow.rtm.subscription.cohort.changed;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

/**
 * This RTM producer produces an RTM event for a changed cohort
 */
public class CohortChangedRTMProducer extends AbstractProducer<CohortChangedRTMConsumable> {

    private CohortChangedRTMConsumable cohortChangedRTMConsumable;

    @Inject
    public CohortChangedRTMProducer() {
    }

    public CohortChangedRTMProducer buildCohortChangedRTMConsumable(RTMClientContext rtmClientContext, UUID cohortId) {
        this.cohortChangedRTMConsumable = new CohortChangedRTMConsumable(rtmClientContext, new CohortBroadcastMessage(cohortId));
        return this;
    }

    @Override
    public CohortChangedRTMConsumable getEventConsumable() {
        return cohortChangedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortChangedRTMProducer that = (CohortChangedRTMProducer) o;
        return Objects.equals(cohortChangedRTMConsumable, that.cohortChangedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cohortChangedRTMConsumable);
    }

    @Override
    public String toString() {
        return "CohortChangedRTMProducer{" +
                "cohortChangedRTMConsumable=" + cohortChangedRTMConsumable +
                '}';
    }
}
