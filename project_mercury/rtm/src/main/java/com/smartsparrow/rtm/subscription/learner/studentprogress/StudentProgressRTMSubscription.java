package com.smartsparrow.rtm.subscription.learner.studentprogress;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.smartsparrow.rtm.subscription.data.AbstractRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;

/**
 * Defines a student progress RTM subscription
 */
public class StudentProgressRTMSubscription extends AbstractRTMSubscription {

    private static final long serialVersionUID = 8204736412418508775L;

    public interface StudentProgressRTMSubscriptionFactory {
        /**
         * Create a new instance of StudentProgressRTMSubscription with a given deploymentId, coursewareElementId, studentId
         *
         * @param deploymentId the deploymentId
         * @param coursewareElementId the coursewareElementId
         * @param studentId the studentId
         * @return the StudentProgressRTMSubscription created instance
         */
        StudentProgressRTMSubscription create(final @Assisted("deploymentId") UUID deploymentId,
                                              final @Assisted("coursewareElementId") UUID coursewareElementId,
                                              final @Assisted("studentId") UUID studentId);
    }
    /**
     * Provides the name of the StudentProgressRTMSubscription
     *
     * @param studentId the student id
     * @param deploymentId the deployment id
     * @param elementId the element id
     * @return the subscription name
     */
    public static String NAME(final UUID studentId,
                              final UUID deploymentId, final UUID elementId) {
        return String.format("learner.progress/%s/%s/%s", studentId, deploymentId, elementId);
    }

    private UUID deploymentId;
    private UUID coursewareElementId;
    private UUID studentId;

    @Inject
    public StudentProgressRTMSubscription(@Assisted("deploymentId") UUID deploymentId,
                                          final @Assisted("coursewareElementId") UUID coursewareElementId,
                                          final @Assisted("studentId") UUID studentId) {
        this.deploymentId = deploymentId;
        this.coursewareElementId = coursewareElementId;
        this.studentId = studentId;
    }

    @Override
    public Class<? extends RTMSubscription> getSubscriptionType() {
        return StudentProgressRTMSubscription.class;
    }

    @Override
    public String getName() {
        return NAME(studentId, deploymentId, coursewareElementId);
    }

    @Override
    public String getBroadcastType() {
        return "learner.progress.broadcast";
    }

    public UUID getDeploymentId() {
        return deploymentId;
    }

    public UUID getCoursewareElementId() {
        return coursewareElementId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentProgressRTMSubscription that = (StudentProgressRTMSubscription) o;
        return Objects.equals(deploymentId, that.deploymentId) &&
                Objects.equals(coursewareElementId, that.coursewareElementId) &&
                Objects.equals(studentId, that.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deploymentId, coursewareElementId, studentId);
    }

    @Override
    public String toString() {
        return "StudentProgressRTMSubscription{" +
                "deploymentId=" + deploymentId +
                ", coursewareElementId=" + coursewareElementId +
                ", studentId=" + studentId +
                '}';
    }
}
