package com.smartsparrow.plugin.data;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ManifestView {

    private UUID pluginId;
    private String version;
    private String context;
    private String entryPointPath;
    private String entryPointData;
    private String contentType;
    private String publicDir;
    private String editorMode;

    public UUID getPluginId() {
        return pluginId;
    }

    public ManifestView setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public ManifestView setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getContext() {
        return context;
    }

    public ManifestView setContext(String context) {
        this.context = context;
        return this;
    }

    public String getEntryPointPath() {
        return entryPointPath;
    }

    @JsonProperty("entryPoint")
    public ManifestView setEntryPointPath(String entryPointPath) {
        this.entryPointPath = entryPointPath;
        return this;
    }

    public String getEntryPointData() {
        return entryPointData;
    }

    public ManifestView setEntryPointData(String entryPointData) {
        this.entryPointData = entryPointData;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public ManifestView setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getPublicDir() {
        return publicDir;
    }

    public ManifestView setPublicDir(String publicDir) {
        this.publicDir = publicDir;
        return this;
    }

    public String getEditorMode() {
        return editorMode;
    }

    public ManifestView setEditorMode(String editorMode) {
        this.editorMode = editorMode;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManifestView that = (ManifestView) o;
        return Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(version, that.version) &&
                Objects.equals(context, that.context) &&
                Objects.equals(entryPointPath, that.entryPointPath) &&
                Objects.equals(entryPointData, that.entryPointData) &&
                Objects.equals(contentType, that.contentType) &&
                Objects.equals(publicDir, that.publicDir) &&
                Objects.equals(editorMode, that.editorMode);
    }

    @Override
    public int hashCode() {

        return Objects.hash(pluginId, version, context, entryPointPath, entryPointData, contentType, publicDir, editorMode);
    }

    @Override
    public String toString() {
        return "ManifestView{" +
                "pluginId=" + pluginId +
                ", version='" + version + '\'' +
                ", context='" + context + '\'' +
                ", entryPointPath='" + entryPointPath + '\'' +
                ", entryPointData='" + entryPointData + '\'' +
                ", contentType='" + contentType + '\'' +
                ", publicDir='" + publicDir + '\'' +
                ", editorMode='" + editorMode + '\'' +
                '}';
    }
}
