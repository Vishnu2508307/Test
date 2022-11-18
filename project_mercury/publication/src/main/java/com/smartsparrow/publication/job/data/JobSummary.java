package com.smartsparrow.publication.job.data;

import com.smartsparrow.publication.job.enums.JobStatus;
import com.smartsparrow.publication.job.enums.JobType;

import java.util.Objects;
import java.util.UUID;

public class JobSummary {

    private UUID id;
    private JobType jobType;
    private JobStatus status;
    private String statusDesc;

    public UUID getId() {
        return id;
    }

    public JobSummary setId(UUID id) {
        this.id = id;
        return this;
    }

    public JobType getJobType() {
        return jobType;
    }

    public JobSummary setJobType(JobType jobType) {
        this.jobType = jobType;
        return this;
    }

    public JobStatus getStatus() {
        return status;
    }

    public JobSummary setStatus(JobStatus status) {
        this.status = status;
        return this;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public JobSummary setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobSummary jobSummary = (JobSummary) o;
        return Objects.equals(id, jobSummary.id) &&
                Objects.equals(jobType, jobSummary.jobType) &&
                Objects.equals(status, jobSummary.status) &&
                Objects.equals(statusDesc, jobSummary.statusDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, jobType, status, statusDesc);
    }

    @Override
    public String toString() {
        return "JobSummary{" +
                "id=" + id +
                ", jobType=" + jobType +
                ", status=" + status +
                ", statusDesc=" + statusDesc +
                '}';
    }
}
