package com.smartsparrow.rtm.message.recv.plugin;

import java.util.Objects;
import java.util.UUID;

import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.data.PublishMode;
import com.smartsparrow.rtm.message.ReceivedMessage;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UWF_UNWRITTEN_FIELD", justification = "Hydrated via Jackson on message deserialization")
public class CreatePluginMessage extends ReceivedMessage {

    private String name;
    private PluginType pluginType;
    private UUID pluginId;
    private PublishMode publishMode;

    public String getName() {
        return name;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    public UUID getPluginId() {
        return pluginId;
    }

    public PublishMode getPublishMode() {
        if(publishMode == null) {
            return PublishMode.DEFAULT;
        }
        return publishMode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreatePluginMessage that = (CreatePluginMessage) o;
        return Objects.equals(name, that.name) &&
                pluginType == that.pluginType &&
                Objects.equals(pluginId, that.pluginId) &&
                publishMode == that.publishMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, pluginType, pluginId, publishMode);
    }
}
