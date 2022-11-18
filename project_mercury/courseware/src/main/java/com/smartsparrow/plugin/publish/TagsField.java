package com.smartsparrow.plugin.publish;


import java.util.List;
import java.util.Map;

public class TagsField implements PluginField<List<String>, Map<String, Object>> {

    private String fieldName;
    private Map<String, Object> jsonObjectMap;

    public TagsField(String fieldName, Map<String, Object> jsonObjectMap) {
        this.fieldName = fieldName;
        this.jsonObjectMap = jsonObjectMap;
    }

    @Override
    public List<String> parse(PluginParserContext pluginParserContext) {
        return (List<String>) jsonObjectMap.get(fieldName);
    }
}
