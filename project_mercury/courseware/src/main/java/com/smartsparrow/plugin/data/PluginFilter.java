package com.smartsparrow.plugin.data;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PluginFilter {

    private UUID pluginId;
    private String version;
    private PluginFilterType filterType;
    private Set<String> filterValues;

    public UUID getPluginId() {
        return pluginId;
    }

    public PluginFilter setPluginId(final UUID pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public PluginFilter setVersion(final String version) {
        this.version = version;
        return this;
    }

    public PluginFilterType getFilterType() {
        return filterType;
    }

    public PluginFilter setFilterType(final PluginFilterType filterType) {
        this.filterType = filterType;
        return this;
    }

    public Set<String> getFilterValues() {
        return filterValues;
    }

    public PluginFilter setFilterValues(final Set<String> filterValues) {
        this.filterValues = filterValues;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginFilter that = (PluginFilter) o;
        return Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(version, that.version) &&
                Objects.equals(filterType, that.filterType) &&
                Objects.equals(filterValues, that.filterValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, version, filterType, filterValues);
    }

    @Override
    public String toString() {
        return "PluginFilter{" +
                "pluginId=" + pluginId +
                ", version='" + version + '\'' +
                ", filterType='" + filterType + '\'' +
                ", filterValues=" + filterValues +
                '}';
    }
}
