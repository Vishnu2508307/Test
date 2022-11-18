package com.smartsparrow.publication.job.data;

import java.util.Objects;
import java.util.UUID;

public class JobByPublication {

    private UUID publicationId;
    private UUID jobId;

    public UUID getPublicationId() {
        return publicationId;
    }

    public JobByPublication setPublicationId(UUID publicationId) {
        this.publicationId = publicationId;
        return this;
    }

    public UUID getJobId() {
        return jobId;
    }

    public JobByPublication setJobId(UUID jobId) {
        this.jobId = jobId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobByPublication that = (JobByPublication) o;
        return Objects.equals(publicationId, that.publicationId) && Objects.equals(jobId, that.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicationId, jobId);
    }

    @Override
    public String toString() {
        return "JobByPublication{" +
                "publicationId=" + publicationId +
                ", jobId=" + jobId +
                '}';
    }
}
