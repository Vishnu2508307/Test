package com.smartsparrow.plugin.payload;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginType;

/**
 * This payload can be used inside others payload (ex. activity) when there is need to include a few information about plugin (not the whole summary)
 */
public class PluginRefPayload {

    private UUID pluginId;
    private String name;
    private PluginType type;
    @JsonProperty("version")
    private String versionExpr;
    private List<PluginFilter> pluginFilters;

    public static PluginRefPayload from(@Nonnull PluginSummary plugin, String versionExpr, List<PluginFilter> pluginFilters) {
        PluginRefPayload result = new PluginRefPayload();
        result.pluginId = plugin.getId();
        result.name = plugin.getName();
        result.type = plugin.getType();
        result.versionExpr = versionExpr;
        result.pluginFilters = pluginFilters;
        return result;
    }

    public UUID getPluginId() {
        return pluginId;
    }

    public String getName() {
        return name;
    }

    public PluginType getType() {
        return type;
    }

    public String getVersionExpr() {
        return versionExpr;
    }

    public List<PluginFilter> getPluginFilters() {
        return pluginFilters;
    }
}
