package com.smartsparrow.learner.searchable;

import com.google.common.base.Objects;
import com.smartsparrow.learner.data.LearnerSearchableDocument;

/**
 * Just wraps a {@link LearnerSearchableDocument} in a "fields" field to conform with CSG api.
 */
public class LearnerSearchableDocumentIndexBodyWrapper {

    LearnerSearchableDocument fields;

    public LearnerSearchableDocument getFields() {
        return fields;
    }

    public LearnerSearchableDocumentIndexBodyWrapper setFields(LearnerSearchableDocument fields) {
        this.fields = fields;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LearnerSearchableDocumentIndexBodyWrapper that = (LearnerSearchableDocumentIndexBodyWrapper) o;
        return Objects.equal(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fields);
    }

    @Override
    public String toString() {
        return "LearnerSearchableDocumentIndexRequest{" +
                "fields=" + fields +
                '}';
    }
}
