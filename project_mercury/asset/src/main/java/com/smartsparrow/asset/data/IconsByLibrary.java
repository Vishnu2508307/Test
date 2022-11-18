package com.smartsparrow.asset.data;

import java.util.Objects;

public class IconsByLibrary {

    private String iconLibrary;
    private String assetUrn;

    public String getIconLibrary() {
        return iconLibrary;
    }

    public IconsByLibrary setIconLibrary(final String iconLibrary) {
        this.iconLibrary = iconLibrary;
        return this;
    }

    public String getAssetUrn() {
        return assetUrn;
    }

    public IconsByLibrary setAssetUrn(final String assetUrn) {
        this.assetUrn = assetUrn;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IconsByLibrary that = (IconsByLibrary) o;
        return iconLibrary == that.iconLibrary && Objects.equals(assetUrn,
                                                                 that.assetUrn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iconLibrary, assetUrn);
    }

    @Override
    public String toString() {
        return "AssetByIconLibrary{" +
                "iconLibrary=" + iconLibrary +
                ", assetUrn=" + assetUrn +
                '}';
    }
}
