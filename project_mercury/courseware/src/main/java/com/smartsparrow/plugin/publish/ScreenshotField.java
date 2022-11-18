package com.smartsparrow.plugin.publish;


import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScreenshotField implements PluginField<Set, Map<String, Object>> {

    private String fieldName;
    private Map<String, Object> jsonObjectMap;

    public ScreenshotField(String fieldName, Map<String, Object> jsonObjectMap) {
        this.fieldName = fieldName;
        this.jsonObjectMap = jsonObjectMap;
    }

    @Override
    public Set<String> parse(PluginParserContext pluginParserContext) {
        Object screenShots = jsonObjectMap.get(fieldName);
        return screenShots != null ? getScreenShots(pluginParserContext, (ArrayList) screenShots) : new HashSet<>();
    }

    private Set<String> getScreenShots(PluginParserContext pluginParserContext, ArrayList screenShotList) {
        Set<String> screenshots = (new HashSet<>(screenShotList));
        if (!screenshots.isEmpty()) {
            screenshots.forEach(screenshot -> validateExist(screenshot, pluginParserContext.getFiles()));
            return screenshots;
        } else {
            return new HashSet<>();
        }
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
