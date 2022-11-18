package com.smartsparrow.plugin.publish;

import org.json.JSONObject;

import java.util.Map;

public class NameField implements PluginField<String, Map<String, Object>> {

    private final String fieldName;
    private final Map<String, Object> jsonObjectMap;

    public NameField(String fieldName, Map<String, Object> jsonObjectMap) {
        this.fieldName = fieldName;
        this.jsonObjectMap = jsonObjectMap;
    }

    @Override
    public String parse(PluginParserContext pluginParserContext) {
        pluginParserContext.setName((String)jsonObjectMap.get(fieldName));
        return (String)jsonObjectMap.get(fieldName);
    }
}
