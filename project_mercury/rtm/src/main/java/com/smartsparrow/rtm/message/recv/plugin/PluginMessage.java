package com.smartsparrow.rtm.message.recv.plugin;

import java.util.UUID;

import com.smartsparrow.rtm.message.MessageType;

public interface PluginMessage extends MessageType {

    UUID getPluginId();

}
