package com.smartsparrow.ingestion.data;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;

public class IngestionPayload {

    private UUID ingestionId;
    private URL signedUrl;

    public UUID getIngestionId() {
        return ingestionId;
    }

    public IngestionPayload setIngestionId(UUID ingestionId) {
        this.ingestionId = ingestionId;
        return this;
    }

    public URL getSignedUrl() {
        return signedUrl;
    }

    public IngestionPayload setSignedUrl(URL signedUrl) {
        this.signedUrl = signedUrl;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IngestionPayload that = (IngestionPayload) o;
        return Objects.equals(ingestionId, that.ingestionId) &&
                Objects.equals(signedUrl, that.signedUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingestionId, signedUrl);
    }

    @Override
    public String toString() {
        return "IngestionPayload{" +
                "ingestionId=" + ingestionId +
                ", signedUrl='" + signedUrl + '\'' +
                '}';
    }
}
