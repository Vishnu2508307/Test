package com.smartsparrow.plugin.publish;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ThumbnailFieldTest {
    @InjectMocks
    private ThumbnailField thumbnailField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() {
        ClassLoader classLoader = getClass().getClassLoader();
        File thumbnail = load(classLoader, "img/thumbnail.png");
        assertNotNull(thumbnail);
        Map<String, File> files = new HashMap<>();
        files.put("img/thumbnail.png", thumbnail);

        PluginParserContext pluginParserContext = new PluginParserContext()
                .setFiles(files);
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.THUMBNAIL, "img/thumbnail.png");
        thumbnailField = new ThumbnailField(PluginParserConstant.THUMBNAIL, jsonObject);
        thumbnailField.parse(pluginParserContext);
        assertNotNull(thumbnailField);
    }

    @Test
    void parse_ERROR_missingThumbNailFile() {
        Map<String, File> files = new HashMap<>();
        PluginParserContext pluginParserContext = new PluginParserContext()
                .setFiles(files);
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.THUMBNAIL, "img/thumbnail.png");
        thumbnailField = new ThumbnailField(PluginParserConstant.THUMBNAIL, jsonObject);
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            thumbnailField.parse(pluginParserContext);
        });
        assertTrue(t.getMessage().contains("not found inside the package"));
    }

    @Test
    void parse_ERROR_missingThumbNailField() {
        Map<String, Object> jsonObject = new HashMap<>();
        thumbnailField = new ThumbnailField(PluginParserConstant.THUMBNAIL, jsonObject);
        String parse = thumbnailField.parse(new PluginParserContext());
        assertNull(parse);
    }

    private File load(ClassLoader classLoader, String name) {
        URL url = classLoader.getResource(name);
        if (url != null) {
            return new File(url.getFile());
        }
        return null;
    }
}
