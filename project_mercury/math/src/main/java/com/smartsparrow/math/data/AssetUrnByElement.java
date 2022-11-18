package com.smartsparrow.math.data;

import java.util.Objects;
import java.util.UUID;

public class AssetUrnByElement {

    private String assetUrn;
    private UUID elementId;

    public String getAssetUrn() {
        return assetUrn;
    }

    public AssetUrnByElement setAssetUrn(final String assetUrn) {
        this.assetUrn = assetUrn;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public AssetUrnByElement setElementId(final UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetUrnByElement that = (AssetUrnByElement) o;
        return Objects.equals(assetUrn, that.assetUrn) &&
                Objects.equals(elementId, that.elementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetUrn, elementId);
    }

    @Override
    public String toString() {
        return "AssetUrnByElement{" +
                "assetUrn='" + assetUrn + '\'' +
                ", elementId=" + elementId +
                '}';
    }
}
