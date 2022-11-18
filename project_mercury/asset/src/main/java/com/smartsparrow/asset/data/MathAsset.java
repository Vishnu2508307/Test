package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MathAsset implements Asset {

    private UUID id;
    private static final AssetProvider assetProvider = AssetProvider.MATH;
    private AssetMediaType assetMediaType;
    private UUID ownerId;
    private UUID subscriptionId;
    private AssetVisibility assetVisibility;

    private String altText;
    private String hash;
    private String mathML;
    private String svgText;
    private String svgShape;

    @Override
    public UUID getId() {
        return id;
    }

    @JsonIgnore
    @Override
    public AssetProvider getAssetProvider() {
        return assetProvider;
    }

    @Override
    public AssetMediaType getAssetMediaType() {
        return assetMediaType;
    }

    public MathAsset setId(UUID id) {
        this.id = id;
        return this;
    }

    public MathAsset setAssetMediaType(AssetMediaType assetMediaType) {
        this.assetMediaType = assetMediaType;
        return this;
    }

    @Override
    public UUID getOwnerId() {
        return ownerId;
    }

    public MathAsset setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    @Override
    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public MathAsset setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public MathAsset setHash(String hash) {
        this.hash = hash;
        return this;
    }

    public String getAltText() {
        return altText;
    }

    public MathAsset setAltText(final String altText) {
        this.altText = altText;
        return this;
    }

    public String getMathML() {
        return mathML;
    }

    public MathAsset setMathML(final String mathML) {
        this.mathML = mathML;
        return this;
    }

    public String getSvgText() {
        return svgText;
    }

    public MathAsset setSvgText(final String svgText) {
        this.svgText = svgText;
        return this;
    }

    public String getSvgShape() {
        return svgShape;
    }

    public MathAsset setSvgShape(final String svgShape) {
        this.svgShape = svgShape;
        return this;
    }

    @Override
    public AssetVisibility getAssetVisibility() {
        return assetVisibility;
    }

    public MathAsset setAssetVisibility(AssetVisibility assetVisibility) {
        this.assetVisibility = assetVisibility;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MathAsset mathAsset = (MathAsset) o;
        return Objects.equals(id, mathAsset.id) &&
                assetMediaType == mathAsset.assetMediaType &&
                Objects.equals(ownerId, mathAsset.ownerId) &&
                Objects.equals(subscriptionId, mathAsset.subscriptionId) &&
                assetVisibility == mathAsset.assetVisibility &&
                Objects.equals(altText, mathAsset.altText) &&
                Objects.equals(hash, mathAsset.hash) &&
                Objects.equals(mathML, mathAsset.mathML) &&
                Objects.equals(svgText, mathAsset.svgText) &&
                Objects.equals(svgShape, mathAsset.svgShape);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,
                            assetMediaType,
                            ownerId,
                            subscriptionId,
                            assetVisibility,
                            altText,
                            hash,
                            mathML,
                            svgText,
                            svgShape);
    }

    @Override
    public String toString() {
        return "MathAsset{" +
                "id=" + id +
                ", assetMediaType=" + assetMediaType +
                ", ownerId=" + ownerId +
                ", subscriptionId=" + subscriptionId +
                ", assetVisibility=" + assetVisibility +
                ", altText='" + altText + '\'' +
                ", hash='" + hash + '\'' +
                ", mathML='" + mathML + '\'' +
                ", svgText='" + svgText + '\'' +
                ", svgShape='" + svgShape + '\'' +
                '}';
    }
}
