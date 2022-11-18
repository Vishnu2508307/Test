package com.smartsparrow.plugin.publish;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SupportUrlFieldTest {
    @InjectMocks
    private SupportUrlField supportUrlField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.SUPPORTURL, " ");
        supportUrlField = new SupportUrlField(PluginParserConstant.SUPPORTURL, jsonObject);
        String description = supportUrlField.parse(new PluginParserContext());
        assertNotNull(description);
    }

    @Test
    void parse_missingSupportUrl() {
        Map<String, Object> jsonObject = new HashMap<>();
        supportUrlField = new SupportUrlField(PluginParserConstant.SUPPORTURL, jsonObject);
        String description = supportUrlField.parse(new PluginParserContext());
        assertNull(description);
    }
}
