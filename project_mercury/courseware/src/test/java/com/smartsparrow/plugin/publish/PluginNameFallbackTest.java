package com.smartsparrow.plugin.publish;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class PluginNameFallbackTest {

    @InjectMocks
    private PluginNameFallback nameFallback;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void parse_success() throws IOException{
        ClassLoader classLoader = getClass().getClassLoader();
        File packageFile = load(classLoader, "package.json");
        Map<String, File> files = new HashMap<>();
        files.put("package.json",packageFile );
        PluginParserContext pluginParserContext = new PluginParserContext()
                .setFiles(files);
        String pluginName = nameFallback.apply(pluginParserContext);
        assertNotNull(pluginName);
    }

    private File load(ClassLoader classLoader, String name) {
        URL url = classLoader.getResource(name);
        if (url != null) {
            return new File(url.getFile());
        }
        return null;
    }
}
