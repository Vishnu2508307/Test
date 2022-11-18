package com.smartsparrow.asset.service;

import com.google.common.base.Objects;

public class AlfrescoNodeInfo {
    private String id;
    private String name;
    private long modifiedAt;
    private String version;
    private String mimeType;
    private double width;
    private double height;
    private String altText;
    private String longDesc;
    private String pathName;
    private String workURN;

    public AlfrescoNodeInfo() {
    }

    public String getId() {
        return id;
    }

    public AlfrescoNodeInfo setId(String uuid) {
        this.id = uuid;
        return this;
    }

    public String getName() {
        return name;
    }

    public AlfrescoNodeInfo setName(String name) {
        this.name = name;
        return this;
    }

    public long getModifiedAt() {
        return modifiedAt;
    }

    public AlfrescoNodeInfo setModifiedAt(long modifiedAt) {
        this.modifiedAt = modifiedAt;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public AlfrescoNodeInfo setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getMimeType() {
        return mimeType;
    }

    public AlfrescoNodeInfo setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public double getWidth() {
        return width;
    }

    public AlfrescoNodeInfo setWidth(double width) {
        this.width = width;
        return this;
    }

    public double getHeight() {
        return height;
    }

    public AlfrescoNodeInfo setHeight(double height) {
        this.height = height;
        return this;
    }

    public String getAltText() {
        return altText;
    }

    public AlfrescoNodeInfo setAltText(String altText) {
        this.altText = altText;
        return this;
    }

    public String getLongDesc() {
        return longDesc;
    }

    public AlfrescoNodeInfo setLongDesc(String longDesc) {
        this.longDesc = longDesc;
        return this;
    }

    public String getPathName() {
        return pathName;
    }

    public AlfrescoNodeInfo setPathName(String pathName) {
        this.pathName = pathName;
        return this;
    }

    public String getWorkURN() {
        return workURN;
    }

    public AlfrescoNodeInfo setWorkURN(String workURN) {
        this.workURN = workURN;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AlfrescoNodeInfo that = (AlfrescoNodeInfo) o;
        return Objects.equal(id, that.id) &&
                Objects.equal(name, that.name) &&
                Objects.equal(modifiedAt, that.modifiedAt) &&
                Objects.equal(version, that.version) &&
                Objects.equal(mimeType, that.mimeType) &&
                Objects.equal(width, that.width) &&
                Objects.equal(height, that.height) &&
                Objects.equal(altText, that.altText) &&
                Objects.equal(longDesc, that.longDesc) &&
                Objects.equal(pathName, that.pathName) &&
                Objects.equal(workURN, that.workURN);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, modifiedAt, version, mimeType, width,
                height, altText, longDesc, pathName, workURN);
    }

    @Override
    public String toString() {
        return "AlfrescoNodeInfo{"
                + "id=" + id
                + ", name=" + name
                + ", modifiedAt=" + modifiedAt
                + ", version=" + version
                + ", mimeType=" + mimeType
                + ", width=" + width
                + ", height=" + height
                + ", altText=" + altText
                + ", longDesc=" + longDesc
                + ", pathName=" + pathName
                + ", workURN=" + workURN
                + '}';
    }
}
