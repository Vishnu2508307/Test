package com.smartsparrow.rtm.subscription.courseware;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.smartsparrow.rtm.subscription.data.AbstractRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;

/**
 * Defines an activity RTM subscription
 */
public class ActivityRTMSubscription extends AbstractRTMSubscription {

    private static final long serialVersionUID = -8713707789068936694L;

    public interface ActivityRTMSubscriptionFactory {
        /**
         * Create a new instance of ActivityRTMSubscription with a given activityId
         *
         * @param activityId the activityId
         * @return the ActivityRTMSubscription created instance
         */
        ActivityRTMSubscription create(final UUID activityId);
    }

    /**
     * Provides the name of the ActivityRTMSubscription
     *
     * @param activityId the activity id this subscription is for
     * @return the subscription name
     */
    public static String NAME(final UUID activityId) {
        return String.format("author.activity/%s", activityId);
    }

    private UUID activityId;

    @Inject
    public ActivityRTMSubscription(@Assisted final UUID activityId) {
        this.activityId = activityId;
    }

    @Override
    public Class<? extends RTMSubscription> getSubscriptionType() {
        return ActivityRTMSubscription.class;
    }

    @Override
    public String getName() {
        return NAME(activityId);
    }

    @Override
    public String getBroadcastType() {
        return "author.activity.broadcast";
    }

    public UUID getActivityId() {
        return activityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityRTMSubscription that = (ActivityRTMSubscription) o;
        return Objects.equals(activityId, that.activityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId);
    }

    @Override
    public String toString() {
        return "ActivityRTMSubscription{" +
                "activityId=" + activityId +
                '}';
    }
}