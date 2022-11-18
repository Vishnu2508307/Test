package com.smartsparrow.courseware.data;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.asset.data.AssetProvider;

/**
 * Being replaced by asset urn tracking (part of immutable asset urn refactoring)
 */
@Deprecated
public class AssetByRootActivity {

    private UUID rootElementId;
    private UUID elementId;
    private AssetProvider assetProvider;
    private CoursewareElementType elementType;
    private UUID assetId;

    public UUID getRootElementId() {
        return rootElementId;
    }

    public AssetByRootActivity setRootElementId(final UUID rootElementId) {
        this.rootElementId = rootElementId;
        return this;
    }

    public UUID getElementId() {
        return elementId;
    }

    public AssetByRootActivity setElementId(final UUID elementId) {
        this.elementId = elementId;
        return this;
    }

    public AssetProvider getAssetProvider() {
        return assetProvider;
    }

    public AssetByRootActivity setAssetProvider(final AssetProvider assetProvider) {
        this.assetProvider = assetProvider;
        return this;
    }

    public CoursewareElementType getElementType() {
        return elementType;
    }

    public AssetByRootActivity setElementType(final CoursewareElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public AssetByRootActivity setAssetId(final UUID assetId) {
        this.assetId = assetId;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetByRootActivity that = (AssetByRootActivity) o;
        return Objects.equals(rootElementId, that.rootElementId) &&
                Objects.equals(elementId, that.elementId) &&
                assetProvider == that.assetProvider &&
                elementType == that.elementType &&
                Objects.equals(assetId, that.assetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootElementId, elementId, assetProvider, elementType, assetId);
    }

    @Override
    public String toString() {
        return "AssetByRootActivity{" +
                "rootElementId=" + rootElementId +
                ", elementId=" + elementId +
                ", assetProvider=" + assetProvider +
                ", elementType=" + elementType +
                ", assetId=" + assetId +
                '}';
    }
}
