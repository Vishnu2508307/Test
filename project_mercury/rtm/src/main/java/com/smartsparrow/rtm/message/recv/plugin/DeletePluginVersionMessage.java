package com.smartsparrow.rtm.message.recv.plugin;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class DeletePluginVersionMessage extends ReceivedMessage implements PluginMessage {
    private UUID pluginId;
    private String version;

    @Override
    public UUID getPluginId() {
        return pluginId;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeletePluginVersionMessage that = (DeletePluginVersionMessage) o;
        return Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, version);
    }

    @Override
    public String toString() {
        return "DeletePluginVersionMessage{" +
                "pluginId=" + pluginId +
                ", version='" + version + '\'' +
                "} " + super.toString();
    }
}
