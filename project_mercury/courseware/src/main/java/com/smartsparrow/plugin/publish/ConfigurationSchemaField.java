package com.smartsparrow.plugin.publish;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.lang.PluginPublishException;
import com.smartsparrow.plugin.lang.S3BucketLoadFileException;
import com.smartsparrow.plugin.service.SchemaValidationService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.everit.json.schema.ValidationException;

import javax.inject.Inject;


public class ConfigurationSchemaField implements PluginField<String, Map<String, Object>> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ConfigurationSchemaField.class);

    private String fieldName;
    private Map<String, Object> jsonObjectMap;

    @Inject
    private SchemaValidationService schemaValidationService;

    public ConfigurationSchemaField setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    public ConfigurationSchemaField setJsonObjectMap(Map<String, Object> jsonObjectMap) {
        this.jsonObjectMap = jsonObjectMap;
        return this;
    }

    @Override
    public String parse(PluginParserContext pluginParserContext) throws PluginPublishException, IOException {
        String configSchema = (String) jsonObjectMap.get(fieldName);
        checkArgument(configSchema != null, "configuration schema field missing in manifest");
        String configurationSchemaContent = getFileContent(pluginParserContext.getFiles(), configSchema);
        validateConfigurationSchema(configurationSchemaContent, pluginParserContext.getPluginType());
        pluginParserContext.setConfigurationSchema(configurationSchemaContent);
        return configurationSchemaContent;
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

    /**
     * Validate the configuration schema against the plugin schema
     *
     * @param configSchema the configuration schema string content
     * @param type         the plugin type
     * @throws PluginPublishException when failing to load the plugin schema or the configuration schema is invalid
     */
    private void validateConfigurationSchema(String configSchema, PluginType type) throws PluginPublishException {
        try {
            String schemaFileName = schemaValidationService.getSchemaFileName(type);
            schemaValidationService.validateWithFile(configSchema, schemaFileName);
        } catch (S3BucketLoadFileException e) {
            log.info("Can not load plugin schema from S3 bucket:" + e.getMessage());
            throw new PluginPublishException("Unable to validate the configuration schema");
        } catch (ValidationException e) {
            log.info(e.getAllMessages().toString());
            throw new PluginPublishException(String.format("Invalid configuration schema as '%s'", e.getAllMessages().toString()));
        }
    }
}
