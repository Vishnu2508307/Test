package com.smartsparrow.rtm.subscription.learner.studentscope;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.smartsparrow.rtm.subscription.data.AbstractRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;

/**
 * Defines a student scope RTM subscription
 */
public class StudentScopeRTMSubscription extends AbstractRTMSubscription {

    private static final long serialVersionUID = 1522967726788166988L;

    public interface StudentScopeRTMSubscriptionFactory {
        /**
         * Create a new instance of StudentScopeRTMSubscription with a given studentId
         *
         * @param studentId the student id
         * @param deploymentId the deployment id
         * @return the StudentScopeRTMSubscription created instance
         */
        StudentScopeRTMSubscription create(@Assisted("studentId") final UUID studentId, @Assisted("deploymentId") final UUID deploymentId);
    }

    /**
     * Provides the name of the StudentScopeRTMSubscription
     *
     * @param studentId the student id
     * @param deploymentId the deployment id
     * @return the subscription name
     */
    public static String NAME(UUID studentId, UUID deploymentId) {
        return String.format("learner.student.scope/%s/%s", studentId, deploymentId);
    }

    private UUID studentId;
    private UUID deploymentId;

    @Inject
    public StudentScopeRTMSubscription(@Assisted("studentId") final UUID studentId, @Assisted("deploymentId") final UUID deploymentId) {
        this.studentId = studentId;
        this.deploymentId = deploymentId;
    }

    @Override
    public Class<? extends RTMSubscription> getSubscriptionType() {
        return StudentScopeRTMSubscription.class;
    }

    @Override
    public String getName() {
        return NAME(studentId, deploymentId);
    }

    @Override
    public String getBroadcastType() {
        return "learner.student.scope.broadcast";
    }

    public UUID getStudentId() {
        return studentId;
    }


    public UUID getDeploymentId() {
        return deploymentId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentScopeRTMSubscription that = (StudentScopeRTMSubscription) o;
        return Objects.equals(studentId, that.studentId) && Objects.equals(deploymentId,
                                                                           that.deploymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, deploymentId);
    }

    @Override
    public String toString() {
        return "StudentScopeRTMSubscription{" +
                "studentId=" + studentId +
                ", deploymentId=" + deploymentId +
                '}';
    }
}
