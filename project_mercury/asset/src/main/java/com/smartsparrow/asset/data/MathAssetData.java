package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class MathAssetData {

    private UUID id;
    private String altText;
    private String hash;
    private String mathML;
    private String svgText;
    private String svgShape;

    public UUID getId() {
        return id;
    }

    public MathAssetData setId(final UUID id) {
        this.id = id;
        return this;
    }

    public String getAltText() {
        return altText;
    }

    public MathAssetData setAltText(final String altText) {
        this.altText = altText;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public MathAssetData setHash(final String hash) {
        this.hash = hash;
        return this;
    }

    public String getMathML() {
        return mathML;
    }

    public MathAssetData setMathML(final String mathML) {
        this.mathML = mathML;
        return this;
    }

    public String getSvgText() {
        return svgText;
    }

    public MathAssetData setSvgText(final String svgText) {
        this.svgText = svgText;
        return this;
    }

    public String getSvgShape() {
        return svgShape;
    }

    public MathAssetData setSvgShape(final String svgShape) {
        this.svgShape = svgShape;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MathAssetData that = (MathAssetData) o;
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
        return "MathAssetData{" +
                "id=" + id +
                ", altText='" + altText + '\'' +
                ", hash='" + hash + '\'' +
                ", mathML='" + mathML + '\'' +
                ", svgText='" + svgText + '\'' +
                ", svgShape='" + svgShape + '\'' +
                '}';
    }
}
