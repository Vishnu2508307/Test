package com.smartsparrow.plugin.publish;


import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class VersionField implements PluginField<String, Map<String, Object>> {

    private String fieldName;
    private Map<String, Object> jsonObjectMap;

    public VersionField(String fieldName, Map<String, Object> jsonObjectMap) {
        this.fieldName = fieldName;
        this.jsonObjectMap = jsonObjectMap;
    }

    @Override
    public String parse(PluginParserContext pluginParserContext)
    {
        String version = (String) jsonObjectMap.get(fieldName);
        checkArgument(version != null, "version missing from manifest");
        pluginParserContext.setVersion(version);
        return version;
    }
}
