package com.smartsparrow.annotation.service;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Object to store an Annotation PK
 */
public class CoursewareAnnotationKey implements Serializable {

    private static final long serialVersionUID = -99488244165781298L;

    private UUID id;
    private UUID version;

    public UUID getId() {
        return id;
    }

    public CoursewareAnnotationKey setId(final UUID id) {
        this.id = id;
        return this;
    }

    public UUID getVersion() {
        return version;
    }

    public CoursewareAnnotationKey setVersion(final UUID version) {
        this.version = version;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoursewareAnnotationKey that = (CoursewareAnnotationKey) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    @Override
    public String toString() {
        return "CoursewareAnnotationKey{" +
                "id=" + id +
                ", version=" + version +
                '}';
    }
}
