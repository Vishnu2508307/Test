package com.smartsparrow.annotation.service;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Object to store Annotation aggregation numbers
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoursewareAnnotationAggregate implements Serializable {

    private static final long serialVersionUID = -1496577131519304022L;

    private final int total;
    private int read;
    private int unRead;
    private int resolved;
    private int unResolved;

    public CoursewareAnnotationAggregate(final int read,
                                         final int unRead,
                                         final int resolved,
                                         final int unResolved) {
        this.read = read;
        this.unRead = unRead;
        this.resolved = resolved;
        this.unResolved = unResolved;
        this.total = resolved + unResolved;
    }

    public int getTotal() {
        return total;
    }

    public int getRead() {
        return read;
    }

    public int setRead(final int read) {
        this.read = read;
        return read;
    }

    public int getUnRead() {
        return unRead;
    }

    public int setUnRead(final int unRead) {
        this.unRead = unRead;
        return unRead;
    }

    public int getResolved() {
        return resolved;
    }

    public int setResolved(final int resolved) {
        this.resolved = resolved;
        return resolved;
    }

    public int getUnResolved() {
        return unResolved;
    }

    public int setUnResolved(final int unResolved) {
        this.unResolved = unResolved;
        return unResolved;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursewareAnnotationAggregate that = (CoursewareAnnotationAggregate) o;
        return total == that.total &&
                read == that.read &&
                unRead == that.unRead &&
                resolved == that.resolved &&
                unResolved == that.unResolved;
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, read, unRead, resolved, unResolved);
    }

    @Override
    public String toString() {
        return "CoursewareAnnotationAggregate{" +
                "total=" + total +
                ", read=" + read +
                ", unRead=" + unRead +
                ", resolved=" + resolved +
                ", unResolved=" + unResolved +
                '}';
    }
}
