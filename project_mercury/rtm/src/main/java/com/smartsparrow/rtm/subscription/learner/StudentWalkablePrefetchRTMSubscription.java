package com.smartsparrow.rtm.subscription.learner;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.smartsparrow.rtm.subscription.data.AbstractRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;

/**
 * Defines a student prefetch RTM subscription
 */
public class StudentWalkablePrefetchRTMSubscription extends AbstractRTMSubscription {

    private static final long serialVersionUID = 1466864539869378493L;

    public interface StudentWalkablePrefetchRTMSubscriptionFactory {
        /**
         * Create a new instance of StudentWalkablePrefetchRTMSubscription with a given studentId
         *
         * @param studentId the studentId
         * @return the StudentWalkablePrefetchRTMSubscription created instance
         */
        StudentWalkablePrefetchRTMSubscription create(final UUID studentId);
    }
    /**
     * Provides the name of the StudentPrefetchRTMSubscription
     *
     * @param studentId the student id this subscription is for
     * @return the subscription name
     */
    public static String NAME(final UUID studentId) {
        return String.format("student/%s", studentId);
    }

    private UUID studentId;

    @Inject
    public StudentWalkablePrefetchRTMSubscription(@Assisted final UUID studentId) {
        this.studentId = studentId;
    }

    @Override
    public Class<? extends RTMSubscription> getSubscriptionType() {
        return StudentWalkablePrefetchRTMSubscription.class;
    }

    @Override
    public String getName() {
        return NAME(studentId);
    }

    @Override
    public String getBroadcastType() {
        return "learner.student.walkable.prefetch.broadcast";
    }

    public UUID getStudentId() {
        return studentId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentWalkablePrefetchRTMSubscription that = (StudentWalkablePrefetchRTMSubscription) o;
        return Objects.equals(studentId, that.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }

    @Override
    public String toString() {
        return "StudentPrefetchRTMSubscription{" +
                "studentId=" + studentId +
                '}';
    }
}
