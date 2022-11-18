package com.smartsparrow.rtm.subscription.cohort.granted;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

/**
 * This RTM producer produces an RTM event for a granted cohort
 */
public class CohortGrantedRTMProducer extends AbstractProducer<CohortGrantedRTMConsumable> {

    private CohortGrantedRTMConsumable cohortGrantedRTMConsumable;

    @Inject
    public CohortGrantedRTMProducer() {
    }

    public CohortGrantedRTMProducer buildCohortGrantedRTMConsumable(RTMClientContext rtmClientContext, UUID cohortId) {
        this.cohortGrantedRTMConsumable = new CohortGrantedRTMConsumable(rtmClientContext, new CohortBroadcastMessage(cohortId));
        return this;
    }

    @Override
    public CohortGrantedRTMConsumable getEventConsumable() {
        return cohortGrantedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortGrantedRTMProducer that = (CohortGrantedRTMProducer) o;
        return Objects.equals(cohortGrantedRTMConsumable, that.cohortGrantedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cohortGrantedRTMConsumable);
    }

    @Override
    public String toString() {
        return "CohortGrantedRTMProducer{" +
                "cohortGrantedRTMConsumable=" + cohortGrantedRTMConsumable +
                '}';
    }
}
