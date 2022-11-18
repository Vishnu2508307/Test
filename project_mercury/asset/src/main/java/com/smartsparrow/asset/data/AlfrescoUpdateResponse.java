package com.smartsparrow.asset.data;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.exception.IllegalArgumentFault;

public class AlfrescoUpdateResponse {

    public static final String MIME_TYPE_FIELD = "mimeType";
    public static final String WIDTH_FIELD = "exif:pixelXDimension";
    public static final String HEIGHT_FIELD = "exif:pixelYDimension";
    public static final String ALT_TEXT_FIELD = "cplg:altText";
    public static final String LONG_DESC_FIELD = "cplg:longDescription";
    public static final String VERSION_FIELD = "cm:versionLabel";
    public static final String WORK_URN_FIELD = "cp:workURN";
    public static final String ENTRY_FIELD = "entry";

    @JsonProperty("id")
    private UUID alfrescoId;
    @JsonProperty("isFile")
    private boolean isFile;
    private Map<String, Object> createdByUser;
    private String modifiedAt;
    private String nodeType;
    private Map<String, Object> content;
    private Map<String, Object> path;
    private UUID parentId;
    private List<String> aspectNames;
    private String createdAt;
    @JsonProperty("isFolder")
    private boolean isFolder;
    private Map<String, Object> modifiedByUser;
    private String name;
    private Map<String, Object> properties;

    public UUID getAlfrescoId() {
        return alfrescoId;
    }

    public AlfrescoUpdateResponse setAlfrescoId(UUID alfrescoId) {
        this.alfrescoId = alfrescoId;
        return this;
    }

    public boolean isFile() {
        return isFile;
    }

    public AlfrescoUpdateResponse setFile(boolean file) {
        isFile = file;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getCreatedByUser() {
        return createdByUser;
    }

    @JsonAnySetter
    public AlfrescoUpdateResponse setCreatedByUser(Map<String, Object> createdByUser) {
        this.createdByUser = createdByUser;
        return this;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public AlfrescoUpdateResponse setModifiedAt(String modifiedAt) {
        this.modifiedAt = modifiedAt;
        return this;
    }

    public String getNodeType() {
        return nodeType;
    }

    public AlfrescoUpdateResponse setNodeType(String nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getContent() {
        return content;
    }

    @JsonAnySetter
    public AlfrescoUpdateResponse setContent(Map<String, Object> content) {
        this.content = content;
        return this;
    }

    public UUID getParentId() {
        return parentId;
    }

    public AlfrescoUpdateResponse setParentId(UUID parentId) {
        this.parentId = parentId;
        return this;
    }

    public List<String> getAspectNames() {
        return aspectNames;
    }

    public AlfrescoUpdateResponse setAspectNames(List<String> aspectNames) {
        this.aspectNames = aspectNames;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public AlfrescoUpdateResponse setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public AlfrescoUpdateResponse setFolder(boolean folder) {
        isFolder = folder;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getModifiedByUser() {
        return modifiedByUser;
    }

    @JsonAnyGetter
    public AlfrescoUpdateResponse setModifiedByUser(Map<String, Object> modifiedByUser) {
        this.modifiedByUser = modifiedByUser;
        return this;
    }

    public String getName() {
        return name;
    }

    public AlfrescoUpdateResponse setName(String name) {
        this.name = name;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return properties;
    }

    @JsonAnyGetter
    public AlfrescoUpdateResponse setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getPath() {
        return path;
    }

    @JsonAnyGetter
    public AlfrescoUpdateResponse setPath(Map<String, Object> path) {
        this.path = path;
        return this;
    }

    /**
     * Get the mimeType from the response content
     *
     * @return the mimeType
     * @throws IllegalArgumentFault when the content does not contain the mime type field
     */
    public String getMimeType() {
        if (content.containsKey(MIME_TYPE_FIELD)) {
            return content.get(MIME_TYPE_FIELD).toString();
        }
        throw new IllegalArgumentFault(String.format("%s field is missing from content", MIME_TYPE_FIELD));
    }

    /**
     * Get a field value from the response properties
     *
     * @param fieldName the field to look for in the properties
     * @param clazz the class type to cast the field value to
     * @param <T> the value type
     * @return the field value
     * @throws IllegalArgumentFault when the field is not found
     * @throws ClassCastException when failing to cast the field value
     */
    public <T> T getProperty(String fieldName, Class<T> clazz) {
        if (properties.containsKey(fieldName)) {
            return clazz.cast(properties.get(fieldName));
        }

        throw new IllegalArgumentFault(String.format("%s field missing from properties", fieldName));
    }

    /**
     * Get a field value from the response properties or returns a default value when not found
     *
     * @param fieldName the field to look for in the properties
     * @param clazz the class type to cast the field value to
     * @param defaultValue the default value to return when the field is not found
     * @param <T> the value type
     * @return the field value
     * @throws ClassCastException when failing to cast the field value
     */
    public <T> T getOrDefaultProperty(String fieldName, Class<T> clazz, T defaultValue) {
        try {
            return getProperty(fieldName, clazz);
        } catch (IllegalArgumentFault f) {
            return defaultValue;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlfrescoUpdateResponse that = (AlfrescoUpdateResponse) o;
        return isFile == that.isFile &&
                isFolder == that.isFolder &&
                Objects.equals(alfrescoId, that.alfrescoId) &&
                Objects.equals(createdByUser, that.createdByUser) &&
                Objects.equals(modifiedAt, that.modifiedAt) &&
                Objects.equals(nodeType, that.nodeType) &&
                Objects.equals(content, that.content) &&
                Objects.equals(path, that.path) &&
                Objects.equals(parentId, that.parentId) &&
                Objects.equals(aspectNames, that.aspectNames) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(modifiedByUser, that.modifiedByUser) &&
                Objects.equals(name, that.name) &&
                Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alfrescoId, isFile, createdByUser, modifiedAt, nodeType, content, path, parentId,
                aspectNames, createdAt, isFolder, modifiedByUser, name, properties);
    }
}
