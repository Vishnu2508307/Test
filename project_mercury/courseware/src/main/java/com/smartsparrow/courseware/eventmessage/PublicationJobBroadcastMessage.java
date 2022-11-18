package com.smartsparrow.courseware.eventmessage;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.courseware.data.PublicationJobStatus;
import com.smartsparrow.dataevent.BroadcastMessage;

public class PublicationJobBroadcastMessage implements BroadcastMessage {

    private static final long serialVersionUID = 1011336248959220253L;

    private UUID publicationId;
    private PublicationJobStatus publicationJobStatus;
    private UUID jobId;
    private String statusMessage;
    private String bookId;
    private String etextVersion;

    public PublicationJobBroadcastMessage(UUID publicationId, PublicationJobStatus publicationJobStatus,
                                          UUID jobId, String statusMessage, String bookId, String etextVersion) {
        this.publicationId = publicationId;
        this.publicationJobStatus = publicationJobStatus;
        this.jobId = jobId;
        this.statusMessage = statusMessage;
        this.bookId = bookId;
        this.etextVersion = etextVersion;
    }

    public UUID getPublicationId() {
        return publicationId;
    }

    public PublicationJobBroadcastMessage setPublicationId(UUID publicationId) {
        this.publicationId = publicationId;
        return this;
    }

    public PublicationJobStatus getPublicationJobStatus() {
        return publicationJobStatus;
    }

    public PublicationJobBroadcastMessage setPublicationJobStatus(PublicationJobStatus publicationJobStatus) {
        this.publicationJobStatus = publicationJobStatus;
        return this;
    }

    public UUID getJobId() {
        return jobId;
    }

    public PublicationJobBroadcastMessage setJobId(UUID jobId) {
        this.jobId = jobId;
        return this;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public PublicationJobBroadcastMessage setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    public String getBookId() {
        return bookId;
    }

    public PublicationJobBroadcastMessage setBookId(final String bookId) {
        this.bookId = bookId;
        return this;
    }

    public String getEtextVersion() {
        return etextVersion;
    }

    public PublicationJobBroadcastMessage setEtextVersion(final String etextVersion) {
        this.etextVersion = etextVersion;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationJobBroadcastMessage that = (PublicationJobBroadcastMessage) o;
        return Objects.equals(publicationId, that.publicationId) &&
                Objects.equals(publicationJobStatus, that.publicationJobStatus) &&
                Objects.equals(jobId, that.jobId) &&
                Objects.equals(statusMessage, that.statusMessage) &&
                Objects.equals(bookId, that.bookId) &&
                Objects.equals(etextVersion, that.etextVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicationId, publicationJobStatus, jobId, statusMessage, bookId, etextVersion);
    }

    @Override
    public String toString() {
        return "PublicationJobBroadcastMessage{" +
                "publicationId=" + publicationId +
                ", publicationJobStatus= " + publicationJobStatus +
                ", jobId= " + jobId +
                ", statusMessage= " + statusMessage +
                ", bookId= " + bookId +
                ", etextVersion= " + etextVersion +
                '}';
    }
}
