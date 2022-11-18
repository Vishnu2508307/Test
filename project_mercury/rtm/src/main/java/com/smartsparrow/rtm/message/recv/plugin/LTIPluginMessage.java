package com.smartsparrow.rtm.message.recv.plugin;

import com.smartsparrow.rtm.message.ReceivedMessage;

import java.util.Objects;
import java.util.UUID;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class LTIPluginMessage extends ReceivedMessage {

    private UUID pluginId;
    private String key;

    public UUID getPluginId() {
        return pluginId;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTIPluginMessage that = (LTIPluginMessage) o;
        return Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, key);
    }

    @Override
    public String toString() {
        return "LTIPluginMessage{" +
                "pluginId=" + pluginId +
                ", key='" + key + '\'' +
                '}';
    }
}
