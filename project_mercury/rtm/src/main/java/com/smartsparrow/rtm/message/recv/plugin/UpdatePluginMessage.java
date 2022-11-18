package com.smartsparrow.rtm.message.recv.plugin;

import java.util.Objects;
import java.util.UUID;
import com.smartsparrow.plugin.data.PublishMode;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class UpdatePluginMessage extends ReceivedMessage implements PluginMessage {

    private UUID pluginId;
    private PublishMode publishMode;

    @Override
    public UUID getPluginId() {
        return pluginId;
    }

    public PublishMode getPublishMode() {
        return publishMode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdatePluginMessage that = (UpdatePluginMessage) o;
        return Objects.equals(pluginId, that.pluginId) &&
                publishMode == that.publishMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, publishMode);
    }
}
