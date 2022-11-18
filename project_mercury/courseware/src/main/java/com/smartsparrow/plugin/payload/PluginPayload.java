package com.smartsparrow.plugin.payload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.plugin.data.ManifestView;
import com.smartsparrow.plugin.data.PluginManifest;

public class PluginPayload {
    private PluginSummaryPayload pluginSummaryPayload;
    private PluginManifest manifest;
    private List<ManifestView> entryPoints = new ArrayList<>();
    private String pluginRepositoryPath;

    @JsonProperty("summary")
    public PluginSummaryPayload getPluginSummaryPayload() {
        return pluginSummaryPayload;
    }

    public PluginPayload setPluginSummaryPayload(PluginSummaryPayload pluginSummaryPayload) {
        this.pluginSummaryPayload = pluginSummaryPayload;
        return this;
    }

    public PluginManifest getManifest() {
        return manifest;
    }

    public PluginPayload setManifest(PluginManifest manifest) {
        this.manifest = manifest;
        return this;
    }

    public List<ManifestView> getEntryPoints() {
        return entryPoints;
    }

    public PluginPayload setEntryPoints(List<ManifestView> entryPoints) {
        this.entryPoints = entryPoints;
        return this;
    }

    public PluginPayload addEntryPoints(ManifestView... entryPoint) {
        this.entryPoints.addAll(Arrays.asList(entryPoint));
        return this;
    }

    //returns the uploaded plugin zip location with repository bucket name
    public String getPluginRepositoryPath() {
        return pluginRepositoryPath;
    }

    public PluginPayload setPluginRepositoryPath(final String pluginRepositoryPath) {
        this.pluginRepositoryPath = pluginRepositoryPath;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginPayload plugin = (PluginPayload) o;
        return Objects.equals(pluginSummaryPayload, plugin.pluginSummaryPayload) &&
                Objects.equals(manifest, plugin.manifest) &&
                Objects.equals(entryPoints, plugin.entryPoints) &&
                Objects.equals(pluginRepositoryPath, plugin.pluginRepositoryPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginSummaryPayload, manifest, entryPoints, pluginRepositoryPath);
    }

    @Override
    public String toString() {
        return "PluginPayload{" +
                "pluginSummaryPayload=" + pluginSummaryPayload +
                ", manifest=" + manifest +
                ", entryPoints=" + entryPoints +
                ", pluginRepositoryPath='" + pluginRepositoryPath + '\'' +
                '}';
    }
}
