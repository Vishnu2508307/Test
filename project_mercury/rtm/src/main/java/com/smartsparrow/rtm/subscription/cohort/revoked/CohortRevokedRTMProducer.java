package com.smartsparrow.rtm.subscription.cohort.revoked;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.pubsub.data.AbstractProducer;
import com.smartsparrow.rtm.message.RTMClientContext;

/**
 * This RTM producer produces an RTM event for a revoked cohort
 */
public class CohortRevokedRTMProducer extends AbstractProducer<CohortRevokedRTMConsumable> {

    private CohortRevokedRTMConsumable cohortRevokedRTMConsumable;

    @Inject
    public CohortRevokedRTMProducer() {
    }

    public CohortRevokedRTMProducer buildCohortRevokedRTMConsumable(RTMClientContext rtmClientContext, UUID cohortId) {
        this.cohortRevokedRTMConsumable = new CohortRevokedRTMConsumable(rtmClientContext, new CohortBroadcastMessage(cohortId));
        return this;
    }

    @Override
    public CohortRevokedRTMConsumable getEventConsumable() {
        return cohortRevokedRTMConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortRevokedRTMProducer that = (CohortRevokedRTMProducer) o;
        return Objects.equals(cohortRevokedRTMConsumable, that.cohortRevokedRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cohortRevokedRTMConsumable);
    }

    @Override
    public String toString() {
        return "CohortRevokedRTMProducer{" +
                "cohortRevokedRTMConsumable=" + cohortRevokedRTMConsumable +
                '}';
    }
}
