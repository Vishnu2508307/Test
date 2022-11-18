package com.smartsparrow.plugin.service;

import java.util.List;
import java.util.Objects;

import com.smartsparrow.plugin.data.ManifestView;
import com.smartsparrow.plugin.data.PluginManifest;

public class PublishedPlugin {

    private PluginManifest pluginManifest;
    private List<ManifestView> manifestView;

    public PluginManifest getPluginManifest() {
        return pluginManifest;
    }

    public PublishedPlugin setPluginManifest(PluginManifest pluginManifest) {
        this.pluginManifest = pluginManifest;
        return this;
    }

    public List<ManifestView> getManifestView() {
        return manifestView;
    }

    public PublishedPlugin setManifestView(List<ManifestView> manifestView) {
        this.manifestView = manifestView;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublishedPlugin that = (PublishedPlugin) o;
        return Objects.equals(pluginManifest, that.pluginManifest) &&
                Objects.equals(manifestView, that.manifestView);
    }

    @Override
    public int hashCode() {

        return Objects.hash(pluginManifest, manifestView);
    }
}
