package com.smartsparrow.math.data;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UsageByAssetUrn {

    private String assetUrn;
    private UUID assetId;
    private List<String> elementId;

    public String getAssetUrn() {
        return assetUrn;
    }

    public UsageByAssetUrn setAssetUrn(String assetUrn) {
        this.assetUrn = assetUrn;
        return this;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public UsageByAssetUrn setAssetId(UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    public List<String> getElementId() {
        return elementId;
    }

    public UsageByAssetUrn setElementId(final List<String> elementId) {
        this.elementId = elementId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsageByAssetUrn that = (UsageByAssetUrn) o;
        return Objects.equals(assetUrn, that.assetUrn) &&
                Objects.equals(assetId, that.assetId) &&
                Objects.equals(elementId, that.elementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetUrn, assetId, elementId);
    }

    @Override
    public String toString() {
        return "UsageByAssetUrn{" +
                "assetUrn='" + assetUrn + '\'' +
                ", assetId=" + assetId +
                ", elementIds=" + elementId +
                '}';
    }
}
