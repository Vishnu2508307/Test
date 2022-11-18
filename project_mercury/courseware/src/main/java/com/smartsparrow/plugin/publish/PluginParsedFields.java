package com.smartsparrow.plugin.publish;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginManifest;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PluginParsedFields {

    private PluginManifest pluginManifest;
    private Map<String, Object> views;
    private JsonNode searchableFields;
    private List<PluginFilter> pluginFilters;

    public PluginManifest getPluginManifest() {
        return pluginManifest;
    }

    public PluginParsedFields setPluginManifest(PluginManifest pluginManifest) {
        this.pluginManifest = pluginManifest;
        return this;
    }

    public Map<String, Object> getViews() {
        return views;
    }

    public PluginParsedFields setViews(Map<String, Object> views) {
        this.views = views;
        return this;
    }

    public JsonNode getSearchableFields() {
        return searchableFields;
    }

    public PluginParsedFields setSearchableFields(JsonNode searchableFields) {
        this.searchableFields = searchableFields;
        return this;
    }

    public List<PluginFilter> getPluginFilters() {
        return pluginFilters;
    }

    public PluginParsedFields setPluginFilters(final List<PluginFilter> pluginFilters) {
        this.pluginFilters = pluginFilters;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginParsedFields that = (PluginParsedFields) o;
        return Objects.equals(pluginManifest, that.pluginManifest) &&
                Objects.equals(views, that.views) &&
                Objects.equals(searchableFields, that.searchableFields) &&
                Objects.equals(pluginFilters, that.pluginFilters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginManifest, views, searchableFields, pluginFilters);
    }

    @Override
    public String toString() {
        return "PluginParsedFields{" +
                "pluginManifest=" + pluginManifest +
                ", views=" + views +
                ", searchableFields=" + searchableFields +
                ", pluginFilters=" + pluginFilters +
                '}';
    }
}
