package com.smartsparrow.pubsub.subscriptions.studentprogress;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;

public class StudentProgressRTMProducer extends AbstractProducer<StudentProgressRTMConsumable> {

    private StudentProgressRTMConsumable studentProgressRTMConsumable;

    @Inject
    public StudentProgressRTMProducer() {
    }

    public StudentProgressRTMProducer buildStudentProgressRTMConsumable(UUID studentId,
                                                                        UUID coursewareElementId,
                                                                        UUID deploymentId,
                                                                        Object progress) {
        this.studentProgressRTMConsumable = new StudentProgressRTMConsumable(
                new StudentProgressBroadcastMessage()
                        .setStudentId(studentId)
                        .setCoursewareElementId(coursewareElementId)
                        .setDeploymentId(deploymentId)
                        .setProgress(progress));
        return this;
    }

    @Override
    public StudentProgressRTMConsumable getEventConsumable() {
        return studentProgressRTMConsumable;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentProgressRTMProducer that = (StudentProgressRTMProducer) o;
        return Objects.equals(studentProgressRTMConsumable, that.studentProgressRTMConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentProgressRTMConsumable);
    }

    @Override
    public String toString() {
        return "StudentProgressRTMProducer{" +
                "studentProgressRTMConsumable=" + studentProgressRTMConsumable +
                '}';
    }
}
