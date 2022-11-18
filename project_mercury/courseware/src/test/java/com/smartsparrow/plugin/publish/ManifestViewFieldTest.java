package com.smartsparrow.plugin.publish;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManifestViewFieldTest {

    @InjectMocks
    private ManifestViewField manifestViewField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() throws IOException {
        Map<String, Object> jsonObject = new HashMap<>();
        Map<String, Object> viewmap = new LinkedHashMap<>();

        Map<String, Object> learnerMap = new LinkedHashMap<>();
        learnerMap.put("contentType", "javascript");
        learnerMap.put("entryPoint", "index.js");
        learnerMap.put("publicDir", "");
        viewmap.put("LEARNER", learnerMap);
        jsonObject.put(PluginParserConstant.VIEWS, viewmap);
        manifestViewField = new ManifestViewField(PluginParserConstant.VIEWS, jsonObject);
        String views = manifestViewField.parse(new PluginParserContext());
        assertNotNull(views);
    }

    @Test
    void parse_Error() {
        Map<String, Object> jsonObject = new HashMap<>();

        manifestViewField = new ManifestViewField(PluginParserConstant.VIEWS, jsonObject);
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            manifestViewField.parse(new PluginParserContext());
        });
        assertTrue(t.getMessage().contains("entry point views required"));
    }
}
