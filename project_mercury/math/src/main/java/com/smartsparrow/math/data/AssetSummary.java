package com.smartsparrow.math.data;

import java.util.Objects;
import java.util.UUID;

public class AssetSummary {

    private UUID id;
    private String altText;
    private String hash;
    private String mathML;
    private String svgText;
    private String svgShape;

    public UUID getId() {
        return id;
    }

    public AssetSummary setId(final UUID id) {
        this.id = id;
        return this;
    }

    public String getAltText() {
        return altText;
    }

    public AssetSummary setAltText(final String altText) {
        this.altText = altText;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public AssetSummary setHash(final String hash) {
        this.hash = hash;
        return this;
    }

    public String getMathML() {
        return mathML;
    }

    public AssetSummary setMathML(final String mathML) {
        this.mathML = mathML;
        return this;
    }

    public String getSvgText() {
        return svgText;
    }

    public AssetSummary setSvgText(final String svgText) {
        this.svgText = svgText;
        return this;
    }

    public String getSvgShape() {
        return svgShape;
    }

    public AssetSummary setSvgShape(final String svgShape) {
        this.svgShape = svgShape;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetSummary that = (AssetSummary) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(altText, that.altText) &&
                Objects.equals(hash, that.hash) &&
                Objects.equals(mathML, that.mathML) &&
                Objects.equals(svgText, that.svgText) &&
                Objects.equals(svgShape, that.svgShape);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, altText, hash, mathML, svgText, svgShape);
    }

    @Override
    public String toString() {
        return "AssetSummary{" +
                "id=" + id +
                ", altText='" + altText + '\'' +
                ", hash='" + hash + '\'' +
                ", mathML='" + mathML + '\'' +
                ", svgText='" + svgText + '\'' +
                ", svgShape='" + svgShape + '\'' +
                '}';
    }
}
