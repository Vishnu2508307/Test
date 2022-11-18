package com.smartsparrow.rtm.subscription.cohort.disenrolled;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

/**
 * This RTM producer produces an RTM event for a dis enrolled cohort
 */
public class CohortDisEnrolledRTMProducer extends AbstractProducer<CohortDisEnrolledRTMConsumable> {

   CohortDisEnrolledRTMConsumable cohortDisEnrolledRTMConsumable;

    @Inject
    public CohortDisEnrolledRTMProducer() {
    }

    public CohortDisEnrolledRTMProducer buildCohortDisEnrolledRTMConsumable(RTMClientContext rtmClientContext, UUID cohortId) {
        this.cohortDisEnrolledRTMConsumable = new CohortDisEnrolledRTMConsumable(rtmClientContext, new CohortBroadcastMessage(cohortId));
        return this;
    }

    @Override
    public CohortDisEnrolledRTMConsumable getEventConsumable() {
        return cohortDisEnrolledRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortDisEnrolledRTMProducer that = (CohortDisEnrolledRTMProducer) o;
        return Objects.equals(cohortDisEnrolledRTMConsumable, that.cohortDisEnrolledRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cohortDisEnrolledRTMConsumable);
    }

    @Override
    public String toString() {
        return "CohortDisEnrolledRTMProducer{" +
                "cohortDisEnrolledRTMConsumable=" + cohortDisEnrolledRTMConsumable +
                '}';
    }
}
