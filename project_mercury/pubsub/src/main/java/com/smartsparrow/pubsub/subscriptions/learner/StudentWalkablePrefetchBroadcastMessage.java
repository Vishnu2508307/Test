package com.smartsparrow.pubsub.subscriptions.learner;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.dataevent.BroadcastMessage;

public class StudentWalkablePrefetchBroadcastMessage implements BroadcastMessage {
    private static final long serialVersionUID = 2825281989666981474L;

    protected final UUID studentId;
    protected final Object walkable;

    public StudentWalkablePrefetchBroadcastMessage(UUID studentId, Object walkable) {
        this.studentId = studentId;
        this.walkable = walkable;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public Object getWalkable() {
        return walkable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentWalkablePrefetchBroadcastMessage that = (StudentWalkablePrefetchBroadcastMessage) o;
        return Objects.equals(studentId, that.studentId) &&
                Objects.equals(walkable, that.walkable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, walkable);
    }

    @Override
    public String toString() {
        return "StudentWalkablePrefetchBroadcastMessage{" +
                "studentId=" + studentId +
                ", walkable=" + walkable +
                '}';
    }
}
