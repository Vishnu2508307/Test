package com.smartsparrow.plugin.publish;


import java.util.Map;
import java.util.UUID;

import static com.smartsparrow.util.Warrants.affirmNotNull;

public class PluginIdField implements PluginField<UUID, Map<String, Object>> {

    private String fieldName;
    private Map<String, Object> jsonObjectMap;

    public PluginIdField(String fieldName, Map<String, Object> jsonObjectMap) {
        this.fieldName = fieldName;
        this.jsonObjectMap = jsonObjectMap;
    }

    @Override
    public UUID parse(PluginParserContext pluginParserContext) {
        if (pluginParserContext.getPluginId() != null) {
            return pluginParserContext.getPluginId();
        } else {
            String pluginId = (String) jsonObjectMap.get(fieldName);
            affirmNotNull(pluginId, "missing required plugin id");
            pluginParserContext.setPluginId(UUID.fromString(pluginId));
            return UUID.fromString(pluginId);
        }
    }
}
