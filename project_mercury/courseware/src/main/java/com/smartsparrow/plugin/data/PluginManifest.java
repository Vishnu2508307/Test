package com.smartsparrow.plugin.data;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.plugin.lang.PluginManifestNotFoundFault;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;

import io.leangen.graphql.annotations.GraphQLIgnore;
import reactor.core.publisher.Mono;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PluginManifest {

    private UUID pluginId;
    private String version;
    private String name;
    private String description;
    private PluginType type;
    private Set<String> screenshots;
    private String thumbnail;
    private UUID publisherId;
    private String configurationSchema;
    private String zipHash;
    private String whatsNew;
    private String websiteUrl;
    private String supportUrl;
    private List<String> tags;
    private String outputSchema;
    private String guide;
    private String defaultHeight;

    public UUID getPluginId() {
        return pluginId;
    }

    @JsonProperty("id")
    public PluginManifest setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public PluginManifest setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PluginManifest setDescription(String description) {
        this.description = description;
        return this;
    }

    public Set<String> getScreenshots() {
        return screenshots;
    }

    public PluginManifest setScreenshots(Set<String> screenshots) {
        this.screenshots = screenshots;
        return this;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public PluginManifest setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public UUID getPublisherId() {
        return publisherId;
    }

    public PluginManifest setPublisherId(UUID publisherId) {
        this.publisherId = publisherId;
        return this;
    }

    public String getConfigurationSchema() {
        return configurationSchema;
    }

    public PluginManifest setConfigurationSchema(String configurationSchema) {
        this.configurationSchema = configurationSchema;
        return this;
    }

    @GraphQLIgnore
    public String getZipHash() {
        return zipHash;
    }

    public PluginManifest setZipHash(String zipHash) {
        this.zipHash = zipHash;
        return this;
    }

    public String getName() {
        return name;
    }

    public PluginManifest setName(String name) {
        this.name = name;
        return this;
    }

    public PluginType getType() {
        return type;
    }

    public PluginManifest setType(PluginType type) {
        this.type = type;
        return this;
    }

    public String getWhatsNew() {
        return whatsNew;
    }

    public PluginManifest setWhatsNew(String whatsNew) {
        this.whatsNew = whatsNew;
        return this;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public PluginManifest setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
        return this;
    }

    public String getSupportUrl() {
        return supportUrl;
    }

    public PluginManifest setSupportUrl(String supportUrl) {
        this.supportUrl = supportUrl;
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public PluginManifest setTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    @GraphQLIgnore
    @JsonIgnore
    public String getOutputSchema() {
        return outputSchema;
    }

    public PluginManifest setOutputSchema(String outputSchema) {
        this.outputSchema = outputSchema;
        return this;
    }

    @Nullable
    public String getGuide() {
        return guide;
    }

    public PluginManifest setGuide(String guide) {
        this.guide = guide;
        return this;
    }

    @Nullable
    public String getDefaultHeight() {
        return defaultHeight;
    }

    public PluginManifest setDefaultHeight(final String defaultHeight) {
        this.defaultHeight = defaultHeight;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginManifest that = (PluginManifest) o;
        return Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(version, that.version) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description) &&
                type == that.type &&
                Objects.equals(screenshots, that.screenshots) &&
                Objects.equals(thumbnail, that.thumbnail) &&
                Objects.equals(publisherId, that.publisherId) &&
                Objects.equals(configurationSchema, that.configurationSchema) &&
                Objects.equals(zipHash, that.zipHash) &&
                Objects.equals(whatsNew, that.whatsNew) &&
                Objects.equals(websiteUrl, that.websiteUrl) &&
                Objects.equals(supportUrl, that.supportUrl) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(outputSchema, that.outputSchema) &&
                Objects.equals(guide, that.guide) &&
                Objects.equals(defaultHeight, that.defaultHeight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, version, name, description, type, screenshots, thumbnail, publisherId,
                configurationSchema, zipHash, whatsNew, websiteUrl, supportUrl, tags, outputSchema, guide, defaultHeight);
    }

    @Override
    public String toString() {
        return "PluginManifest{" +
                "pluginId=" + pluginId +
                ", version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type=" + type +
                ", screenshots=" + screenshots +
                ", thumbnail='" + thumbnail + '\'' +
                ", publisherId=" + publisherId +
                ", configurationSchema='" + configurationSchema + '\'' +
                ", zipHash='" + zipHash + '\'' +
                ", whatsNew='" + whatsNew + '\'' +
                ", websiteUrl='" + websiteUrl + '\'' +
                ", supportUrl='" + supportUrl + '\'' +
                ", tags=" + tags +
                ", outputSchema='" + outputSchema + '\'' +
                ", guide='" + guide + '\'' +
                ", defaultHeight='" + defaultHeight + '\'' +
                '}';
    }

    public String getBuildZipPath(PluginManifest manifest) {
        return String.format("%s/%s/%s.zip", manifest.getPluginId(), manifest.getVersion(), manifest.getZipHash());
    }

    public String getBuildFilePath(PluginManifest manifest, String fileName) {
        return String.format("%s/%s/%s", manifest.getPluginId(), manifest.getZipHash(), fileName);
    }

    /**
     * Builds the plugin repository path url for a plugin
     *
     * @param repositoryPublicUrl repository public url
     * @return plugin path String
     */
    public String buildPluginRepositoryUrl(final String repositoryPublicUrl) {
            return String.format("%s/%s/%s/%s.zip", repositoryPublicUrl, pluginId, version, zipHash);
    }
}
