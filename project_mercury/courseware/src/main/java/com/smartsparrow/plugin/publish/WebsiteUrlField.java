package com.smartsparrow.plugin.publish;


import java.util.Map;

public class WebsiteUrlField implements PluginField<String, Map<String, Object>> {

    private String fieldName;
    private Map<String, Object> jsonObjectMap;

    public WebsiteUrlField(String fieldName, Map<String, Object> jsonObjectMap) {
        this.fieldName = fieldName;
        this.jsonObjectMap = jsonObjectMap;
    }

    @Override
    public String parse(PluginParserContext pluginParserContext) {
        return (String) jsonObjectMap.get(fieldName);
    }
}
