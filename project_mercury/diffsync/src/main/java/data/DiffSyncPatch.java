package data;

import java.util.LinkedList;
import java.util.Objects;


public class DiffSyncPatch {

    public LinkedList<DiffSyncDiff> diffs;
    public int start1;
    public int start2;
    public int length1;
    public int length2;

    public LinkedList<DiffSyncDiff> getDiffs() {
        return diffs;
    }

    public DiffSyncPatch setDiffs(final LinkedList<DiffSyncDiff> diffs) {
        this.diffs = diffs;
        return this;
    }

    public int getStart1() {
        return start1;
    }

    public DiffSyncPatch setStart1(final int start1) {
        this.start1 = start1;
        return this;
    }

    public int getStart2() {
        return start2;
    }

    public DiffSyncPatch setStart2(final int start2) {
        this.start2 = start2;
        return this;
    }

    public int getLength1() {
        return length1;
    }

    public DiffSyncPatch setLength1(final int length1) {
        this.length1 = length1;
        return this;
    }

    public int getLength2() {
        return length2;
    }

    public DiffSyncPatch setLength2(final int length2) {
        this.length2 = length2;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffSyncPatch that = (DiffSyncPatch) o;
        return start1 == that.start1 &&
                start2 == that.start2 &&
                length1 == that.length1 &&
                length2 == that.length2 &&
                Objects.equals(diffs, that.diffs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(diffs, start1, start2, length1, length2);
    }

    @Override
    public String toString() {
        return "DiffSyncPatch{" +
                "diffs=" + diffs +
                ", start1=" + start1 +
                ", start2=" + start2 +
                ", length1=" + length1 +
                ", length2=" + length2 +
                '}';
    }
}
