package com.smartsparrow.publication.data;

import java.util.Objects;
import java.util.UUID;

public class PublicationByExport {

    private UUID publicationId;
    private UUID exportId;

    public UUID getPublicationId() {
        return publicationId;
    }

    public PublicationByExport setPublicationId(UUID publicationId) {
        this.publicationId = publicationId;
        return this;
    }

    public UUID getExportId() {
        return exportId;
    }

    public PublicationByExport setExportId(UUID exportId) {
        this.exportId = exportId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicationByExport that = (PublicationByExport) o;
        return Objects.equals(publicationId, that.publicationId) &&
                Objects.equals(exportId, that.exportId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicationId, exportId);
    }

    @Override
    public String toString() {
        return "PublicationByExport{" +
                "publicationId=" + publicationId +
                ", exportId=" + exportId +
                '}';
    }
}
