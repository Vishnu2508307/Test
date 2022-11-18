package com.smartsparrow.rtm.message.recv.plugin;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class PluginGenericMessage extends ReceivedMessage implements PluginMessage {

    private UUID pluginId;

    @Override
    public UUID getPluginId() {
        return pluginId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PluginGenericMessage that = (PluginGenericMessage) o;
        return Objects.equals(pluginId, that.pluginId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), pluginId);
    }

    @Override
    public String toString() {
        return "PluginAccountListMessage{" +
                "pluginId=" + pluginId +
                "} " + super.toString();
    }
}
