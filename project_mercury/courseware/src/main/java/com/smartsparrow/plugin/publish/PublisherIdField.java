package com.smartsparrow.plugin.publish;


import java.util.Map;
import java.util.UUID;

public class PublisherIdField implements PluginField<UUID, Map<String, Object>> {

    @Override
    public UUID parse(PluginParserContext pluginParserContext) {
        return pluginParserContext.getPublisherId();
    }
}
