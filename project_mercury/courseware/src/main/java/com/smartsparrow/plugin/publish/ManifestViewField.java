package com.smartsparrow.plugin.publish;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class ManifestViewField implements PluginField<String, Map<String, Object>> {

    private String fieldName;
    private Map<String, Object> jsonObjectMap;

    public ManifestViewField(String fieldName, Map<String, Object> jsonObjectMap) {
        this.fieldName = fieldName;
        this.jsonObjectMap = jsonObjectMap;
    }

    @Override
    public String parse(PluginParserContext pluginParserContext) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object viewsObject = jsonObjectMap.get(fieldName);

        checkArgument(viewsObject != null, "entry point views required");
        return mapper.writeValueAsString(viewsObject);
    }
}
