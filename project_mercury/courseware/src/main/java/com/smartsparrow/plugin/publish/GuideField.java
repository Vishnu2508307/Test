package com.smartsparrow.plugin.publish;


import java.io.File;
import java.util.Map;

public class GuideField implements PluginField<String, Map<String, Object>> {

    private String fieldName;
    private Map<String, Object> jsonObjectMap;


    public GuideField(String fieldName, Map<String, Object> jsonObjectMap) {
        this.fieldName = fieldName;
        this.jsonObjectMap = jsonObjectMap;
    }

    @Override
    public String parse(PluginParserContext pluginParserContext) {
        String guideField = (String) jsonObjectMap.get(fieldName);
        if (guideField != null) {
            validateExist(guideField, pluginParserContext.getFiles());
        }
        return guideField;
    }

    /*
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
