package data;

import java.util.Objects;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

public class DiffSyncDiff {

    /**
     * One of: INSERT, DELETE or EQUAL.
     */
    public DiffMatchPatch.Operation operation;
    public String text;

    public DiffMatchPatch.Operation getOperation() {
        return operation;
    }

    public DiffSyncDiff setOperation(final DiffMatchPatch.Operation operation) {
        this.operation = operation;
        return this;
    }

    public String getText() {
        return text;
    }

    public DiffSyncDiff setText(final String text) {
        this.text = text;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DiffSyncDiff that = (DiffSyncDiff) o;
        return operation == that.operation &&
                Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), operation, text);
    }

    @Override
    public String toString() {
        return "DiffSyncDiff{" +
                "operation=" + operation +
                ", text='" + text + '\'' +
                '}';
    }
}
