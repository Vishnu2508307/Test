package com.smartsparrow.plugin.publish;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersionFieldTest {

    @InjectMocks
    private VersionField versionField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.VERSION, "1.2.0");
        versionField = new VersionField(PluginParserConstant.VERSION, jsonObject);
        String version = versionField.parse(new PluginParserContext());
        assertNotNull(version);
    }

    @Test
    void parse_MissingVersion() {
        Map<String, Object> jsonObject = new HashMap<>();
        versionField = new VersionField(PluginParserConstant.VERSION, jsonObject);
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            versionField.parse(new PluginParserContext());
        });
        assertTrue(t.getMessage().contains("version missing from manifest"));
    }
}
