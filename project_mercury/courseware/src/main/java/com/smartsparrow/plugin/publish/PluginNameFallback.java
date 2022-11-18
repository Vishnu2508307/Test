package com.smartsparrow.plugin.publish;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartsparrow.util.Json;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public class PluginNameFallback implements FallbackStrategy {

    @Override
    public String apply(PluginParserContext pluginParserContext) throws IOException {
        String packageContent = getFileContent(pluginParserContext.getFiles(), PluginParserConstant.PACKAGE_JSON);
        JsonNode manifestSchemaJsonNode = Json.toJsonNode(packageContent);
        JsonNode bronte = manifestSchemaJsonNode.get(PluginParserConstant.BRONTE_PACKAGE);
        checkArgument(bronte != null, "name missing from manifest");
        JsonNode pluginName = bronte.get(PluginParserConstant.NAME);
        checkArgument(pluginName != null, "name missing from manifest");
        pluginParserContext.setName(pluginName.asText());
        return pluginName.asText();
    }

    /**
     * Read the file and return its content as a string
     *
     * @param files    the map of file from which to read the file
     * @param fileName the name of the file to read
     * @return a {@link String} representation of the file content
     * @throws IOException when the file reading operation fails
     */
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "JDK 11 changes broke this rule")
    private String getFileContent(Map<String, File> files, String fileName) throws IOException {
        File file = files.get(fileName);
        checkArgument(file != null, String.format("%s file missing", fileName));
        try (Stream<String> lines = java.nio.file.Files.lines(Paths.get(file.getAbsolutePath()))) {
            return lines.collect(Collectors.joining());
        }
    }
}
