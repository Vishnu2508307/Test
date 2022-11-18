package com.smartsparrow.publication.data;

import java.util.Objects;
import java.util.UUID;

public class PublicationByActivity {

    private UUID publicationId;
    private UUID activityId;

    public UUID getPublicationId() {
        return publicationId;
    }

    public PublicationByActivity setPublicationId(UUID publicationId) {
        this.publicationId = publicationId;
        return this;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public PublicationByActivity setActivityId(UUID activityId) {
        this.activityId = activityId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationByActivity that = (PublicationByActivity) o;
        return Objects.equals(publicationId, that.publicationId) &&
                Objects.equals(activityId, that.activityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicationId, activityId);
    }

    @Override
    public String toString() {
        return "PublicationByActivity{" +
                "publicationId=" + publicationId +
                ", activityId=" + activityId +
                '}';
    }
}
