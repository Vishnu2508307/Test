package com.smartsparrow.plugin.publish;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GuideFieldTest {

    @InjectMocks
    private GuideField guideField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() {
        Map<String, File> files = new HashMap<>();
        files.put("guide.md", new File(""));
        PluginParserContext pluginParserContext = new PluginParserContext()
                .setFiles(files);
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.GUIDE, "guide.md");
        guideField = new GuideField(PluginParserConstant.GUIDE, jsonObject);
        String description = guideField.parse(pluginParserContext);
        assertNotNull(description);
    }

    @Test
    void parse_missingGuildeField() {
        Map<String, Object> jsonObject = new HashMap<>();
        guideField = new GuideField(PluginParserConstant.GUIDE, jsonObject);
        String description = guideField.parse(new PluginParserContext());
        assertNull(description);
    }

    @Test
    void parse_missingGuildeFile() {
        Map<String, File> files = new HashMap<>();
        PluginParserContext pluginParserContext = new PluginParserContext()
                .setFiles(files);
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.GUIDE, "guide.md");
        guideField = new GuideField(PluginParserConstant.GUIDE, jsonObject);

        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            guideField.parse(pluginParserContext);
        });
        assertTrue(t.getMessage().contains("not found inside the package"));

    }


}
