package com.smartsparrow.plugin.publish;


import com.smartsparrow.plugin.data.PluginType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TypeFieldTest {

    @InjectMocks
    private TypeField typeField;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(PluginParserConstant.TYPE, "component");
        typeField = new TypeField(PluginParserConstant.TYPE, jsonObject);
        PluginType type = typeField.parse(new PluginParserContext());
        assertNotNull(type);
    }

    @Test
    void parse_ERROR() {
        Map<String, Object> jsonObject = new HashMap<>();
        typeField = new TypeField(PluginParserConstant.TYPE, jsonObject);
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            typeField.parse(new PluginParserContext());
        });
        assertTrue(t.getMessage().contains("type missing from manifest"));
    }
}
