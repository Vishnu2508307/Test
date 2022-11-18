package com.smartsparrow.rtm.message.recv.courseware.publication;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.publication.data.PublicationOutputType;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreatePublicationMessage extends ReceivedMessage {

    private UUID activityId;
    private UUID accountId;
    private String publicationTitle;
    private String description;
    private String author;
    private String version;
    private String config;
    private UUID exportId;
    private PublicationOutputType outputType;

    public UUID getActivityId() {
        return activityId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getPublicationTitle() {
        return publicationTitle;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public String getConfig() {
        return config;
    }

    public String getVersion() { return version; }

    public UUID getExportId() { return exportId; }

    public PublicationOutputType getOutputType() {
        return outputType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreatePublicationMessage that = (CreatePublicationMessage) o;
        return Objects.equals(activityId, that.activityId) &&
                Objects.equals(accountId, that.accountId) &&
                Objects.equals(publicationTitle, that.publicationTitle) &&
                Objects.equals(description, that.description) &&
                Objects.equals(author, that.author) &&
                Objects.equals(version, that.version) &&
                Objects.equals(config, that.config) &&
                Objects.equals(exportId, that.exportId) &&
                outputType == that.getOutputType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(activityId, accountId, publicationTitle, description, author, version, config, exportId, outputType);
    }

    @Override
    public String toString() {
        return "CreatePublicationMessage{" +
                "activityId=" + activityId +
                ", accountId=" + accountId +
                ", publicationTitle=" + publicationTitle +
                ", description=" + description +
                ", author=" + author +
                ", version=" + version +
                ", config=" + config +
                ", exportId=" + exportId +
                ", outputType=" + outputType +
                '}';
    }
}
