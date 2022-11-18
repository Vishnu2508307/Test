package com.smartsparrow.rtm.message.recv.plugin;

import java.util.UUID;

import com.smartsparrow.plugin.data.PluginLogLevel;
import com.smartsparrow.rtm.message.MessageType;

public interface PluginLogMessage extends MessageType {
    UUID getPluginId();

    String getVersion();

    PluginLogLevel getLevel();

    String getMessage();

    String getArgs();

    String getPluginContext();

    UUID getTransactionId();

    String getTransactionName();

    String getTransactionSequence();

    UUID getSegmentId();

    String getSegmentName();

    UUID getEventId();

}
