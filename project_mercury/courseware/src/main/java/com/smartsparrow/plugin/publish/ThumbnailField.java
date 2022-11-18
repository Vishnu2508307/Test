package com.smartsparrow.plugin.publish;

import java.io.File;
import java.util.Map;

public class ThumbnailField implements PluginField<String, Map<String, Object>> {

    private String fieldName;
    private Map<String, Object> jsonObjectMap;

    public ThumbnailField(String fieldName, Map<String, Object> jsonObjectMap) {
        this.fieldName = fieldName;
        this.jsonObjectMap = jsonObjectMap;
    }

    @Override
    public String parse(PluginParserContext pluginParserContext) {
        String thumbnail = (String) jsonObjectMap.get(fieldName);
        validateExist(thumbnail, pluginParserContext.getFiles());
        return thumbnail;
    }

    /**
     * Validate that the file exists
     *
     * @param filePath the file path to validate
     * @param files    the unzipped file map
     * @throws IllegalArgumentException when the file is not found inside the map
     */
    private void validateExist(String filePath, Map<String, File> files) {
        if (filePath != null) {
            if (!files.containsKey(filePath)) {
                throw new IllegalArgumentException("File " + filePath + " not found inside the package");
            }
        }
    }
}
