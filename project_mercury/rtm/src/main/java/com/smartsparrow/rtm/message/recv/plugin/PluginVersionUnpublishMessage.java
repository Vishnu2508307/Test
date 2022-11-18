package com.smartsparrow.rtm.message.recv.plugin;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class PluginVersionUnpublishMessage extends ReceivedMessage implements PluginMessage {
    UUID pluginId;
    Integer major;
    Integer minor;
    Integer patch;

    @Override
    public UUID getPluginId() {
        return pluginId;
    }

    public Integer getMajor() {
        return major;
    }

    public Integer getMinor() {
        return minor;
    }

    public Integer getPatch() {
        return patch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginVersionUnpublishMessage that = (PluginVersionUnpublishMessage) o;
        return Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(major, that.major) &&
                Objects.equals(minor, that.minor) &&
                Objects.equals(patch, that.patch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, major, minor, patch);
    }

    @Override
    public String toString() {
        return "PluginVersionUnpublishMessage{" +
                "pluginId=" + pluginId +
                ", major=" + major +
                ", minor=" + minor +
                ", patch=" + patch +
                "} " + super.toString();
    }
}
