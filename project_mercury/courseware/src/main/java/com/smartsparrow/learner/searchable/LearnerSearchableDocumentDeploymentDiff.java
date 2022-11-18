package com.smartsparrow.learner.searchable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Sets;
import com.smartsparrow.learner.data.LearnerSearchableDocumentIdentity;

public class LearnerSearchableDocumentDeploymentDiff {

    private final Set<LearnerSearchableDocumentIdentity> current = new HashSet<>();
    private final Set<LearnerSearchableDocumentIdentity> previous = new HashSet<>();

    public Set<LearnerSearchableDocumentIdentity> getCurrentDeploymentElements() {
        return current;
    }

    public Set<LearnerSearchableDocumentIdentity> getPreviousDeploymentElements() {
        return previous;
    }

    /**
     * Returns a set containing  all elements that are present in previous deployment, but are no longer
     * present in the most recent version of deployment.
     */
    public Set<LearnerSearchableDocumentIdentity> getDiff() {
        return Sets.difference(previous, current);
    }

    @Override
    public String toString() {
        return "LearnerSearchableDocumentDeploymentDiff{" +
                "current=" + current +
                ", previous=" + previous +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerSearchableDocumentDeploymentDiff that = (LearnerSearchableDocumentDeploymentDiff) o;
        return Objects.equals(current, that.current) &&
                Objects.equals(previous, that.previous);
    }

    @Override
    public int hashCode() {
        return Objects.hash(current, previous);
    }
}
