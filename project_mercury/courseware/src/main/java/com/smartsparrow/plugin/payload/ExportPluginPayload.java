package com.smartsparrow.plugin.payload;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ExportPluginPayload is utilized in publishing ExportRequestNotification to add plugin summary and repository URL
 */
public class ExportPluginPayload {
    private PluginSummaryPayload pluginSummaryPayload;
    private String pluginRepositoryPath;

    @JsonProperty("summary")
    public PluginSummaryPayload getPluginSummaryPayload() {
        return pluginSummaryPayload;
    }

    public ExportPluginPayload setPluginSummaryPayload(PluginSummaryPayload pluginSummaryPayload) {
        this.pluginSummaryPayload = pluginSummaryPayload;
        return this;
    }

    //returns the uploaded plugin zip location with repository bucket name
    public String getPluginRepositoryPath() {
        return pluginRepositoryPath;
    }

    public ExportPluginPayload setPluginRepositoryPath(final String pluginRepositoryPath) {
        this.pluginRepositoryPath = pluginRepositoryPath;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportPluginPayload plugin = (ExportPluginPayload) o;
        return Objects.equals(pluginSummaryPayload, plugin.pluginSummaryPayload) &&
                Objects.equals(pluginRepositoryPath, plugin.pluginRepositoryPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginSummaryPayload, pluginRepositoryPath);
    }

    @Override
    public String toString() {
        return "PluginPayload{" +
                "pluginSummaryPayload=" + pluginSummaryPayload +
                ", pluginRepositoryPath='" + pluginRepositoryPath + '\'' +
                '}';
    }
}
