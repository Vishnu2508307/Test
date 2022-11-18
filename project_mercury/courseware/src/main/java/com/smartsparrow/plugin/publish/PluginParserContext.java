package com.smartsparrow.plugin.publish;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.plugin.data.PluginType;

public class PluginParserContext {

    private UUID pluginId;
    private String hash;
    private UUID publisherId;
    private String configurationSchema;
    private Map<String, File> files;
    private PluginType pluginType;
    private String name;
    private String version;

    public UUID getPluginId() {
        return pluginId;
    }

    public PluginParserContext setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public PluginParserContext setHash(String hash) {
        this.hash = hash;
        return this;
    }

    public UUID getPublisherId() {
        return publisherId;
    }

    public PluginParserContext setPublisherId(UUID publisherId) {
        this.publisherId = publisherId;
        return this;
    }

    public String getConfigurationSchema() {
        return configurationSchema;
    }

    public PluginParserContext setConfigurationSchema(String configurationSchema) {
        this.configurationSchema = configurationSchema;
        return this;
    }

    public Map<String, File> getFiles() {
        return files;
    }

    public PluginParserContext setFiles(Map<String, File> files) {
        this.files = files;
        return this;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    public PluginParserContext setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
        return this;
    }

    public String getName() {
        return name;
    }

    public PluginParserContext setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public PluginParserContext setVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginParserContext that = (PluginParserContext) o;
        return Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(hash, that.hash) &&
                Objects.equals(publisherId, that.publisherId) &&
                Objects.equals(configurationSchema, that.configurationSchema) &&
                Objects.equals(files, that.files) &&
                pluginType == that.pluginType &&
                Objects.equals(name, that.name) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, hash, publisherId, configurationSchema, files, pluginType, name, version);
    }

    @Override
    public String toString() {
        return "PluginParserContext{" +
                "pluginId=" + pluginId +
                ", hash='" + hash + '\'' +
                ", publisherId=" + publisherId +
                ", configurationSchema='" + configurationSchema + '\'' +
                ", files=" + files +
                ", pluginType=" + pluginType +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
