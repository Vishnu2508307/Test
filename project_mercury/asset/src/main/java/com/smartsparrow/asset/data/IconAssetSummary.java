package com.smartsparrow.asset.data;

import java.util.List;
import java.util.Objects;

public class IconAssetSummary {

    private String iconLibrary;
    private String assetUrn;
    private List<AssetMetadata> metadata;

    public String getIconLibrary() {
        return iconLibrary;
    }

    public IconAssetSummary setIconLibrary(final String iconLibrary) {
        this.iconLibrary = iconLibrary;
        return this;
    }

    public String getAssetUrn() {
        return assetUrn;
    }

    public IconAssetSummary setAssetUrn(final String assetUrn) {
        this.assetUrn = assetUrn;
        return this;
    }

    public List<AssetMetadata> getMetadata() {
        return metadata;
    }

    public IconAssetSummary setMetadata(final List<AssetMetadata> metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IconAssetSummary that = (IconAssetSummary) o;
        return iconLibrary == that.iconLibrary && Objects.equals(assetUrn,
                                                                 that.assetUrn) && Objects.equals(
                metadata,
                that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iconLibrary, assetUrn, metadata);
    }

    @Override
    public String toString() {
        return "IconAssetSummary{" +
                "iconLibrary=" + iconLibrary +
                ", assetUrn=" + assetUrn +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}
