package com.smartsparrow.rtm.message.recv.plugin;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class GetPluginVersionsMessage extends ReceivedMessage {

    private UUID pluginId;

    public UUID getPluginId() {
        return pluginId;
    }

    public void setPluginId(UUID pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetPluginVersionsMessage that = (GetPluginVersionsMessage) o;
        return Objects.equals(pluginId, that.pluginId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(pluginId);
    }
}
