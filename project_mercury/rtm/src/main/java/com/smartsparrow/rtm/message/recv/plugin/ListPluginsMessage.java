package com.smartsparrow.rtm.message.recv.plugin;

import java.util.List;
import java.util.Objects;

import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class ListPluginsMessage extends ReceivedMessage {

    private PluginType pluginType;
    private List<PluginFilter> pluginFilters;

    public PluginType getPluginType() {
        return pluginType;
    }

    public List<PluginFilter> getPluginFilters() {
        return pluginFilters;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListPluginsMessage that = (ListPluginsMessage) o;
        return pluginType == that.pluginType &&
                Objects.equals(pluginFilters, that.pluginFilters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginType, pluginFilters);
    }
}
