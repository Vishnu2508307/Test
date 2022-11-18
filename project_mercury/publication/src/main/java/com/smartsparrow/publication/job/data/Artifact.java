package com.smartsparrow.publication.job.data;

import com.smartsparrow.publication.job.enums.ArtifactType;

import java.util.Objects;
import java.util.UUID;

public class Artifact {
    private UUID id;
    private UUID jobId;
    private ArtifactType artifactType;

    public UUID getId() {
        return id;
    }

    public Artifact setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Artifact setJobId(UUID jobId) {
        this.jobId = jobId;
        return this;
    }

    public ArtifactType getArtifactType() {
        return artifactType;
    }

    public Artifact setArtifactType(ArtifactType artifactType) {
        this.artifactType = artifactType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artifact artifact = (Artifact) o;
        return Objects.equals(id, artifact.id) && Objects.equals(jobId, artifact.jobId) && Objects.equals(artifactType, artifact.artifactType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, jobId, artifactType);
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "id=" + id +
                ", jobId=" + jobId +
                ", artifactType=" + artifactType +
                '}';
    }
}