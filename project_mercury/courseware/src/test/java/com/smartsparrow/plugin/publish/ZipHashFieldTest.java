package com.smartsparrow.plugin.publish;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ZipHashFieldTest {
    @InjectMocks
    private ZipHashField zipHashField;
    private static final String ZIP_HASH = "5cd282f2";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() {
        String ziphash = zipHashField.parse(new PluginParserContext()
                .setHash(ZIP_HASH));
        assertNotNull(ziphash);
    }
}

