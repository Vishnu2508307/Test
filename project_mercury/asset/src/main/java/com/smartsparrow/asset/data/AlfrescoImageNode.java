package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class AlfrescoImageNode extends AlfrescoNode {
    private Double width;
    private Double height;
    private String source;
    private String mimeType;
    private String altText = "";
    private String longDescription = "";
    private String workURN;

    public Double getWidth() {
        return width;
    }

    public AlfrescoImageNode setWidth(Double width) {
        this.width = width;
        return this;
    }

    public Double getHeight() {
        return height;
    }

    public AlfrescoImageNode setHeight(Double height) {
        this.height = height;
        return this;
    }

    public String getSource() {
        return source;
    }

    public AlfrescoImageNode setSource(String source) {
        this.source = source;
        return this;
    }

    public String getMimeType() {
        return mimeType;
    }

    public AlfrescoImageNode setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public String getAltText() {
        return altText;
    }

    public AlfrescoImageNode setAltText(String altText) {
        this.altText = altText;
        return this;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public AlfrescoImageNode setLongDescription(String longDescription) {
        this.longDescription = longDescription;
        return this;
    }

    public String getWorkURN() {
        return workURN;
    }

    public AlfrescoImageNode setWorkURN(String workURN) {
        this.workURN = workURN;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AlfrescoImageNode that = (AlfrescoImageNode) o;
        return Objects.equals(width, that.width) &&
                Objects.equals(height, that.height) &&
                Objects.equals(source, that.source) &&
                Objects.equals(mimeType, that.mimeType) &&
                Objects.equals(altText, that.altText) &&
                Objects.equals(longDescription, that.longDescription) &&
                Objects.equals(workURN, that.workURN);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), width, height, source, mimeType, altText, longDescription, workURN);
    }

    @Override
    public UUID getAlfrescoId() {
        return super.getAlfrescoId();
    }

    @Override
    public AlfrescoImageNode setAlfrescoId(UUID alfrescoId) {
        super.setAlfrescoId(alfrescoId);
        return this;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public AlfrescoImageNode setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public String getVersion() {
        return super.getVersion();
    }

    @Override
    public AlfrescoImageNode setVersion(String version) {
        super.setVersion(version);
        return this;
    }

    @Override
    public Long getLastModifiedDate() {
        return super.getLastModifiedDate();
    }

    @Override
    public AlfrescoImageNode setLastModifiedDate(Long lastModifiedDate) {
         super.setLastModifiedDate(lastModifiedDate);
         return this;
    }

    @Override
    public String getPath() {
        return super.getPath();
    }

    @Override
    public AlfrescoImageNode setPath(String path) {
        super.setPath(path);
        return this;
    }

    @Override
    public String toString() {
        return "AlfrescoImageAsset{" +
                "width=" + width +
                ", height=" + height +
                ", source='" + source + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", altText='" + altText + '\'' +
                ", longDescription='" + longDescription + '\'' +
                ", workURN='" + workURN + '\'' +
                "} " + super.toString();
    }
}
