package com.smartsparrow.plugin.data;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginSearchableField {

    private UUID pluginId;
    private String version;
    private UUID id;
    private String name;
    private String contentType;
    private Set<String> summary;
    private Set<String> body;
    private Set<String> source;
    private Set<String> preview;
    private Set<String> tag;

    public UUID getPluginId() {
        return pluginId;
    }

    public PluginSearchableField setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public PluginSearchableField setVersion(String version) {
        this.version = version;
        return this;
    }

    public UUID getId() {
        return id;
    }

    public PluginSearchableField setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PluginSearchableField setName(String name) {
        this.name = name;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public PluginSearchableField setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public Set<String> getSummary() {
        return summary;
    }

    public PluginSearchableField setSummary(Set<String> summary) {
        this.summary = summary;
        return this;
    }

    public Set<String> getBody() {
        return body;
    }

    public PluginSearchableField setBody(Set<String> body) {
        this.body = body;
        return this;
    }

    public Set<String> getSource() {
        return source;
    }

    public PluginSearchableField setSource(Set<String> source) {
        this.source = source;
        return this;
    }

    public Set<String> getPreview() {
        return preview;
    }

    public PluginSearchableField setPreview(Set<String> preview) {
        this.preview = preview;
        return this;
    }

    public Set<String> getTag() {
        return tag;
    }

    public PluginSearchableField setTag(Set<String> tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginSearchableField that = (PluginSearchableField) o;
        return Objects.equal(pluginId, that.pluginId) &&
                Objects.equal(version, that.version) &&
                Objects.equal(id, that.id) &&
                Objects.equal(name, that.name) &&
                Objects.equal(contentType, that.contentType) &&
                Objects.equal(summary, that.summary) &&
                Objects.equal(body, that.body) &&
                Objects.equal(source, that.source) &&
                Objects.equal(preview, that.preview) &&
                Objects.equal(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pluginId, version, id, name, contentType, summary, body, source, preview, tag);
    }

    @Override
    public String toString() {
        return "PluginSearchableField{" +
                "pluginId=" + pluginId +
                ", version='" + version + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", contentType='" + contentType + '\'' +
                ", summary=" + summary +
                ", body=" + body +
                ", source=" + source +
                ", preview=" + preview +
                ", tag=" + tag +
                '}';
    }
}
