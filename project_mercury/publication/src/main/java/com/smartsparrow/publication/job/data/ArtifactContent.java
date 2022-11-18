package com.smartsparrow.publication.job.data;

import java.util.Objects;
import java.util.UUID;

public class ArtifactContent {

    private UUID id;
    private UUID artifactId;

    public UUID getId() {
        return id;
    }

    public ArtifactContent setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getArtifactId() {
        return artifactId;
    }

    public ArtifactContent setArtifactId(UUID artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtifactContent that = (ArtifactContent) o;
        return Objects.equals(id, that.id) && Objects.equals(artifactId, that.artifactId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, artifactId);
    }

    @Override
    public String toString() {
        return "ArtifactContent{" +
                "id=" + id +
                ", artifactId=" + artifactId +
                '}';
    }
}
