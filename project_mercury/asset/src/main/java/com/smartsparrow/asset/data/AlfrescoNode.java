package com.smartsparrow.asset.data;

import java.util.Objects;
import java.util.UUID;

public class AlfrescoNode {
    private UUID alfrescoId;
    private String name;
    private String version;
    private Long lastModifiedDate;
    private String path;

    public UUID getAlfrescoId() {
        return alfrescoId;
    }

    public AlfrescoNode setAlfrescoId(UUID alfrescoId) {
        this.alfrescoId = alfrescoId;
        return this;
    }

    public String getName() {
        return name;
    }

    public AlfrescoNode setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public AlfrescoNode setVersion(String version) {
        this.version = version;
        return this;
    }

    public Long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public AlfrescoNode setLastModifiedDate(Long lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }

    public String getPath() {
        return path;
    }

    public AlfrescoNode setPath(String path) {
        this.path = path;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlfrescoNode that = (AlfrescoNode) o;
        return Objects.equals(alfrescoId, that.alfrescoId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(version, that.version) &&
                Objects.equals(lastModifiedDate, that.lastModifiedDate) &&
                Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alfrescoId, name, version, lastModifiedDate, path);
    }

    @Override
    public String toString() {
        return "AlfrescoNode{" +
                "alfrescoId=" + alfrescoId +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", lastModifiedDate=" + lastModifiedDate +
                ", path='" + path + '\'' +
                '}';
    }
}
