package com.smartsparrow.plugin.publish;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.smartsparrow.plugin.lang.PluginPublishException;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Singleton
public class PluginParserService {

    private static final ObjectMapper om = new ObjectMapper();
    private final ConfigurationSchemaField configurationSchemaField;
    private final PluginParser pluginParser;

    @Inject
    public PluginParserService(final ConfigurationSchemaField configurationSchemaField,
                               final PluginParser pluginParser) {
        this.configurationSchemaField = configurationSchemaField;
        this.pluginParser = pluginParser;
    }

    /**
     * Parse the manifest or json file and return the parsed plugin fields
     *
     * @param files       a map of file from which the manifest or package file get extracted
     * @param pluginId    the plugin id to override id from manifest file, can be null
     * @param hash        zip hash of the file
     * @param publisherId the publisher account id
     * @throws IOException when the file reading operation fails
     */
    public PluginParsedFields parse(final Map<String, File> files, final UUID pluginId, final String hash, final UUID publisherId) throws IOException, PluginPublishException {
        if (files.get(PluginParserConstant.MANIFEST_JSON) != null) {
            String manifestContent = getFileContent(files, PluginParserConstant.MANIFEST_JSON);
            Map<String, Object> manifestJsonObject = om.readValue(manifestContent, Map.class);

            PluginParserContext pluginParserContext = new PluginParserContext()
                    .setPluginId(pluginId)
                    .setFiles(files)
                    .setHash(hash)
                    .setPublisherId(publisherId);

            PluginParserBuilder pluginParserBuilder = new PluginParserBuilder()
                    .setFieldMap(new LinkedHashMap<>())
                    .setFallbackStrategies(new LinkedHashMap<>())
                    .withField(PluginParserConstant.DESCRIPTION, new DescriptionField(PluginParserConstant.DESCRIPTION, manifestJsonObject))
                    .withField(PluginParserConstant.TYPE, new TypeField(PluginParserConstant.TYPE, manifestJsonObject))
                    .withField(PluginParserConstant.CONFIG_SCHEMA, configurationSchemaField
                            .setFieldName(PluginParserConstant.CONFIG_SCHEMA)
                            .setJsonObjectMap(manifestJsonObject))
                    .withField(PluginParserConstant.NAME, new NameField(PluginParserConstant.NAME, manifestJsonObject))
                    .withField(PluginParserConstant.PLUGIN_ID, new PluginIdField(PluginParserConstant.PLUGIN_ID, manifestJsonObject))
                    .withField(PluginParserConstant.VERSION, new VersionField(PluginParserConstant.VERSION, manifestJsonObject))
                    .withField(PluginParserConstant.SCREENSHOTS, new ScreenshotField(PluginParserConstant.SCREENSHOTS, manifestJsonObject))
                    .withField(PluginParserConstant.THUMBNAIL, new ThumbnailField(PluginParserConstant.THUMBNAIL, manifestJsonObject))
                    .withField(PluginParserConstant.WHATSNEW, new WhatsNewField(PluginParserConstant.WHATSNEW, manifestJsonObject))
                    .withField(PluginParserConstant.WEBSITEURL, new WebsiteUrlField(PluginParserConstant.WEBSITEURL, manifestJsonObject))
                    .withField(PluginParserConstant.SUPPORTURL, new SupportUrlField(PluginParserConstant.SUPPORTURL, manifestJsonObject))
                    .withField(PluginParserConstant.TAGS, new TagsField(PluginParserConstant.TAGS, manifestJsonObject))
                    .withField(PluginParserConstant.GUIDE, new GuideField(PluginParserConstant.GUIDE, manifestJsonObject))
                    .withField(PluginParserConstant.ZIP_HASH, new ZipHashField())
                    .withField(PluginParserConstant.PUBLISHER_ID, new PublisherIdField())
                    .withField(PluginParserConstant.VIEWS, new ManifestViewField(PluginParserConstant.VIEWS, manifestJsonObject))
                    .withField(PluginParserConstant.SEARCHABLE, new SearchableField(PluginParserConstant.SEARCHABLE, manifestContent))
                    .withField(PluginParserConstant.DEFAULT_HEIGHT, new DefaultHeightField(PluginParserConstant.DEFAULT_HEIGHT, manifestJsonObject))
                    .withField(PluginParserConstant.PLUGIN_FILTERS, new PluginFilterField(PluginParserConstant.PLUGIN_FILTERS, manifestContent))
                    .addFallbackStrategyFor(PluginParserConstant.NAME, new PluginNameFallback());


            return pluginParser.parse(pluginParserContext, pluginParserBuilder);


        } else if (files.get(PluginParserConstant.PACKAGE_JSON) != null) {
            String manifestContent = getFileContent(files, PluginParserConstant.PACKAGE_JSON);
            Map<String, Object> packageJsonObject = om.readValue(manifestContent, Map.class);
            PluginParserContext pluginParserContext = new PluginParserContext()
                    .setPluginId(pluginId)
                    .setFiles(files)
                    .setHash(hash)
                    .setPublisherId(publisherId);

            Object bronteObject = packageJsonObject.get(PluginParserConstant.BRONTE);
            checkArgument(bronteObject != null, "bronte is missing in package.json");
            String bronteContent = om.writeValueAsString(bronteObject);
            Map<String, Object> bronteMap = om.readValue(bronteContent, Map.class);

            PluginParserBuilder pluginParserBuilder = new PluginParserBuilder()
                    .setFieldMap(new LinkedHashMap<>())
                    .setFallbackStrategies(new LinkedHashMap<>())
                    .withField(PluginParserConstant.DESCRIPTION, new DescriptionField(PluginParserConstant.DESCRIPTION, packageJsonObject))
                    .withField(PluginParserConstant.TYPE, new TypeField(PluginParserConstant.TYPE, bronteMap))
                    .withField(PluginParserConstant.CONFIG_SCHEMA, configurationSchemaField
                            .setFieldName(PluginParserConstant.CONFIG_SCHEMA)
                            .setJsonObjectMap(bronteMap))
                    .withField(PluginParserConstant.NAME, new NameField(PluginParserConstant.NAME, bronteMap))
                    .withField(PluginParserConstant.PLUGIN_ID, new PluginIdField(PluginParserConstant.PLUGIN_ID, bronteMap))
                    .withField(PluginParserConstant.VERSION, new VersionField(PluginParserConstant.VERSION, packageJsonObject))
                    .withField(PluginParserConstant.SCREENSHOTS, new ScreenshotField(PluginParserConstant.SCREENSHOTS, bronteMap))
                    .withField(PluginParserConstant.THUMBNAIL, new ThumbnailField(PluginParserConstant.THUMBNAIL, bronteMap))
                    .withField(PluginParserConstant.WHATSNEW, new WhatsNewField(PluginParserConstant.WHATSNEW, bronteMap))
                    .withField(PluginParserConstant.WEBSITEURL, new WebsiteUrlField(PluginParserConstant.WEBSITEURL, packageJsonObject))
                    .withField(PluginParserConstant.SUPPORTURL, new SupportUrlField(PluginParserConstant.SUPPORTURL, packageJsonObject))
                    .withField(PluginParserConstant.TAGS, new TagsField(PluginParserConstant.TAGS, bronteMap))
                    .withField(PluginParserConstant.GUIDE, new GuideField(PluginParserConstant.GUIDE, bronteMap))
                    .withField(PluginParserConstant.ZIP_HASH, new ZipHashField())
                    .withField(PluginParserConstant.PUBLISHER_ID, new PublisherIdField())
                    .withField(PluginParserConstant.VIEWS, new ManifestViewField(PluginParserConstant.VIEWS, bronteMap))
                    .withField(PluginParserConstant.SEARCHABLE, new SearchableField(PluginParserConstant.SEARCHABLE, bronteContent))
                    .withField(PluginParserConstant.DEFAULT_HEIGHT, new DefaultHeightField(PluginParserConstant.DEFAULT_HEIGHT, bronteMap))
                    .withField(PluginParserConstant.PLUGIN_FILTERS, new PluginFilterField(PluginParserConstant.PLUGIN_FILTERS, bronteContent))
                    .addFallbackStrategyFor(PluginParserConstant.NAME, new PluginNameFallback());

            return pluginParser.parse(pluginParserContext, pluginParserBuilder);
        } else {
            throw new IllegalArgumentException(String.valueOf(PluginParserConstant.MANIFEST_JSON + " file missing"));
        }
    }

    /**
     * Get bronte content from package json object
     *
     * @param packageJsonObject package json file object
     * @return a {@link Map} of bronte field name ->  field value object
     * @throws IOException when any I/O operation fails
     */
    private Map<String, Object> getBronteMap(final Map<String, Object> packageJsonObject) throws IOException {
        Object bronteObject = packageJsonObject.get(PluginParserConstant.BRONTE);
        checkArgument(bronteObject != null, "bronte is missing in package.json");
        String bronteContent = om.writeValueAsString(bronteObject);
        return om.readValue(bronteContent, Map.class);
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
    private String getFileContent(final Map<String, File> files, final String fileName) throws IOException {
        File file = files.get(fileName);
        checkArgument(file != null, String.format("%s file missing", fileName));

        try (Stream<String> lines = java.nio.file.Files.lines(Paths.get(file.getAbsolutePath()))) {
            return lines.collect(Collectors.joining());
        }
    }
}
