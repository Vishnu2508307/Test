package com.smartsparrow.rtm.message.recv.plugin;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class SyncPluginMessage extends ReceivedMessage {

    UUID pluginId;

    String hash;

    public UUID getPluginId() {
        return pluginId;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SyncPluginMessage that = (SyncPluginMessage) o;
        return Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, hash);
    }

    @Override
    public String toString() {
        return "SyncPluginMessage{" +
                "pluginId=" + pluginId +
                ", hash='" + hash + '\'' +
                '}';
    }
}
