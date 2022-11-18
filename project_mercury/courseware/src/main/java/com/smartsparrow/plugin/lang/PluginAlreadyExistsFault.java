package com.smartsparrow.plugin.lang;

import java.util.UUID;

import com.smartsparrow.exception.ConflictFault;

public class PluginAlreadyExistsFault extends ConflictFault {

    public PluginAlreadyExistsFault(UUID pluginId) {
        super(String.format("Plugin id %s already exists", pluginId));
    }

}
