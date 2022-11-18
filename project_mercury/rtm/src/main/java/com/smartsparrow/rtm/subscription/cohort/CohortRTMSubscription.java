package com.smartsparrow.rtm.subscription.cohort;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.smartsparrow.rtm.subscription.data.AbstractRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;

/**
 * Defines a cohort RTM subscription
 */
public class CohortRTMSubscription extends AbstractRTMSubscription {

    private static final long serialVersionUID = 8204736412418508775L;

    public interface CohortRTMSubscriptionFactory {
        /**
         * Create a new instance of CohortRTMSubscription with a given cohortId
         *
         * @param cohortId the cohortId
         * @return the CohortRTMSubscription created instance
         */
        CohortRTMSubscription create(final UUID cohortId);
    }

    /**
     * Provides the name of the CohortRTMSubscription
     *
     * @param cohortId the cohort id this subscription is for
     * @return the subscription name
     */
    public static String NAME(final UUID cohortId) {
        return String.format("workspace.cohort/subscription/%s", cohortId);
    }

    private UUID cohortId;

    @Inject
    public CohortRTMSubscription(@Assisted UUID cohortId) {
        this.cohortId = cohortId;
    }

    @Override
    public Class<? extends RTMSubscription> getSubscriptionType() {
        return CohortRTMSubscription.class;
    }

    @Override
    public String getName() {
        return NAME(cohortId);
    }

    @Override
    public String getBroadcastType() {
        return "workspace.cohort.broadcast";
    }

    public UUID getCohortId() {
        return cohortId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortRTMSubscription that = (CohortRTMSubscription) o;
        return Objects.equals(cohortId, that.cohortId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cohortId);
    }

    @Override
    public String toString() {
        return "CohortRTMSubscription{" +
                "cohortId=" + cohortId +
                '}';
    }
}
