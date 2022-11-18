package com.smartsparrow.pubsub.subscriptions.learner;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.pubsub.data.AbstractProducer;

/**
 * This RTM producer produces an event for a prefetched walkable
 */
@Singleton
public class StudentWalkablePrefetchProducer extends AbstractProducer<StudentWalkablePrefetchConsumable> {

    private StudentWalkablePrefetchConsumable studentWalkablePrefetchConsumable;

    @Inject
    public StudentWalkablePrefetchProducer() {
    }

    public StudentWalkablePrefetchProducer buildStudentWalkablePrefetchConsumable(UUID accountId,
                                                                                        Object walkable) {
        this.studentWalkablePrefetchConsumable = new StudentWalkablePrefetchConsumable(new StudentWalkablePrefetchBroadcastMessage(accountId, walkable));
        return this;
    }

    @Override
    public StudentWalkablePrefetchConsumable getEventConsumable() {
        return studentWalkablePrefetchConsumable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentWalkablePrefetchProducer that = (StudentWalkablePrefetchProducer) o;
        return Objects.equals(studentWalkablePrefetchConsumable, that.studentWalkablePrefetchConsumable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentWalkablePrefetchConsumable);
    }

    @Override
    public String toString() {
        return "StudentWalkablePrefetchProducer{" +
                "studentWalkablePrefetchConsumable=" + studentWalkablePrefetchConsumable +
                '}';
    }
}
