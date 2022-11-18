package com.smartsparrow.rtm.subscription.cohort.enrolled;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

/**
 * This RTM producer produces an RTM event for a enrolled cohort
 */
public class CohortEnrolledRTMProducer extends AbstractProducer<CohortEnrolledRTMConsumable> {

   CohortEnrolledRTMConsumable cohortEnrolledRTMConsumable;

    @Inject
    public CohortEnrolledRTMProducer() {
    }

    public CohortEnrolledRTMProducer buildCohortEnrolledRTMConsumable(RTMClientContext rtmClientContext, UUID cohortId) {
        this.cohortEnrolledRTMConsumable = new CohortEnrolledRTMConsumable(rtmClientContext, new CohortBroadcastMessage(cohortId));
        return this;
    }

    @Override
    public CohortEnrolledRTMConsumable getEventConsumable() {
        return cohortEnrolledRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortEnrolledRTMProducer that = (CohortEnrolledRTMProducer) o;
        return Objects.equals(cohortEnrolledRTMConsumable, that.cohortEnrolledRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cohortEnrolledRTMConsumable);
    }

    @Override
    public String toString() {
        return "CohortEnrolledRTMProducer{" +
                "cohortEnrolledRTMConsumable=" + cohortEnrolledRTMConsumable +
                '}';
    }
}
