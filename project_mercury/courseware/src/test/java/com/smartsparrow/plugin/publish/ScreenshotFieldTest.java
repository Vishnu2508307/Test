package com.smartsparrow.plugin.publish;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ScreenshotFieldTest {

    @InjectMocks
    private ScreenshotField screenshotField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() {

        ClassLoader classLoader = getClass().getClassLoader();
        File screenshot = load(classLoader, "img/mercury.jpg");
        assertNotNull(screenshot);
        Map<String, File> files = new HashMap<>();
        files.put("img/mercury.jpg", screenshot);

        PluginParserContext pluginParserContext = new PluginParserContext()
                .setFiles(files);
        Map<String, Object> jsonObject = new HashMap<>();
        List<String> screenShots = new ArrayList<>();
        screenShots.add("img/mercury.jpg");
        jsonObject.put(PluginParserConstant.SCREENSHOTS, screenShots);
        screenshotField = new ScreenshotField(PluginParserConstant.SCREENSHOTS, jsonObject);
        screenshotField.parse(pluginParserContext);
        assertNotNull(screenshotField);
    }

    @Test
    void parse_ERROR() {
        Map<String, File> files = new HashMap<>();
        PluginParserContext pluginParserContext = new PluginParserContext()
                .setFiles(files);
        Map<String, Object> jsonObject = new HashMap<>();
        List<String> screenShots = new ArrayList<>();
        screenShots.add("img/mercury.jpg");
        jsonObject.put(PluginParserConstant.SCREENSHOTS, screenShots);
        screenshotField = new ScreenshotField(PluginParserConstant.SCREENSHOTS, jsonObject);
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            screenshotField.parse(pluginParserContext);
        });
        assertTrue(t.getMessage().contains("not found inside the package"));
    }

    @Test
    void parse_success_EmptyScreenshotField() {
        Map<String, Object> jsonObject = new HashMap<>();
        List<String> screenShots = new ArrayList<>();
        jsonObject.put(PluginParserConstant.SCREENSHOTS, screenShots);
        screenshotField = new ScreenshotField(PluginParserConstant.SCREENSHOTS, jsonObject);
        screenshotField.parse(new PluginParserContext());
        assertNotNull(screenshotField);
    }

    @Test
    void parse_success_MissingScreenshotField() {
        Map<String, Object> jsonObject = new HashMap<>();
        screenshotField = new ScreenshotField(PluginParserConstant.SCREENSHOTS, jsonObject);
        screenshotField.parse(new PluginParserContext());
        assertNotNull(screenshotField);
    }

    private File load(ClassLoader classLoader, String name) {
        URL url = classLoader.getResource(name);
        if (url != null) {
            return new File(url.getFile());
        }
        return null;
    }

}
