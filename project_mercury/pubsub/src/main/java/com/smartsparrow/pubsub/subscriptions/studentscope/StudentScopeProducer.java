package com.smartsparrow.pubsub.subscriptions.studentscope;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.pubsub.data.AbstractProducer;

public class StudentScopeProducer extends AbstractProducer<StudentScopeConsumable> {

    private StudentScopeConsumable studentScopeConsumable;

    @Inject
    public StudentScopeProducer() {
    }

    public StudentScopeProducer buildStudentScopeConsumable(UUID studentId,
                                                            UUID deploymentId,
                                                            UUID studentScopeUrn,
                                                            Object studentScopeEntry) {
        this.studentScopeConsumable = new StudentScopeConsumable(
                new StudentScopeBroadcastMessage()
                        .setStudentId(studentId)
                        .setDeploymentId(deploymentId)
                        .setStudentScopeUrn(studentScopeUrn)
                        .setStudentScopeEntry(studentScopeEntry));
        return this;
    }

    @Override
    public StudentScopeConsumable getEventConsumable() {
        return studentScopeConsumable;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentScopeProducer that = (StudentScopeProducer) o;
        return Objects.equals(studentScopeConsumable, that.studentScopeConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentScopeConsumable);
    }

    @Override
    public String toString() {
        return "StudentScopeProducer{" +
                "studentScopeConsumable=" + studentScopeConsumable +
                '}';
    }
}
