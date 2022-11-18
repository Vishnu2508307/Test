package com.smartsparrow.plugin.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Sets;
import com.google.inject.Provider;
import com.smartsparrow.courseware.data.ActivityGateway;
import com.smartsparrow.courseware.data.ComponentGateway;
import com.smartsparrow.courseware.data.InteractiveGateway;
import com.smartsparrow.exception.ConflictFault;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.MethodNotAllowedFault;
import com.smartsparrow.exception.NotAllowedException;
import com.smartsparrow.iam.data.team.TeamAccount;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountIdentityAttributes;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.TeamService;
import com.smartsparrow.plugin.data.DeletedPlugin;
import com.smartsparrow.plugin.data.LTIProviderCredential;
import com.smartsparrow.plugin.data.ManifestView;
import com.smartsparrow.plugin.data.PluginAccessGateway;
import com.smartsparrow.plugin.data.PluginAccount;
import com.smartsparrow.plugin.data.PluginAccountCollaborator;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginFilterType;
import com.smartsparrow.plugin.data.PluginGateway;
import com.smartsparrow.plugin.data.PluginManifest;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.data.PluginVersion;
import com.smartsparrow.plugin.data.PublishMode;
import com.smartsparrow.plugin.lang.PluginAlreadyExistsFault;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.PluginPublishException;
import com.smartsparrow.plugin.lang.PluginSchemaParserFault;
import com.smartsparrow.plugin.lang.S3BucketLoadFileException;
import com.smartsparrow.plugin.lang.S3BucketUploadException;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.payload.PluginPayload;
import com.smartsparrow.plugin.payload.PluginSummaryPayload;
import com.smartsparrow.plugin.publish.PluginParsedFields;
import com.smartsparrow.plugin.publish.PluginParserService;
import com.smartsparrow.plugin.wiring.PluginConfig;
import com.smartsparrow.util.Json;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

class PluginServiceTest {

    @InjectMocks
    private PluginService pluginService;
    @Mock
    private PluginGateway pluginGateway;
    @Mock
    private PluginAccessGateway pluginAccessGateway;
    @Mock
    private S3Bucket s3Bucket;
    @Mock
    private Provider<PluginConfig> pluginConfigProvider;
    @Mock
    private PluginConfig pluginConfig;
    @Mock
    private PluginSummary pluginSummary;
    @Mock
    private AccountService accountService;
    @Mock
    private PluginPermissionService pluginPermissionService;
    @Mock
    private TeamService teamService;
    @Mock
    private SchemaValidationService schemaValidationService;
    @Mock
    private PluginSchemaParser pluginSchemaParser;
    @Mock
    private PluginParserService pluginParserService;
    @Mock
    private ActivityGateway activityGateway;
    @Mock
    private InteractiveGateway interactiveGateway;
    @Mock
    private ComponentGateway componentGateway;

    private static final UUID PLUGIN_ID = UUIDs.timeBased();
    private static final Integer major = 1;
    private static final Integer minor = 22;
    private static final Integer patch = 144;
    private static final UUID WRONG_PLUGIN_ID = UUIDs.timeBased();
    private static final UUID publisherId = UUID.randomUUID();
    private static final String VIEW = "EDITOR";
    private static final String VERSION = "1.2.0";
    private static final String LATEST_VERSION = "2.0.0";
    private static final String ZIP_HASH = "5cd282f2";
    private static final String PLUGIN_VERSION = "1.22.143";
    private String name;
    private PluginType type;
    private Account account;
    private static final String pluginNotAZipFileName = "plugin_not_a_zip.zip";
    private static final String pluginNoViewsFileName = "plugin_no_view.zip";
    private static final String pluginFileName = "plugin_success.zip";
    private static final String pluginFileNameWithTags = "plugin_success_tags.zip";
    private static final String pluginFileNameWitheText = "eText_plugin_success.zip";
    private static final String pluginThumbnailAndScreenshotExists = "plugin_valid_thumbnail_and_screenshot.zip";
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final String distributionPublicUrl = "publicUrl";
    private static final String repositoryPublicUrl = "repositoryPublicUrl";
    private static final String manifestContentWithView = "{\n" +
            "    \"name\": \"Simple Course\",\n" +
            "    \"description\": \"A course plugin that displays a list of units and lessons\",\n" +
            "    \"version\": \"1.2.0\",\n" +
            "    \"author\": \"Sparrow.Phoenix\",\n" +
            "    \"email\": \"\",\n" +
            "    \"type\": \"course\",\n" +
            "    \"configurationSchema\": \"schemas/aeroProperties.schema.json\",\n" +
            "    \"views\": {\n" +
            "        \"LEARNER\": {\n" +
            "            \"contentType\": \"text/html\",\n" +
            "            \"publicDir\": \"dist\",\n" +
            "            \"entryPoint\": \"index.html\"\n" +
            "        }\n" +
            "    }\n" +
            "}";

    String searchableConfigSchema ="{\n" +
            "    \"title\": {\n" +
            "        \"type\": \"text\"\n" +
            "    },\n" +
            "    \"description\": {\n" +
            "        \"type\": \"rich-text\"\n" +
            "    },\n" +
            "    \"items\": {\n" +
            "        \"type\": \"list\",\n" +
            "        \"listType\": \"text\",\n" +
            "        \"label\": \"items\"\n" +
            "    },\n" +
            "    \"selection\": {\n" +
            "        \"type\": \"list\",\n" +
            "        \"listType\": \"text\",\n" +
            "        \"learnerEditable\": true,\n" +
            "        \"label\": \"selection\"\n" +
            "    },\n" +
            "    \"options\": {\n" +
            "        \"type\": \"group\",\n" +
            "        \"flat\": true,\n" +
            "        \"properties\": {\n" +
            "            \"allowMultipleSelections\": {\n" +
            "                \"type\": \"boolean\",\n" +
            "                \"default\": false,\n" +
            "                \"label\": \"allow mulit-select\"\n" +
            "            },\n" +
            "            \"layout\": {\n" +
            "                \"type\": \"enum\",\n" +
            "                \"items\": [\n" +
            "                    \"vertical\",\n" +
            "                    \"horizontal\"\n" +
            "                ],\n" +
            "                \"default\": \"vertical\"\n" +
            "            },\n" +
            "            \"foo\": {\n" +
            "                \"type\": \"text\",\n" +
            "                \"learnerEditable\": true,\n" +
            "                \"default\": \"default\"\n" +
            "            }\n" +
            "        }\n" +
            "    },\n" +
            "    \"cards\": {\n" +
            "        \"type\": \"list\",\n" +
            "        \"label\": \"Cards\",\n" +
            "        \"description\": \"The images in the component.\",\n" +
            "        \"listType\": {\n" +
            "            \"type\": \"group\",\n" +
            "            \"properties\": {\n" +
            "                \"front-image\": {\n" +
            "                    \"type\": \"image\",\n" +
            "                    \"label\": \"Front Image\",\n" +
            "                    \"description\": \"The image (optional) on the front (default side) of the card\"\n" +
            "                },\n" +
            "                \"front-text\": {\n" +
            "                    \"type\": \"rich-text\",\n" +
            "                    \"label\": \"Front Text\",\n" +
            "                    \"default\": \"Front text\",\n" +
            "                    \"description\": \"The text on the front (default side) of the card\"\n" +
            "                },\n" +
            "                \"back-image\": {\n" +
            "                    \"type\": \"image\",\n" +
            "                    \"label\": \"Back Image\",\n" +
            "                    \"description\": \"The image (optional) on the back (default side) of the card\"\n" +
            "                },\n" +
            "                \"back-text\": {\n" +
            "                    \"type\": \"rich-text\",\n" +
            "                    \"label\": \"Back Text\",\n" +
            "                    \"default\": \"Back text\",\n" +
            "                    \"description\": \"The text on the back (default side) of the card\"\n" +
            "                }\n" +
            "            }\n" +
            "        },\n" +
            "        \"default\": [\n" +
            "            {\n" +
            "                \"label\": \"Card 1\",\n" +
            "                \"front-image\": \"\",\n" +
            "                \"front-text\": \"Front text\",\n" +
            "                \"back-text\": \"Back text\",\n" +
            "                \"back-image\": \"\"\n" +
            "            },\n" +
            "            {\n" +
            "                \"label\": \"Card 2\",\n" +
            "                \"front-image\": \"\",\n" +
            "                \"front-text\": \"Front text\",\n" +
            "                \"back-text\": \"Back text\",\n" +
            "                \"back-image\": \"\"\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    \"stage\": {\n" +
            "        \"type\": \"group\",\n" +
            "        \"properties\": {},\n" +
            "        \"hidden\": true\n" +
            "    }\n" +
            "}\n";

    String searchable = "[{\"contentType\": \"mcq\"," +
            "\"summary\": \"title\"," +
            "\"body\": \"selection\"}," +
            "{\"contentType\": \"text\"," +
            "\"body\": [\"options.foo\"]}," +
            "{\"contentType\": \"image\"," +
            "\"body\": [\"cards.front-text\"," +
            "\"cards.back-text\"]," +
            "\"source\": [\"cards.front-image\"," +
            "\"cards.back-image\"]}," +
            "{\"contentType\": \"text\"," +
            "\"summary\": [\"title\"]," +
            "\"body\": [\"stage.text\"]}]";

    String missingContentSearchable = "[{"+
            "\"summary\":\"title\",\"body\":\"selection\"}," +
            "{\"contentType\":\"text\",\"body\":[\"options.foo\"]}," +
            "{\"contentType\":\"image\",\"body\":[\"cards.front-text\",\"cards.back-text\"]," +
            "\"source\":[\"cards.front-image\",\"cards.back-image\"]}," +
            "{\"contentType\":\"text\",\"summary\":[\"title\"],\"body\":[\"stage.text\"]}]";


    String missingFieldConfigSchema = "{\n" +
            "\t\"title\": {\n" +
            "      \"type\":\"text\"\n" +
            "    },\n" +
            "\n" +
            "    \"options\": {\n" +
            "      \"type\": \"group\",\n" +
            "      \"flat\": true,\n" +
            "      \"properties\": {\n" +
            "        \"allowMultipleSelections\": {\n" +
            "          \"type\": \"boolean\",\n" +
            "          \"default\": false,\n" +
            "          \"label\": \"allow mulit-select\"\n" +
            "        },\n" +
            "        \"layout\": {\n" +
            "          \"type\": \"enum\",\n" +
            "          \"items\": [ \"vertical\", \"horizontal\" ],\n" +
            "          \"default\": \"vertical\"\n" +
            "        },\n" +
            "        \"foo\": {\n" +
            "          \"type\": \"text\",\n" +
            "          \"learnerEditable\": true,\n" +
            "          \"default\": \"default\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n";

    String missingFieldTypeConfigSchema = "{\n" +
            "\t\"title\": {\n" +
            "      \"type\":\"text\"\n" +
            "    },\n" +
            "    \"description\":{\n" +
            "      \"type\": \"rich-text\"\n" +
            "    },\n" +
            "    \n" +
            "    \"selection\": {\n" +
            "     \n" +
            "      \"listType\": \"text\",\n" +
            "      \"learnerEditable\": true,\n" +
            "      \"label\": \"selection\"\n" +
            "    }\n" +
            "    \n" +
            "  }\n";

    private static final String pluginPackageFileName = "plugin_package.zip";

    PluginVersion pluginVersion1 = new PluginVersion()
            .setPluginId(pluginId)
            .setBuild("124")
            .setMajor(major)
            .setMinor(minor)
            .setPatch(patch)
            .setPreRelease("beta")
            .setUnpublished(false);

    PluginVersion pluginVersion2 = new PluginVersion()
            .setPluginId(pluginId)
            .setBuild("123")
            .setMajor(major)
            .setMinor(minor)
            .setPatch(patch + 1)
            .setPreRelease("alpha")
            .setUnpublished(false);

    PluginVersion unPublishedPluginVersion = new PluginVersion()
            .setPluginId(pluginId)
            .setBuild("123")
            .setMajor(major)
            .setMinor(minor)
            .setPatch(patch - 1)
            .setPreRelease("alpha-1")
            .setUnpublished(true);


    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(), eq(2))).thenReturn(Flux.empty());
        pluginService = new PluginService(pluginGateway, pluginConfigProvider, s3Bucket, pluginAccessGateway,
                                          accountService, pluginPermissionService, teamService, schemaValidationService,
                                          pluginSchemaParser, pluginParserService, activityGateway, interactiveGateway,
                                          componentGateway);

        account = mock(Account.class);
        name = "plugin name";
        type = PluginType.COMPONENT;

        pluginConfig = mock(PluginConfig.class);
        AccountIdentityAttributes identity = mock(AccountIdentityAttributes.class);

        when(pluginConfigProvider.get()).thenReturn(pluginConfig);
        when(pluginConfig.getDistributionPublicUrl()).thenReturn(distributionPublicUrl);
        when(pluginConfig.getRepositoryPublicUrl()).thenReturn(repositoryPublicUrl);
        when(account.getId()).thenReturn(accountId);
        when(account.getSubscriptionId()).thenReturn(UUID.randomUUID());
        when(pluginGateway.persistSummary(any(PluginSummary.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginAccessGateway.persist(any(PluginAccount.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginAccessGateway.persist(any(PluginAccountCollaborator.class))).thenReturn(Flux.just(new Void[]{}));

        when(pluginGateway.fetchAllPluginVersionsById(eq(PLUGIN_ID))).thenReturn(
                Flux.just(createPluginVersion(2, 0, 0),
                        createPluginVersion(1, 2, 0),
                        createPluginVersion(1, 1, 0)));

        when(pluginGateway.fetchAllPluginVersionsById(eq(WRONG_PLUGIN_ID))).thenReturn(Flux.empty());
        when(identity.getPrimaryEmail()).thenReturn("some@email.dev");
        when(pluginSummary.getLatestVersion()).thenReturn("2.0.0");
        when(pluginSummary.isPublished()).thenReturn(true);
        when(pluginSummary.getId()).thenReturn(PLUGIN_ID);
        when(pluginSummary.getPublishMode()).thenReturn(PublishMode.DEFAULT);
        when(pluginSummary.getCreatorId()).thenReturn(accountId);
        when(accountService.getAccountPayload(accountId)).thenReturn(Mono.just(new AccountPayload().setAccountId(accountId)));
        when(pluginGateway.fetchPluginSummaryById(eq(PLUGIN_ID))).thenReturn(Mono.just(pluginSummary));
        when(schemaValidationService.getSchemaFileName(any(PluginType.class))).thenReturn("foo");
        when(pluginGateway.persistSearchableFieldByPlugin(any(Flux.class))).thenReturn(Flux.empty());
        when(pluginPermissionService.findHighestPermissionLevel(any(), any())).thenReturn(Mono.just(PermissionLevel.OWNER));
        when(pluginGateway.persistPluginFilters(any())).thenReturn(Flux.just(new Void[]{}));
    }

    @Test
    void findPluginByIdAndView_noPlugin() {
        assertThrows(IllegalArgumentException.class, () -> pluginService.findPluginByIdAndView(null, VIEW, VERSION));
    }

    @Test
    void findPluginByIdAndView_noView() {
        assertThrows(IllegalArgumentException.class, () -> pluginService.findPluginByIdAndView(PLUGIN_ID, null, VERSION));
        assertThrows(IllegalArgumentException.class, () -> pluginService.findPluginByIdAndView(PLUGIN_ID, "", VERSION));
    }

    @Test
    void findPluginByIdAndView_noVersion() throws VersionParserFault {
        when(pluginGateway.fetchPluginManifestByIdVersion(eq(PLUGIN_ID), eq(LATEST_VERSION))).thenReturn(Mono.just(new PluginManifest()));
        when(pluginGateway.fetchManifestViewByIdVersionContext(eq(PLUGIN_ID), eq(LATEST_VERSION), eq(VIEW)))
                .thenReturn(Mono.just(new ManifestView()));

        when(pluginConfigProvider.get()).thenReturn(new PluginConfig().setDistributionPublicUrl("publicUrl"));

        PluginPayload result = pluginService.findPluginByIdAndView(PLUGIN_ID, VIEW, null).block();

        assertNotNull(result);
        verify(pluginGateway, never()).fetchAllPluginVersionsById(eq(PLUGIN_ID));
    }

    @Test
    void findPluginByIdAndView_buildPaths() throws VersionParserFault {
        when(pluginGateway.fetchPluginManifestByIdVersion(eq(PLUGIN_ID), eq(LATEST_VERSION))).thenReturn(Mono.just(
                new PluginManifest()
                        .setPluginId(PLUGIN_ID)
                        .setZipHash(ZIP_HASH)
                        .setScreenshots(Sets.newHashSet("screenshot1.png"))
                        .setThumbnail("thumbnail.png"))
        );
        when(pluginGateway.fetchManifestViewByIdVersionContext(eq(PLUGIN_ID), eq(LATEST_VERSION), eq(VIEW)))
                .thenReturn(Mono.just(
                        new ManifestView()
                                .setEntryPointPath("index.js").setPublicDir("publicDir/"))
                );

        when(pluginConfigProvider.get()).thenReturn(new PluginConfig().setDistributionPublicUrl("publicUrl"));

        PluginPayload result = pluginService.findPluginByIdAndView(PLUGIN_ID, VIEW, null).block();

        assertNotNull(result);
        assertEquals("publicUrl/" + PLUGIN_ID + "/" + ZIP_HASH + "/screenshot1.png", result.getManifest().getScreenshots().toArray()[0]);
        assertEquals("publicUrl/" + PLUGIN_ID + "/" + ZIP_HASH + "/thumbnail.png", result.getManifest().getThumbnail());
        assertEquals("publicUrl/" + PLUGIN_ID + "/" + ZIP_HASH + "/publicDir/index.js", result.getEntryPoints().get(0).getEntryPointPath());
    }

    @Test
    void findLatestVersion() throws VersionParserFault {
        final String version = "1.1.0";

        StepVerifier
                .create(pluginService.findLatestVersion(PLUGIN_ID, version))
                .expectNext(version)
                .verifyComplete();
    }

    @Test
    void findLatestVersion_VersionWildCard() throws VersionParserFault {
        final String wildcard = "1.*";
        final String expected_version = "1.2.0";

        StepVerifier
                .create(pluginService.findLatestVersion(PLUGIN_ID, wildcard))
                .expectNext(expected_version)
                .verifyComplete();
    }

    @Test
    void findPluginByIdAndView_pluginNotFound() {
        when(pluginGateway.fetchPluginSummaryById(eq(WRONG_PLUGIN_ID))).thenReturn(Mono.empty());
        PluginNotFoundFault t = assertThrows(PluginNotFoundFault.class, () -> pluginService.findPluginByIdAndView(WRONG_PLUGIN_ID, VIEW, VERSION).block());
        assertEquals("summary not found for plugin_id='" + WRONG_PLUGIN_ID + "'", t.getMessage());
    }

    @Test
    void findPluginByIdAndView_versionNotFound() {
        PluginNotFoundFault t = assertThrows(PluginNotFoundFault.class, () -> pluginService.findPluginByIdAndView(PLUGIN_ID, VIEW, "3.*").block());
        assertEquals("Version '3.*' for plugin_id='" + PLUGIN_ID + "' does not exist", t.getMessage());
    }

    @Test
    void findPluginByIdAndView_viewNotFound() {
        when(pluginGateway.fetchPluginManifestByIdVersion(eq(PLUGIN_ID), eq(VERSION))).thenReturn(Mono.just(new PluginManifest()));
        when(pluginGateway.fetchManifestViewByIdVersionContext(eq(PLUGIN_ID), eq(VERSION), eq(VIEW))).thenReturn(Mono.empty());

        PluginNotFoundFault t = assertThrows(PluginNotFoundFault.class,
                () -> pluginService.findPluginByIdAndView(PLUGIN_ID, VIEW, VERSION).block());
        assertEquals("view '" + VIEW + "' for plugin '" + PLUGIN_ID + "' and version '" + VERSION + "' does not exist", t.getMessage());
    }

    @Test
    void findPluginByIdAndView_manifestNotFound() {
        when(pluginGateway.fetchPluginManifestByIdVersion(eq(PLUGIN_ID), eq(VERSION))).thenReturn(Mono.empty());
        when(pluginGateway.fetchManifestViewByIdVersionContext(eq(PLUGIN_ID), eq(VERSION), eq(VIEW))).thenReturn(Mono.just(new ManifestView()));

        PluginPayload payload = pluginService.findPluginByIdAndView(PLUGIN_ID, VIEW, VERSION).block();
        assertNotNull(payload);
        assertNull(payload.getManifest());
    }

    @Test
    void findLatestVersion_unParsedVersion() {
        assertThrows(VersionParserFault.class, () -> pluginService.findLatestVersion(PLUGIN_ID, "1*").block());
    }

    @Test
    void findPlugin_noPluginId() {
        Throwable t = assertThrows(IllegalArgumentException.class, ()-> pluginService.findPlugin(null, null));
        assertEquals("pluginId is required", t.getMessage());
    }

    @Test
    void findPlugin_versionNotFound() {
        when(pluginGateway.fetchAllPluginVersionsById(PLUGIN_ID)).thenReturn(Flux.empty());

        Throwable t = assertThrows(PluginNotFoundFault.class, () -> pluginService.findPlugin(PLUGIN_ID, VERSION).block());
        assertEquals("Version '1.2.0' for plugin_id='" + PLUGIN_ID + "' does not exist", t.getMessage());
    }

    @Test
    void findPlugin_pluginNotFound() {
        when(pluginGateway.fetchAllPluginVersionsById(WRONG_PLUGIN_ID)).thenReturn(Flux.just(new PluginVersion()
        .setMajor(1)
        .setMinor(2)
        .setPatch(0)));
        when(pluginGateway.fetchPluginSummaryById(WRONG_PLUGIN_ID)).thenReturn(Mono.empty());
        when(pluginGateway.fetchViews(WRONG_PLUGIN_ID, VERSION)).thenReturn(Flux.empty());
        when(pluginGateway.fetchPluginManifestByIdVersion(eq(WRONG_PLUGIN_ID), anyString())).thenReturn(Mono.just(new PluginManifest()));

        Throwable t = assertThrows(PluginNotFoundFault.class, () -> pluginService.findPlugin(WRONG_PLUGIN_ID, VERSION).block());
        assertEquals(String.format("summary not found for plugin_id='%s'", WRONG_PLUGIN_ID), t.getMessage());
    }

    @Test
    void findPlugin_viewsNotFound() throws VersionParserFault {
        when(pluginGateway.fetchAllPluginVersionsById(WRONG_PLUGIN_ID)).thenReturn(Flux.just(new PluginVersion()
                .setMajor(1)
                .setMinor(2)
                .setPatch(0)));
        when(pluginGateway.fetchPluginSummaryById(WRONG_PLUGIN_ID)).thenReturn(Mono.just(pluginSummary));
        when(pluginGateway.fetchViews(eq(WRONG_PLUGIN_ID), anyString())).thenReturn(Flux.empty());
        when(pluginGateway.fetchPluginManifestByIdVersion(eq(WRONG_PLUGIN_ID), anyString())).thenReturn(Mono.just(new PluginManifest()));

        PluginPayload plugin = pluginService.findPlugin(WRONG_PLUGIN_ID, VERSION).block();

        assertNotNull(plugin);
        assertTrue(plugin.getEntryPoints().isEmpty());
    }

    @Test
    void findPlugin_manifestNotFound() {
        when(pluginGateway.fetchAllPluginVersionsById(WRONG_PLUGIN_ID)).thenReturn(Flux.just(new PluginVersion()
                .setMajor(1)
                .setMinor(2)
                .setPatch(0)));
        when(pluginGateway.fetchPluginSummaryById(WRONG_PLUGIN_ID)).thenReturn(Mono.just(new PluginSummary()
                .setCreatorId(accountId)
                .setId(WRONG_PLUGIN_ID)
                .setLatestVersion(VERSION)));
        when(pluginGateway.fetchViews(eq(WRONG_PLUGIN_ID), anyString())).thenReturn(Flux.empty());
        when(pluginGateway.fetchPluginManifestByIdVersion(eq(WRONG_PLUGIN_ID), anyString())).thenReturn(Mono.empty());

        PluginPayload result = pluginService.findPlugin(WRONG_PLUGIN_ID, VERSION).block();
        assertNotNull(result);
        assertNull(result.getManifest());
    }

    @Test
    void findPlugin() throws VersionParserFault {
        when(pluginGateway.fetchAllPluginVersionsById(WRONG_PLUGIN_ID)).thenReturn(Flux.just(new PluginVersion()
                .setMajor(1)
                .setMinor(2)
                .setPatch(0)));
        when(pluginGateway.fetchPluginSummaryById(WRONG_PLUGIN_ID)).thenReturn(Mono.just(pluginSummary));
        when(pluginGateway.fetchViews(eq(WRONG_PLUGIN_ID), anyString())).thenReturn(Flux.just(new ManifestView()));
        when(pluginGateway.fetchPluginManifestByIdVersion(eq(WRONG_PLUGIN_ID), anyString())).thenReturn(Mono.just(new PluginManifest()));

        PluginPayload plugin = pluginService.findPlugin(WRONG_PLUGIN_ID, VERSION).block();
        assertNotNull(plugin);
    }

    @Test
    void findLatestVersion_withPreRelease() throws VersionParserFault {
        final String expected_version = "2.0.0-beta";

        when(pluginGateway.fetchAllPluginVersionsById(eq(PLUGIN_ID))).thenReturn(
                Flux.just(createPluginVersion(2, 0, 0).setPreRelease("beta"),
                        createPluginVersion(1, 2, 0),
                        createPluginVersion(1, 1, 0)));

        StepVerifier
                .create(pluginService.findLatestVersion(PLUGIN_ID, expected_version))
                .expectNext(expected_version)
                .verifyComplete();
    }

    @Test
    void findLatestVersion_invalidVersion1() {
        final String expected_version = "*2.0.0";

        assertThrows(VersionParserFault.class, () -> pluginService.findLatestVersion(PLUGIN_ID, expected_version).block());
    }

    @Test
    void findLatestVersion_invalidVersion2() {
        final String expected_version = "2.0.0-*";

        assertThrows(VersionParserFault.class, () -> pluginService.findLatestVersion(PLUGIN_ID, expected_version).block());
    }

    @Test
    void findLatestVersion_noStableVersions() throws VersionParserFault {
        when(pluginGateway.fetchPluginSummaryById(eq(PLUGIN_ID))).thenReturn(Mono.just(new PluginSummary().setLatestVersion("2.0.0-beta")));
        when(pluginGateway.fetchAllPluginVersionsById(eq(PLUGIN_ID))).thenReturn(
                Flux.just(
                        createPluginVersion(2, 0, 0).setPreRelease("beta")));

        assertVersionNotFound("*");
        assertVersionNotFound("2.*");
    }

    @Test
    void findLatestVersion_noExpr_noLatest() throws VersionParserFault {
        when(pluginGateway.fetchPluginSummaryById(eq(PLUGIN_ID))).thenReturn(Mono.just(new PluginSummary().setLatestVersion(null)));

        assertVersionNotFound(null);
        assertVersionNotFound("");
        verify(pluginGateway, never()).fetchAllPluginVersionsById(eq(PLUGIN_ID));
    }

    @Test
    void findLatestVersion_noExpr_latestStable() throws VersionParserFault {
        when(pluginGateway.fetchPluginSummaryById(eq(PLUGIN_ID))).thenReturn(Mono.just(new PluginSummary().setLatestVersion("10.0.0")));

        assertVersion("", "10.0.0");
        verify(pluginGateway, never()).fetchAllPluginVersionsById(eq(PLUGIN_ID));
    }

    @Test
    void findLatestVersion_noExpr_latestUnStable() throws VersionParserFault {
        when(pluginGateway.fetchPluginSummaryById(eq(PLUGIN_ID))).thenReturn(Mono.just(new PluginSummary().setLatestVersion("10.0.0-alpha")));

        assertVersion("", "10.0.0-alpha");
        verify(pluginGateway, never()).fetchAllPluginVersionsById(eq(PLUGIN_ID));
    }

    @Test
    void findLatestVersion_expr() throws VersionParserFault {
        when(pluginGateway.fetchPluginSummaryById(eq(PLUGIN_ID))).thenReturn(Mono.just(new PluginSummary().setLatestVersion("2.0.0")));
        when(pluginGateway.fetchAllPluginVersionsById(eq(PLUGIN_ID))).thenReturn(
                Flux.just(
                        createPluginVersion(5, 1, 0).setPreRelease("alpha"),
                        createPluginVersion(5, 0, 0).setPreRelease("alpha"),
                        createPluginVersion(3, 0, 0).setPreRelease("beta"),
                        createPluginVersion(2, 0, 0),
                        createPluginVersion(1, 1, 1).setPreRelease("alpha"),
                        createPluginVersion(1, 0, 0)));

        assertVersion("*", "2.0.0");
        assertVersion("1.*", "1.0.0");
        assertVersionNotFound("1.1.*");
        assertVersion("1.1.1-alpha", "1.1.1-alpha");
        assertVersion("2.*", "2.0.0");
        assertVersionNotFound("2.1.*");
        assertVersionNotFound("3.*");
        assertVersionNotFound("4.*");
        assertVersionNotFound("5.*");
    }

    private void assertVersionNotFound(String versionExpr) {
        StepVerifier
                .create(pluginService.findLatestVersion(PLUGIN_ID, versionExpr))
                .expectError(PluginNotFoundFault.class)
                .verify();
    }

    private void assertVersion(String versionExpr, String expected) {
        StepVerifier
                .create(pluginService.findLatestVersion(PLUGIN_ID, versionExpr))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void findLatestVersion_noPlugin() {
        when(pluginGateway.fetchPluginSummaryById(eq(PLUGIN_ID))).thenReturn(Mono.empty());

        assertVersionNotFound("");
        verify(pluginGateway, never()).fetchAllPluginVersionsById(eq(PLUGIN_ID));
    }

    @Test
    void buildZipPath() {
        PluginManifest pluginManifest = new PluginManifest()
                .setPluginId(PLUGIN_ID)
                .setVersion(LATEST_VERSION)
                .setZipHash(ZIP_HASH);

        String result = pluginManifest.getBuildZipPath(pluginManifest);

        assertEquals(PLUGIN_ID + "/" + LATEST_VERSION + "/" + ZIP_HASH + ".zip", result);
    }

    @Test
    void buildFilePath() {
        PluginManifest pluginManifest = new PluginManifest()
                .setPluginId(PLUGIN_ID)
                .setZipHash(ZIP_HASH);

        String result = pluginManifest.getBuildFilePath(pluginManifest, "test.html");

        assertEquals(PLUGIN_ID + "/" + ZIP_HASH + "/test.html", result);
    }

    @Test
    void buildPublicUrl() {
        PluginManifest pluginManifest = new PluginManifest()
                .setPluginId(PLUGIN_ID)
                .setZipHash(ZIP_HASH);

        when(pluginConfigProvider.get()).thenReturn(new PluginConfig().setDistributionPublicUrl("publicUrl"));

        String result = pluginService.buildPublicUrl(pluginManifest, "test.html");

        assertEquals("publicUrl/" + PLUGIN_ID + "/" + ZIP_HASH + "/test.html", result);
    }

    @Test
    void findPluginByIdAndView_noConfig() {
        when(pluginConfigProvider.get()).thenReturn(null);
        when(pluginGateway.fetchPluginManifestByIdVersion(eq(PLUGIN_ID), eq(VERSION))).thenReturn(Mono.just(new PluginManifest().setPluginId(PLUGIN_ID)));
        when(pluginGateway.fetchManifestViewByIdVersionContext(eq(PLUGIN_ID), eq(VERSION), eq(VIEW)))
                .thenReturn(Mono.just(new ManifestView()));

        assertThrows(NullPointerException.class, () -> pluginService.findPluginByIdAndView(PLUGIN_ID, VIEW, VERSION).block());
    }

    private static PluginVersion createPluginVersion(int maj, int min, int patch) {
        return new PluginVersion().setPluginId(PLUGIN_ID).setMajor(maj).setMinor(min).setPatch(patch);
    }

    @Test
    void parseAndValidateVersion() throws VersionParserFault, PluginPublishException {
        PluginManifest pluginManifest = new PluginManifest();
        pluginManifest.setVersion("2.0.0");
        pluginManifest.setPluginId(PLUGIN_ID);

        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(), eq(2))).thenReturn(Flux.empty());

        PluginVersion result = pluginService.buildPluginVersion(pluginManifest);

        assertEquals(2, result.getMajor());
        assertEquals(0, result.getMinor());
        assertEquals(0, result.getPatch());
        assertEquals(null, result.getPreRelease());
        assertEquals(null, result.getBuild());
    }

    @Test
    void parseAndValidateVersion_WithPreRelease() {
        PluginManifest pluginManifest = new PluginManifest();
        pluginManifest.setVersion("2.0.0-alpha");
        pluginManifest.setPluginId(PLUGIN_ID);

        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(), eq(2)))
                .thenReturn(Flux.just(new PluginVersion().setMajor(2).setMinor(0).setPatch(0)));

        assertThrows(IllegalArgumentException.class, () -> pluginService.buildPluginVersion(pluginManifest));
    }

    @Test
    void parseAndValidateVersion_WithLowestVersion() {
        PluginManifest pluginManifest = new PluginManifest();
        pluginManifest.setVersion("2.0.0");
        pluginManifest.setPluginId(PLUGIN_ID);

        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(), eq(2)))
                .thenReturn(Flux.just(new PluginVersion().setMajor(2).setMinor(1).setPatch(0)));

        assertThrows(IllegalArgumentException.class, () -> pluginService.buildPluginVersion(pluginManifest));
    }

    @Test
    void parseAndValidateVersion_InvalidVersion() {
        PluginManifest pluginManifest = new PluginManifest();
        pluginManifest.setVersion("2.*");
        pluginManifest.setPluginId(PLUGIN_ID);

        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(), eq(2))).thenReturn(Flux.empty());

        assertThrows(VersionParserFault.class, () -> pluginService.buildPluginVersion(pluginManifest));
    }

    @Test
    void parseAndValidateVersion_WithBuild_Error() {
        PluginManifest pluginManifest = new PluginManifest();
        pluginManifest.setVersion("2.1.0-beta.482+d19b165");
        pluginManifest.setPluginId(PLUGIN_ID);

        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(), eq(2)))
                .thenReturn(Flux.just(new PluginVersion().setMajor(2).setMinor(1).setPatch(0)));

        assertThrows(IllegalArgumentException.class, () -> pluginService.buildPluginVersion(pluginManifest));
    }

    @Test
    void parseAndValidateVersion_WithBuild_Success() throws VersionParserFault, PluginPublishException {
        PluginManifest pluginManifest = new PluginManifest();
        pluginManifest.setVersion("2.1.0-beta.482+d19b165");
        pluginManifest.setPluginId(PLUGIN_ID);
        pluginManifest.setConfigurationSchema("");

        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(), eq(2)))
                .thenReturn(Flux.just(new PluginVersion().setMajor(2).setMinor(0).setPatch(0)));
        when(pluginGateway.fetchPluginManifestByIdVersion(any(), anyString())).thenReturn(Mono.just(pluginManifest));

        PluginVersion result = pluginService.buildPluginVersion(pluginManifest);

        assertEquals(2, result.getMajor());
        assertEquals(1, result.getMinor());
        assertEquals(0, result.getPatch());
        assertEquals("beta.482", result.getPreRelease());
        assertEquals("d19b165", result.getBuild());
        assertEquals(PLUGIN_ID, result.getPluginId());
        assertTrue(result.getReleaseDate() > 0);
    }

    @Test
    void createPluginSummary_nullNameNullType() {
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            pluginService.createPluginSummary(null, null, null, account).block();
        });

        assertTrue(t.getMessage().contains("name is required"));
        verify(pluginGateway, never()).persistSummary(any(PluginSummary.class));
    }

    @Test
    void createPluginSummary_nullType() {
        when(pluginPermissionService.saveAccountPermission(eq(account.getId()), any(), eq(PermissionLevel.OWNER))).thenReturn(Flux.empty());

        PluginSummary pluginSummary = pluginService.createPluginSummary(name, null,null, account).block();
        assertNotNull(pluginSummary);
        assertEquals(name, pluginSummary.getName());
        assertNull(pluginSummary.getType());
        verify(pluginGateway, atLeastOnce()).persistSummary(pluginSummary);

        verify(pluginPermissionService, atLeastOnce()).saveAccountPermission(
                account.getId(),
                pluginSummary.getId(),
                PermissionLevel.OWNER
        );
    }

    @Test
    void createPluginSummary_success() {
        when(pluginPermissionService.saveAccountPermission(eq(account.getId()), any(), eq(PermissionLevel.OWNER))).thenReturn(Flux.empty());

        PluginSummary pluginSummary = pluginService.createPluginSummary(name, type,  PublishMode.DEFAULT, account).block();
        assertNotNull(pluginSummary);
        assertEquals(name, pluginSummary.getName());
        assertEquals(type, pluginSummary.getType());
        verify(pluginGateway, atLeastOnce()).persistSummary(pluginSummary);

        verify(pluginPermissionService, atLeastOnce()).saveAccountPermission(
                account.getId(),
                pluginSummary.getId(),
                PermissionLevel.OWNER
        );
    }

    @Test
    void createPluginSummary_withIdSuccess() {

        when(pluginPermissionService.saveAccountPermission(eq(account.getId()), any(), eq(PermissionLevel.OWNER))).thenReturn(Flux.empty());
        when(pluginGateway.fetchPluginSummaryById(eq(pluginId))).thenReturn(Mono.empty());

        PluginSummary pluginSummary = pluginService.createPluginSummary(name, type, pluginId, PublishMode.DEFAULT, account).block();
        assertNotNull(pluginSummary);
        assertEquals(name, pluginSummary.getName());
        assertEquals(type, pluginSummary.getType());
        assertEquals(pluginId, pluginSummary.getId());

        verify(pluginGateway).fetchPluginSummaryById(pluginId);
        verify(pluginGateway, atLeastOnce()).persistSummary(pluginSummary);
        verify(pluginPermissionService, atLeastOnce()).saveAccountPermission(
                account.getId(),
                pluginSummary.getId(),
                PermissionLevel.OWNER
        );
    }

    @Test
    void createPluginSummary_withIdConflic() {

        when(pluginPermissionService.saveAccountPermission(eq(account.getId()), any(), eq(PermissionLevel.OWNER))).thenReturn(Flux.empty());
        when(pluginGateway.fetchPluginSummaryById(eq(pluginId))).thenReturn(Mono.just(new PluginSummary()));

        assertThrows(PluginAlreadyExistsFault.class, () -> pluginService.createPluginSummary(name, type, pluginId, PublishMode.DEFAULT, account).block());
        verify(pluginGateway).fetchPluginSummaryById(pluginId);
        verify(pluginGateway, never()).persistSummary(pluginSummary);
        verify(pluginPermissionService, never()).saveAccountPermission(
                account.getId(),
                pluginSummary.getId(),
                PermissionLevel.OWNER
        );
    }

    @Test
    void publish_notAZipFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        File pluginNotAZip = load(classLoader, pluginNotAZipFileName);

        assertNotNull(pluginNotAZip);
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            try (FileInputStream fileInputStream = new FileInputStream(pluginNotAZip)) {
                pluginService.publish(fileInputStream, pluginNotAZipFileName, publisherId, null);
            }
        });

        assertEquals("zip format required", t.getMessage());
    }

    @Test
    void publish_pluginDoesNotExist() throws IOException, PluginPublishException {
        ClassLoader classLoader = getClass().getClassLoader();
        File plugin = load(classLoader, pluginFileName);
        when(pluginGateway.fetchPluginSummaryById(any(UUID.class))).thenReturn(Mono.empty());
        when(pluginParserService.parse(any(),any(),anyString(),any())).thenReturn(getPluginPublishFields());

        assertNotNull(plugin);
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            try (FileInputStream fileInputStream = new FileInputStream(plugin)) {
                pluginService.publish(fileInputStream, pluginFileName, publisherId, null);
            }
        });

        assertEquals("Plugin summary not found for id 93e2771f-6e4a-47d2-921c-ee6ab3ddb6aa", t.getMessage());
    }

    @Test
    void publish_noPermission() throws IOException, PluginPublishException {
        ClassLoader classLoader = getClass().getClassLoader();
        File plugin = load(classLoader, pluginFileName);
        PluginSummary pluginSummary = mock(PluginSummary.class);
        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(UUID.class), anyInt())).thenReturn(Flux.empty());
        when(pluginGateway.fetchAllPluginVersionsById(any())).thenReturn(Flux.just(new PluginVersion()
                                                                                           .setMajor(2)
                                                                                           .setMinor(0)
                                                                                           .setPatch(0)));
        when(pluginGateway.fetchPluginSummaryById(any(UUID.class))).thenReturn(Mono.just(pluginSummary));
        when(pluginParserService.parse(any(),any(),anyString(),any())).thenReturn(getPluginPublishFields());
        when(pluginPermissionService.findHighestPermissionLevel(any(), any())).thenReturn(Mono.empty());

        assertNotNull(plugin);
        Throwable t = assertThrows(NotAllowedException.class, () -> {
            try (FileInputStream fileInputStream = new FileInputStream(plugin)) {
                pluginService.publish(fileInputStream, pluginFileName, publisherId, null);
            }
        });

        assertTrue(t.getMessage().contains("User must have valid permission"));
    }

    @Test
    void publish_notAllowedInDefaultMode() throws IOException, PluginPublishException {
        ClassLoader classLoader = getClass().getClassLoader();
        File plugin = load(classLoader, pluginFileName);
        PluginSummary pluginSummary = mock(PluginSummary.class);
        when(pluginSummary.getPublishMode()).thenReturn(PublishMode.DEFAULT);
        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(UUID.class), anyInt())).thenReturn(Flux.empty());
        when(pluginGateway.fetchAllPluginVersionsById(any())).thenReturn(Flux.just(new PluginVersion()
                                                                                           .setMajor(2)
                                                                                           .setMinor(0)
                                                                                           .setPatch(0)));
        when(pluginGateway.fetchPluginManifestByIdVersion(any(), anyString())).thenReturn(Mono.just(new PluginManifest()));
        when(pluginGateway.fetchPluginSummaryById(any(UUID.class))).thenReturn(Mono.just(pluginSummary));
        when(pluginParserService.parse(any(),any(),anyString(),any())).thenReturn(getPluginPublishFields());
        when(pluginPermissionService.findHighestPermissionLevel(any(), any())).thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertNotNull(plugin);
        Throwable t = assertThrows(NotAllowedException.class, () -> {
            try (FileInputStream fileInputStream = new FileInputStream(plugin)) {
                pluginService.publish(fileInputStream, pluginFileName, publisherId, null);
            }
        });

        assertTrue(t.getMessage().contains("higher permission level can publish a new version in DEFAULT mode"));
    }

    @Test
    void publish_notAllowedInStrictMode() throws IOException, PluginPublishException {
        ClassLoader classLoader = getClass().getClassLoader();
        File plugin = load(classLoader, pluginFileName);
        PluginSummary pluginSummary = mock(PluginSummary.class);
        when(pluginSummary.getPublishMode()).thenReturn(PublishMode.STRICT);
        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(UUID.class), anyInt())).thenReturn(Flux.empty());
        when(pluginGateway.fetchAllPluginVersionsById(any())).thenReturn(Flux.just(new PluginVersion()
                                                                                           .setMajor(2)
                                                                                           .setMinor(0)
                                                                                           .setPatch(0)));
        when(pluginGateway.fetchPluginManifestByIdVersion(any(), anyString())).thenReturn(Mono.just(new PluginManifest()));
        when(pluginGateway.fetchPluginSummaryById(any(UUID.class))).thenReturn(Mono.just(pluginSummary));
        when(pluginParserService.parse(any(),any(),anyString(),any())).thenReturn(getPluginPublishFields());
        when(pluginPermissionService.findHighestPermissionLevel(any(), any())).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertNotNull(plugin);
        Throwable t = assertThrows(NotAllowedException.class, () -> {
            try (FileInputStream fileInputStream = new FileInputStream(plugin)) {
                pluginService.publish(fileInputStream, pluginFileName, publisherId, null);
            }
        });

        assertTrue(t.getMessage().contains("owner can publish a new version in STRICT mode"));
    }

    @Test
    void publish_invalidVersion() throws IOException, PluginPublishException {
        ClassLoader classLoader = getClass().getClassLoader();
        File plugin = load(classLoader, pluginFileName);
        PluginVersion pluginVersion = new PluginVersion()
                .setMajor(1)
                .setMinor(2)
                .setPatch(0);
        when(pluginGateway.fetchPluginSummaryById(any(UUID.class))).thenReturn(Mono.just(mock(PluginSummary.class)));
        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(UUID.class), anyInt())).thenReturn(Flux.just(pluginVersion));
        when(pluginParserService.parse(any(),any(),anyString(),any())).thenReturn(getPluginPublishFields());

        assertNotNull(plugin);
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            try (FileInputStream fileInputStream = new FileInputStream(plugin)) {
                pluginService.publish(fileInputStream, pluginFileName, publisherId, null);
            }
        });

        assertTrue(t.getMessage().contains("should be greater than the existing"));
    }

    @Test
    void publish_failToParseEntryPoint() throws IOException, PluginPublishException {
        validateFileExistsOnPublishing(pluginNoViewsFileName, "File `index.js` not found");
    }

    @Test
    void publish_failToUploadToS3() throws S3BucketUploadException, IOException, PluginPublishException {
        ClassLoader classLoader = getClass().getClassLoader();
        final File plugin = load(classLoader, pluginFileName);

        PluginSummary pluginSummary = mock(PluginSummary.class);
        when(pluginSummary.getPublishMode()).thenReturn(PublishMode.DEFAULT);

        when(pluginGateway.fetchPluginSummaryById(any(UUID.class))).thenReturn(Mono.just(pluginSummary));
        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(UUID.class), anyInt())).thenReturn(Flux.empty());
        when(pluginGateway.fetchAllPluginVersionsById(any())).thenReturn(Flux.just(new PluginVersion()
                .setMajor(2)
                .setMinor(0)
                .setPatch(0)));
        when(pluginGateway.fetchPluginManifestByIdVersion(any(), anyString())).thenReturn(Mono.just(new PluginManifest()));
        when(pluginParserService.parse(any(),any(),anyString(),any())).thenReturn(getPluginPublishFields());

        doThrow(S3BucketUploadException.class).when(s3Bucket).uploadPlugin(any(PluginManifest.class), any(File.class), any());

        assertNotNull(plugin);
        assertThrows(PluginPublishException.class, () -> {
            try (FileInputStream fileInputStream = new FileInputStream(plugin)) {
                pluginService.publish(fileInputStream, pluginFileName, publisherId, null);
            }
        });
    }

    @Test
    void publish_deletedPlugin() throws IOException, PluginPublishException {
        ClassLoader classLoader = getClass().getClassLoader();
        File plugin = load(classLoader, pluginFileName);
        when(pluginGateway.fetchPluginSummaryById(any())).thenReturn(Mono.just(new PluginSummary().setDeletedId(UUID.randomUUID())));
        when(pluginParserService.parse(any(), any(), anyString(), any())).thenReturn(getPluginPublishFields());
        assertNotNull(plugin);
        IllegalArgumentException t = assertThrows(IllegalArgumentException.class, () -> {
            try (FileInputStream fileInputStream = new FileInputStream(plugin)) {
                pluginService.publish(fileInputStream, pluginFileName, publisherId, null);
            }
        });

        assertTrue(t.getMessage().startsWith("Plugin summary not found for id "));
    }

    @Test
    void publish_success_no_images() throws PluginPublishException, S3BucketUploadException, IOException {
        PublishedPlugin publishedPlugin = successUpload(pluginFileName, new PluginSummary());
        assertTrue(publishedPlugin.getPluginManifest().getScreenshots().isEmpty());
        assertNotNull(publishedPlugin.getPluginManifest().getThumbnail());
    }

    @Test
    void publish_success_withThumbnailAndScreenshots() throws IOException, PluginPublishException, S3BucketUploadException {
        PublishedPlugin publishedPlugin = successUpload(pluginThumbnailAndScreenshotExists ,new PluginSummary());
        assertTrue(publishedPlugin.getPluginManifest().getScreenshots().isEmpty());
        assertNotNull(publishedPlugin.getPluginManifest().getThumbnail());
        ArgumentCaptor<PluginSummary> pluginSummaryCaptor = ArgumentCaptor.forClass(PluginSummary.class);
        verify(pluginGateway).persistSummary(pluginSummaryCaptor.capture());

        PluginSummary capturedPluginSummary = pluginSummaryCaptor.getValue();

        assertNotNull(capturedPluginSummary);
        assertTrue(capturedPluginSummary.getThumbnail().contains(distributionPublicUrl));
    }

    @Test
    void publish_success_latestVersionUpdated() throws PluginPublishException, IOException, S3BucketUploadException {
        final String expectedVersion = "1.2.0"; //version from zip file

        successUpload(pluginFileName, new PluginSummary());

        ArgumentCaptor<PluginSummary> pluginSummaryCaptor = ArgumentCaptor.forClass(PluginSummary.class);
        verify(pluginGateway).persistSummary(pluginSummaryCaptor.capture());
        assertEquals(expectedVersion, pluginSummaryCaptor.getValue().getLatestVersion());
    }

    @Test
    void publish_success_latestVersionNotUpdated() throws PluginPublishException, IOException, S3BucketUploadException {
        final String expectedVersion = "2.0.0"; //existing latest version

        successUpload(pluginFileName, new PluginSummary().setLatestVersion(expectedVersion));

        ArgumentCaptor<PluginSummary> pluginSummaryCaptor = ArgumentCaptor.forClass(PluginSummary.class);
        verify(pluginGateway).persistSummary(pluginSummaryCaptor.capture());
        assertEquals(expectedVersion, pluginSummaryCaptor.getValue().getLatestVersion());
    }

    @Test
    void publish_success_pluginSummaryHasType() throws PluginPublishException, IOException, S3BucketUploadException {
        PluginSummary pluginSummary = mock(PluginSummary.class);
        when(pluginSummary.getType()).thenReturn(PluginType.COMPONENT);
        when(pluginSummary.getPublishMode()).thenReturn(PublishMode.DEFAULT);

        successUpload(pluginFileName, pluginSummary);

        verify(pluginSummary, never()).setType(PluginType.COMPONENT);
    }

    @Test
    void publish_success_pluginSummaryHasNullType() throws PluginPublishException, IOException, S3BucketUploadException {
        PluginSummary pluginSummary = mock(PluginSummary.class);
        when(pluginSummary.getType()).thenReturn(null);
        when(pluginSummary.getPublishMode()).thenReturn(PublishMode.DEFAULT);

        successUpload(pluginFileName, pluginSummary);

        verify(pluginSummary).setType(PluginType.COMPONENT);
        verify(pluginGateway).persistSummary(pluginSummary);
    }

    @Test
    void publish_success_with_tags() throws PluginPublishException, IOException, S3BucketUploadException {
        when(pluginSummary.getLatestVersion()).thenReturn(null);
        PublishedPlugin publishedPlugin = successUpload(pluginFileNameWithTags, new PluginSummary().setId(PLUGIN_ID));
        ArgumentCaptor<PluginSummary> pluginSummaryCaptor = ArgumentCaptor.forClass(PluginSummary.class);
        verify(pluginGateway).persistSummary(pluginSummaryCaptor.capture());
        PluginManifest manifest = publishedPlugin.getPluginManifest();

        assertNotNull(manifest);

        PluginSummary capturedPluginSummary = pluginSummaryCaptor.getValue();

        assertNotNull(capturedPluginSummary);
        assertEquals(manifest.getTags(), capturedPluginSummary.getTags());
        assertNotNull(manifest.getTags());
        assertEquals(2, manifest.getTags().size());
        assertEquals("smart", manifest.getTags().get(0));
        assertEquals("sparrow", manifest.getTags().get(1));
    }

    @Test
    void publish_success_pluginTypeMismatch() throws IOException, PluginPublishException {
        PluginSummary pluginSummary = mock(PluginSummary.class);
        when(pluginSummary.getType()).thenReturn(PluginType.PATHWAY);
        when(pluginSummary.getPublishMode()).thenReturn(PublishMode.DEFAULT);

        ClassLoader classLoader = getClass().getClassLoader();
        File plugin = load(classLoader, pluginFileName);
        when(pluginGateway.fetchPluginSummaryById(any(UUID.class))).thenReturn(Mono.just(pluginSummary));
        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(UUID.class), anyInt())).thenReturn(Flux.empty());

        when(pluginGateway.persistVersion(any(PluginVersion.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.persistManifest(any(PluginManifest.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.persistView(any(ManifestView.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginParserService.parse(any(),any(),anyString(),any())).thenReturn(getPluginPublishFields());

        assertNotNull(plugin);
        try (FileInputStream fileInputStream = new FileInputStream(plugin)) {
              Throwable t = assertThrows(IllegalArgumentException.class, ()-> pluginService
                      .publish(fileInputStream, pluginFileName, publisherId, null));

            System.out.println(t.getMessage());
            assertEquals("Plugin type must be the same. Found `component` expected `pathway`.", t.getMessage());
        }
    }

    @Test
    void publish_setOutputSchema() throws Exception {
        when(pluginSchemaParser.extractOutputSchema(any())).thenReturn("outputSchema");

        successUpload(pluginFileName, new PluginSummary());

        ArgumentCaptor<PluginManifest> pluginManifestCaptor = ArgumentCaptor.forClass(PluginManifest.class);
        verify(pluginGateway).persistManifest(pluginManifestCaptor.capture());
        assertEquals("outputSchema", pluginManifestCaptor.getValue().getOutputSchema());
    }

    @Test
    void publish_setOutputSchema_fault() {
        when(pluginSchemaParser.extractOutputSchema(any())).thenThrow(PluginSchemaParserFault.class);

        assertThrows(PluginSchemaParserFault.class, () -> successUpload(pluginFileName, new PluginSummary()));
    }

    private PublishedPlugin successUpload(String pluginThumbnailAndScreenshotExists, PluginSummary pluginSummary)
            throws PluginPublishException, IOException, S3BucketUploadException {
        return successUpload(pluginThumbnailAndScreenshotExists, pluginSummary, null);
    }

    private PublishedPlugin successUpload(String pluginThumbnailAndScreenshotExists, PluginSummary pluginSummary, UUID pluginId)
            throws IOException, PluginPublishException, S3BucketUploadException {
        ClassLoader classLoader = getClass().getClassLoader();
        File plugin = load(classLoader, pluginThumbnailAndScreenshotExists);
        when(pluginGateway.fetchPluginSummaryById(any(UUID.class))).thenReturn(Mono.just(pluginSummary));
        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(UUID.class), anyInt())).thenReturn(Flux.empty());
        when(pluginGateway.fetchAllPluginVersionsById(any())).thenReturn(Flux.just(new PluginVersion()
                .setMajor(2)
                .setMinor(0)
                .setPatch(0)));
        when(pluginGateway.fetchPluginManifestByIdVersion(any(), anyString())).thenReturn(Mono.just(new PluginManifest()));

        when(pluginGateway.persistVersion(any(PluginVersion.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.persistManifest(any(PluginManifest.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.persistView(any(ManifestView.class))).thenReturn(Flux.just(new Void[]{}));
        PluginParsedFields pluginPublish = getPluginPublishFields();
        if(pluginThumbnailAndScreenshotExists != null && pluginThumbnailAndScreenshotExists.equals("eText_plugin_success.zip")){
            when(pluginParserService.parse(any(),any(),anyString(),any())).thenReturn(getPluginPublishFieldseText());
        }else{
            when(pluginParserService.parse(any(),any(),anyString(),any())).thenReturn(pluginPublish);
        }

        when(pluginPermissionService.findHighestPermissionLevel(any(),any())).thenReturn(Mono.just(PermissionLevel.OWNER));

        assertNotNull(plugin);
        try (FileInputStream fileInputStream = new FileInputStream(plugin)) {
            PublishedPlugin publishedPlugin = pluginService.publish(fileInputStream, pluginFileName, publisherId, pluginId);
            assertNotNull(publishedPlugin);
            verify(s3Bucket, atLeastOnce()).uploadPlugin(any(PluginManifest.class), any(File.class), any());
            verify(pluginGateway, atLeastOnce()).persistSummary(any(PluginSummary.class));
            return publishedPlugin;
        }
    }

    private File load(ClassLoader classLoader, String name) {
        URL url = classLoader.getResource(name);
        if (url != null) {
            return new File(url.getFile());
        }
        return null;
    }

    private void validateFileExistsOnPublishing(String filePath, String error) throws IOException, PluginPublishException {
        ClassLoader classLoader = getClass().getClassLoader();
        final File file = load(classLoader, filePath);
        when(pluginGateway.fetchPluginSummaryById(any(UUID.class))).thenReturn(Mono.just(mock(PluginSummary.class)));
        when(pluginParserService.parse(any(),any(),anyString(),any())).thenReturn(getPluginPublishFields());

        assert file != null;
        Throwable t = assertThrows(IllegalArgumentException.class, () -> {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                pluginService.publish(fileInputStream, filePath, publisherId, null);
            }
        });

        assertTrue(t.getMessage().contains(error));
    }

    @Test
    void getPluginVersions_noPluginId() {
        assertThrows(NullPointerException.class, () -> pluginService.getPluginVersions(null));
    }

    @Test
    void getPluginVersions() throws IOException, PluginPublishException {
        PluginVersion version1 = mock(PluginVersion.class);
        PluginVersion version2 = mock(PluginVersion.class);
        when(pluginGateway.fetchAllPluginVersionsById(eq(PLUGIN_ID))).thenReturn(Flux.just(version1, version2));
        when(pluginParserService.parse(any(),any(),anyString(),any())).thenReturn(getPluginPublishFields());

        StepVerifier
                .create(pluginService.getPluginVersions(PLUGIN_ID))
                .expectNext(version1)
                .expectNext(version2)
                .verifyComplete();
    }

    @Test
    void isUpdateLatestVersion() throws VersionParserFault {
        assertTrue(pluginService.isUpdateLatestVersion(null, "1.0.0-beta"));
        assertTrue(pluginService.isUpdateLatestVersion("", "0.0.0"));

        assertTrue(pluginService.isUpdateLatestVersion("1.0.0-beta", "1.1.1-beta"));
        assertFalse(pluginService.isUpdateLatestVersion("1.1.1-beta", "1.0.0-beta"));

        assertTrue(pluginService.isUpdateLatestVersion("2.0.0-beta", "1.1.1"));
        assertFalse(pluginService.isUpdateLatestVersion("1.1.1", "2.0.0-beta"));

        assertFalse(pluginService.isUpdateLatestVersion("2.0.0-beta", "2.0.0-alpha+34"));
        assertFalse(pluginService.isUpdateLatestVersion("2.0.0-alpha", "2.0.0-alpha+34"));

        assertTrue(pluginService.isUpdateLatestVersion("2.0.0", "2.0.1"));
        assertFalse(pluginService.isUpdateLatestVersion("2.0.1", "2.0.0"));
    }

    @Test
    void findCollaborators() {
        PluginAccountCollaborator col1 = mock(PluginAccountCollaborator.class);
        when(col1.getAccountId()).thenReturn(UUID.randomUUID());
        PluginAccountCollaborator col2 = mock(PluginAccountCollaborator.class);
        when(col2.getAccountId()).thenReturn(UUID.randomUUID());
        PluginAccountCollaborator col3 = mock(PluginAccountCollaborator.class);
        when(col3.getAccountId()).thenReturn(UUID.randomUUID());
        when(pluginAccessGateway.fetchAccounts(eq(PLUGIN_ID))).thenReturn(Flux.just(col1, col2, col3));

        StepVerifier
                .create(pluginService.findAccountCollaborators(PLUGIN_ID))
                .expectNext(col1)
                .expectNext(col2)
                .expectNext(col3)
                .verifyComplete();
    }

    @Test
    void fetchPlugins_byAccount() {
        final UUID accountId = UUID.randomUUID();
        final UUID pluginId1 = UUID.randomUUID();
        final UUID pluginId2 = UUID.randomUUID();
        PluginSummary plugin1 = new PluginSummary().setId(pluginId1);
        PluginSummary plugin2 = new PluginSummary().setId(pluginId2);

        when(pluginAccessGateway.fetchPluginsByAccount(eq(accountId))).thenReturn(Flux.just(
                new PluginAccount().setPluginId(pluginId1),
                new PluginAccount().setPluginId(pluginId2)
        ));
        when(pluginGateway.fetchPluginSummaryById(eq(pluginId1))).thenReturn(Mono.just(plugin1));
        when(pluginGateway.fetchPluginSummaryById(eq(pluginId2))).thenReturn(Mono.just(plugin2));

        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.empty());

        StepVerifier
                .create(pluginService.fetchPlugins(accountId))
                .expectNext(plugin1)
                .expectNext(plugin2)
                .verifyComplete();
    }

    @Test
    void fetchPlugins_byAccount_noPlugins() {
        final UUID accountId = UUID.randomUUID();

        when(pluginAccessGateway.fetchPluginsByAccount(eq(accountId))).thenReturn(Flux.empty());
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.empty());

        StepVerifier
                .create(pluginService.fetchPlugins(accountId))
                .verifyComplete();
    }

    @Test
    void fetchPublishedPlugins_withType() {
        UUID pluginIdOne = UUID.randomUUID();

        PluginSummary summaryOne = mock(PluginSummary.class);
        when(summaryOne.getType()).thenReturn(type);
        when(summaryOne.getLatestVersion()).thenReturn("1.0.0");

        when(pluginAccessGateway.fetchPluginsByAccount(account.getId()))
                .thenReturn(Flux.just(
                        pluginAccountFor(pluginIdOne)));

        when(pluginGateway.fetchPluginSummaryById(pluginIdOne)).thenReturn(Mono.just(summaryOne));
        when(teamService.findTeamsForAccount(account.getId())).thenReturn(Flux.empty());

        List<PluginSummary> result = pluginService.fetchPublishedPlugins(account.getId(), type)
                .collectList().block();

        assert result != null;
        assertEquals(1, result.size());

        verify(pluginAccessGateway, atLeastOnce()).fetchPluginsByAccount(account.getId());
    }

    @Test
    void fetchPublishedPlugins_noType() {
        UUID pluginIdOne = UUID.randomUUID();
        UUID pluginIdTwo = UUID.randomUUID();

        PluginSummary summaryOne = mock(PluginSummary.class);
        when(summaryOne.getType()).thenReturn(PluginType.COMPONENT);
        when(summaryOne.getLatestVersion()).thenReturn("1.0.0");
        PluginSummary summaryTwo = mock(PluginSummary.class);
        when(summaryTwo.getType()).thenReturn(PluginType.COURSE);
        when(summaryTwo.getLatestVersion()).thenReturn("2.0.0");

        when(pluginAccessGateway.fetchPluginsByAccount(account.getId()))
                .thenReturn(Flux.just(pluginAccountFor(pluginIdOne), pluginAccountFor(pluginIdTwo)));

        when(pluginGateway.fetchPluginSummaryById(pluginIdOne)).thenReturn(Mono.just(summaryOne));
        when(pluginGateway.fetchPluginSummaryById(pluginIdTwo)).thenReturn(Mono.just(summaryTwo));
        when(teamService.findTeamsForAccount(account.getId())).thenReturn(Flux.empty());

        List<PluginSummary> result = pluginService.fetchPublishedPlugins(account.getId(), null)
                .collectList().block();

        assert result != null;
        assertEquals(2, result.size());

        verify(pluginAccessGateway, atLeastOnce()).fetchPluginsByAccount(account.getId());
    }

    private PluginAccount pluginAccountFor(UUID pluginId) {
        return new PluginAccount()
                .setPluginId(pluginId);
    }

    @Test
    void findPluginInfo_noPlugin() {
        assertThrows(IllegalArgumentException.class, () -> pluginService.findPluginInfo(null, null));
    }

    @Test
    void findPluginInfo_anyVersion() {
        when(pluginSummary.getName()).thenReturn("someName");

        PluginManifest manifest = new PluginManifest();
        manifest.setPluginId(PLUGIN_ID);
        manifest.setVersion(LATEST_VERSION);
        manifest.setThumbnail("thumbnail.jpg");

        when(pluginGateway.fetchPluginManifestByIdVersion(eq(PLUGIN_ID), eq(LATEST_VERSION))).thenReturn(Mono.just(manifest));
        when(pluginConfigProvider.get()).thenReturn(new PluginConfig().setDistributionPublicUrl("publicUrl"));

        PluginPayload result = pluginService.findPluginInfo(PLUGIN_ID, null).block();

        assertNotNull(result);
        assertNotNull(result.getPluginSummaryPayload());
        assertEquals(manifest, result.getManifest());
        assertTrue(result.getEntryPoints().isEmpty());
    }

    @Test
    void findPluginInfo_noVersions() {
        when(pluginSummary.getName()).thenReturn("someName");
        when(pluginSummary.isPublished()).thenReturn(false);

        when(pluginGateway.fetchAllPluginVersionsById(eq(PLUGIN_ID))).thenReturn(Flux.empty());
        PluginPayload result = pluginService.findPluginInfo(PLUGIN_ID, null).block();

        assertNotNull(result);
        assertNull(result.getManifest());
        assertTrue(result.getEntryPoints().isEmpty());
    }

    @Test
    void findPluginInfo_versionNotExist() {
        PluginNotFoundFault t = assertThrows(PluginNotFoundFault.class, () -> pluginService.findPluginInfo(PLUGIN_ID, "3.*").block());
        assertEquals("Version '3.*' for plugin_id='" + PLUGIN_ID + "' does not exist", t.getMessage());
    }

    @Test
    void findPluginInfo_pluginNotExist() {
        when(pluginGateway.fetchPluginSummaryById(eq(PLUGIN_ID))).thenReturn(Mono.empty());

        PluginNotFoundFault t = assertThrows(PluginNotFoundFault.class, () -> pluginService.findPluginInfo(PLUGIN_ID, "").block());
        assertEquals("Plugin with id '" + PLUGIN_ID + "' is not found", t.getMessage());
    }

    @Test
    void findPluginInfo_unParsedVersion() {
        VersionParserFault t = assertThrows(VersionParserFault.class, () -> pluginService.findPluginInfo(PLUGIN_ID, "1*").block());
        assertEquals("Can not parse version '1*'", t.getMessage());
    }

    @Test
    void findPluginInfo_updateUrls() {
        when(pluginGateway.fetchPluginManifestByIdVersion(eq(PLUGIN_ID), eq(LATEST_VERSION))).thenReturn(Mono.just(
                new PluginManifest()
                        .setPluginId(PLUGIN_ID)
                        .setZipHash(ZIP_HASH)
                        .setScreenshots(Sets.newHashSet("screenshot1.png"))
                        .setThumbnail("thumbnail.png"))
        );
        when(pluginConfigProvider.get()).thenReturn(new PluginConfig().setDistributionPublicUrl("publicUrl"));

        PluginPayload result = pluginService.findPluginInfo(PLUGIN_ID, null).block();

        assertNotNull(result);
        assertEquals("publicUrl/" + PLUGIN_ID + "/" + ZIP_HASH + "/screenshot1.png", result.getManifest().getScreenshots().toArray()[0]);
        assertEquals("publicUrl/" + PLUGIN_ID + "/" + ZIP_HASH + "/thumbnail.png", result.getManifest().getThumbnail());
    }

    @Test
    void findPluginInfo_manifestNotFound() {
        when(pluginGateway.fetchPluginManifestByIdVersion(eq(PLUGIN_ID), eq(VERSION))).thenReturn(Mono.empty());

        PluginPayload result = pluginService.findPluginInfo(PLUGIN_ID, VERSION).block();

        assertNotNull(result);
        assertNull(result.getManifest());
    }

    @Test
    void fetchPlugins_filterByType_nullType() {
        UUID pluginId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        when(pluginAccessGateway.fetchPluginsByAccount(accountId)).thenReturn(Flux.just(new PluginAccount()
                .setAccountId(accountId)
                .setPluginId(pluginId)));
        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.empty());
        when(pluginGateway.fetchPluginSummaryById(pluginId)).thenReturn(Mono.just(new PluginSummary()
        .setType(null)));

        Flux<PluginSummary> results = pluginService.fetchPlugins(accountId, PluginType.COMPONENT);

        assertTrue(Objects.requireNonNull(results.collectList().block()).isEmpty());
    }

    @Test
    void fetchPlugins_filteredByType() {
        UUID pluginId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.empty());

        when(pluginAccessGateway.fetchPluginsByAccount(accountId)).thenReturn(Flux.just(new PluginAccount()
                .setAccountId(accountId)
                .setPluginId(pluginId)));

        when(pluginGateway.fetchPluginSummaryById(pluginId)).thenReturn(Mono.just(new PluginSummary()
                .setType(PluginType.COMPONENT)));

        Flux<PluginSummary> results = pluginService.fetchPlugins(accountId, PluginType.COMPONENT);

        assertFalse(Objects.requireNonNull(results.collectList().block()).isEmpty());
    }

    @Test
    void fetchPlugins_filteredByTypeMultiple() {
        UUID pluginIdOne = UUID.randomUUID();
        UUID pluginIdTwo = UUID.randomUUID();
        UUID pluginIdThree = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        when(teamService.findTeamsForAccount(accountId)).thenReturn(Flux.empty());

        when(pluginAccessGateway.fetchPluginsByAccount(accountId)).thenReturn(Flux.just(new PluginAccount()
                        .setAccountId(accountId)
                        .setPluginId(pluginIdOne),
                new PluginAccount()
                        .setPluginId(pluginIdTwo)
                        .setAccountId(accountId),
                new PluginAccount()
                        .setAccountId(accountId)
                        .setPluginId(pluginIdThree)));

        when(pluginGateway.fetchPluginSummaryById(pluginIdOne)).thenReturn(Mono.just(new PluginSummary()
                .setType(PluginType.COMPONENT)));
        when(pluginGateway.fetchPluginSummaryById(pluginIdTwo)).thenReturn(Mono.just(new PluginSummary()
                .setType(null)));
        when(pluginGateway.fetchPluginSummaryById(pluginIdThree)).thenReturn(Mono.just(new PluginSummary()
                .setType(PluginType.COURSE)));

        List<PluginSummary> results = pluginService.fetchPlugins(accountId, PluginType.COMPONENT).collectList().block();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(PluginType.COMPONENT, results.get(0).getType());
    }

    @Test
    void fetchPlugins_emptyFromAccount() {
        UUID accountId = UUID.randomUUID();
        UUID teamIdOne = UUID.randomUUID();
        UUID teamIdTwo = UUID.randomUUID();

        UUID pluginIdOne = UUID.randomUUID();
        UUID pluginIdTwo = UUID.randomUUID();
        UUID pluginIdThree = UUID.randomUUID();

        when(pluginAccessGateway.fetchPluginsByAccount(accountId)).thenReturn(Flux.empty());
        when(teamService.findTeamsForAccount(accountId))
                .thenReturn(Flux.just(
                        new TeamAccount().setTeamId(teamIdOne),
                        new TeamAccount().setTeamId(teamIdTwo)
                ));

        when(pluginAccessGateway.fetchPluginsByTeam(teamIdOne)).thenReturn(Flux.just(pluginIdOne, pluginIdThree));
        when(pluginAccessGateway.fetchPluginsByTeam(teamIdTwo)).thenReturn(Flux.just(pluginIdTwo));

        when(pluginGateway.fetchPluginSummaryById(any(UUID.class))).thenReturn(Mono.just(new PluginSummary()));

        List<PluginSummary> results = pluginService.fetchPlugins(accountId).collectList().block();

        assertNotNull(results);
        assertEquals(3, results.size());

    }

    @Test
    void fetchPlugins_fromTeamsAndAccount() {
        UUID accountId = UUID.randomUUID();
        UUID teamIdOne = UUID.randomUUID();
        UUID teamIdTwo = UUID.randomUUID();

        UUID pluginIdOne = UUID.randomUUID();
        UUID pluginIdTwo = UUID.randomUUID();
        UUID pluginIdThree = UUID.randomUUID();
        UUID pluginIdFour = UUID.randomUUID();
        UUID pluginIdFive = UUID.randomUUID();

        when(pluginAccessGateway.fetchPluginsByAccount(accountId))
                .thenReturn(Flux.just(
                        new PluginAccount().setPluginId(pluginIdFour),
                        new PluginAccount().setPluginId(pluginIdFive),
                        new PluginAccount().setPluginId(pluginIdOne) // plugin one is duplicated
                ));

        when(teamService.findTeamsForAccount(accountId))
                .thenReturn(Flux.just(
                        new TeamAccount().setTeamId(teamIdOne),
                        new TeamAccount().setTeamId(teamIdTwo)
                ));

        when(pluginAccessGateway.fetchPluginsByTeam(teamIdOne)).thenReturn(Flux.just(pluginIdOne, pluginIdThree));
        when(pluginAccessGateway.fetchPluginsByTeam(teamIdTwo)).thenReturn(Flux.just(pluginIdTwo));

        when(pluginGateway.fetchPluginSummaryById(any(UUID.class))).thenReturn(Mono.just(new PluginSummary()));

        List<PluginSummary> results = pluginService.fetchPlugins(accountId).collectList().block();

        assertNotNull(results);
        assertEquals(5, results.size());
    }

    @SuppressWarnings("Duplicates")
    @Test
    void getSummaryPayload_creatorNotFound() {
        when(accountService.getAccountPayload(accountId)).thenReturn(Mono.empty());

        PluginSummaryPayload result = pluginService.getPluginSummaryPayload(pluginSummary).block();

        assertNotNull(result);
        assertEquals(PLUGIN_ID, result.getPluginId());
        assertNull(result.getAccountPayload());
    }

    @SuppressWarnings("Duplicates")
    @Test
    void getSummaryPayload_byId_creatorNotFound() {
        when(accountService.getAccountPayload(accountId)).thenReturn(Mono.empty());

        PluginSummaryPayload result = pluginService.getPluginSummaryPayload(PLUGIN_ID).block();

        assertNotNull(result);
        assertEquals(PLUGIN_ID, result.getPluginId());
        assertNull(result.getAccountPayload());
    }

    @Test
    void getSummaryPayload() {

        PluginSummaryPayload result = pluginService.getPluginSummaryPayload(pluginSummary).block();

        assertNotNull(result);
        assertEquals(PLUGIN_ID, result.getPluginId());
        assertEquals(accountId, result.getAccountPayload().getAccountId());
    }

    @Test
    void getSummaryPayload_byId() {

        PluginSummaryPayload result = pluginService.getPluginSummaryPayload(PLUGIN_ID).block();

        assertNotNull(result);
        assertEquals(PLUGIN_ID, result.getPluginId());
        assertEquals(accountId, result.getAccountPayload().getAccountId());
    }

    @Test
    void getSummaryPayload_byId_pluginNotFound() {
        when(pluginGateway.fetchPluginSummaryById(PLUGIN_ID)).thenReturn(Mono.empty());

        PluginNotFoundFault t = assertThrows(PluginNotFoundFault.class, () -> pluginService.getPluginSummaryPayload(PLUGIN_ID).block());

        assertEquals("summary not found for plugin_id='" + PLUGIN_ID + "'", t.getMessage());
    }

    @Test
    void deletePlugin() {
        UUID accountId = UUID.randomUUID();
        when(pluginGateway.fetchPluginSummaryById(PLUGIN_ID)).thenReturn(Mono.just(new PluginSummary()));
        PublisherProbe<Void> deletedInfoProbe = PublisherProbe.empty();
        when(pluginGateway.persist(any(DeletedPlugin.class))).thenReturn(deletedInfoProbe.flux());
        PublisherProbe<Void> accountProbe = PublisherProbe.empty();
        when(pluginPermissionService.deleteAccountPermissions(PLUGIN_ID)).thenReturn(accountProbe.flux());
        PublisherProbe<Void> teamProbe = PublisherProbe.empty();
        when(pluginPermissionService.deleteTeamPermissions(PLUGIN_ID)).thenReturn(teamProbe.flux());

        pluginService.deletePlugin(accountId, PLUGIN_ID).subscribe();

        accountProbe.assertWasSubscribed();
        teamProbe.assertWasSubscribed();
        deletedInfoProbe.assertWasSubscribed();

        ArgumentCaptor<PluginSummary> summaryCaptor = ArgumentCaptor.forClass(PluginSummary.class);
        ArgumentCaptor<DeletedPlugin> deletedCaptor = ArgumentCaptor.forClass(DeletedPlugin.class);
        verify(pluginGateway).persistSummary(summaryCaptor.capture());
        verify(pluginGateway).persist(deletedCaptor.capture());

        assertNotNull(summaryCaptor.getValue().getDeletedId());
        assertEquals(summaryCaptor.getValue().getDeletedId(), deletedCaptor.getValue().getId());
        assertEquals(accountId, deletedCaptor.getValue().getAccountId());
        assertEquals(PLUGIN_ID, deletedCaptor.getValue().getPluginId());
    }

    @Test
    @DisplayName("throws an exception when the plugin summary argument supplied is null")
    void getPluginSummaryPayload_nullPluginSummary() {
        PluginSummary pluginSummary = null;
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, ()-> pluginService.getPluginSummaryPayload(pluginSummary));
        assertEquals("pluginSummary can not be null", e.getMessage());
    }

    @Test
    @DisplayName("it should include thumbnail and tags in the payload when the plugin manifest is found")
    void getPluginSummaryPayload() {
        final String thumbnail = "thumbnail/path";
        final List<String> tags = Lists.newArrayList("smart", "sparrow");
        when(pluginSummary.getThumbnail()).thenReturn(thumbnail);
        when(pluginSummary.getTags()).thenReturn(tags);

        PluginSummaryPayload payload = pluginService.getPluginSummaryPayload(pluginSummary).block();

        assertNotNull(payload);
        assertEquals(thumbnail, payload.getThumbnail());
        assertEquals(tags, payload.getTags());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void SyncPlugin_existing() throws S3BucketLoadFileException, IOException {
        String hash = "I am a hash";
        PluginSummary pluginSummary = new PluginSummary().setId(PLUGIN_ID).setCreatorId(account.getId());
        PluginVersion version = new PluginVersion()
                .setPluginId(PLUGIN_ID)
                .setMajor(1)
                .setMinor(2)
                .setPatch(0);


        when(s3Bucket.getPluginFileContent(eq(PLUGIN_ID), eq(hash), eq("manifest.json"))).thenReturn(manifestContentWithView);
        when(pluginGateway.fetchPluginSummaryById(eq(PLUGIN_ID)))
                .thenReturn(Mono.just(pluginSummary));
        when(pluginGateway.fetchAllPluginVersionsById(eq(PLUGIN_ID))).thenReturn(Flux.just(version));
        when(pluginGateway.persistVersion(any(PluginVersion.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.persistSummary(any(Mono.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.persistManifest(any())).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.persistView(any(Flux.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.fetchPluginManifestByIdVersion(eq(PLUGIN_ID), eq(VERSION))).thenReturn(Mono.empty());
        when(pluginGateway.fetchViews(eq(PLUGIN_ID), eq(VERSION))).thenReturn(Flux.just(new ManifestView()));

        pluginService.syncFromRepo(PLUGIN_ID, hash, account).block();

        verify(s3Bucket).getPluginFileContent(eq(PLUGIN_ID), eq(hash), eq("manifest.json"));
        verify(pluginGateway, times(2)).fetchPluginSummaryById(eq(PLUGIN_ID));
        verify(pluginGateway).persistVersion(any(PluginVersion.class));
        verify(pluginGateway).persistSummary(any(Mono.class));
        verify(pluginGateway).persistManifest(any(PluginManifest.class));
        verify(pluginGateway).persistView(any(Flux.class));
        verify(pluginGateway, times(1)).fetchPluginManifestByIdVersion(eq(PLUGIN_ID), eq(VERSION));
        verify(pluginGateway).fetchViews(eq(PLUGIN_ID), eq(VERSION));
    }

    @Test
    public void SyncPlugin_new() throws S3BucketLoadFileException, IOException {
        String hash = "I am a hash";

        PluginSummary pluginSummary = new PluginSummary().setId(PLUGIN_ID).setCreatorId(account.getId());
        PluginVersion version = new PluginVersion()
                .setPluginId(PLUGIN_ID)
                .setMajor(1)
                .setMinor(2)
                .setPatch(0);

        when(s3Bucket.getPluginFileContent(eq(PLUGIN_ID), eq(hash), eq("manifest.json"))).thenReturn(manifestContentWithView);
        when(pluginGateway.fetchPluginSummaryById(eq(PLUGIN_ID)))
                .thenReturn(Mono.empty()) // return empty on first call to test the creation block
                .thenReturn(Mono.just(pluginSummary));
        when(pluginGateway.fetchAllPluginVersionsById(eq(PLUGIN_ID))).thenReturn(Flux.just(version));
        when(pluginGateway.persistVersion(any(PluginVersion.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.persistSummary(any(Mono.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.persistManifest(any())).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.persistView(any(Flux.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.fetchPluginManifestByIdVersion(eq(PLUGIN_ID), eq(VERSION))).thenReturn(Mono.empty());
        when(pluginGateway.fetchViews(eq(PLUGIN_ID), eq(VERSION))).thenReturn(Flux.just(new ManifestView()));

        pluginService.syncFromRepo(PLUGIN_ID, hash, account).block();

        verify(s3Bucket).getPluginFileContent(eq(PLUGIN_ID), eq(hash), eq("manifest.json"));
        verify(pluginGateway, times(2)).fetchPluginSummaryById(eq(PLUGIN_ID));
        verify(pluginGateway).persistVersion(any(PluginVersion.class));
        verify(pluginGateway).persistSummary(any(Mono.class));
        verify(pluginGateway).persistManifest(any(PluginManifest.class));
        verify(pluginGateway).persistView(any(Flux.class));
        verify(pluginGateway, times(1)).fetchPluginManifestByIdVersion(eq(PLUGIN_ID), eq(VERSION));
        verify(pluginGateway).fetchViews(eq(PLUGIN_ID), eq(VERSION));
    }

    @Test
    void publish_pluginFromPackage() throws IOException, PluginPublishException {
        when(pluginSchemaParser.extractOutputSchema(any())).thenReturn("outputSchema");
        when(pluginGateway.fetchPluginSummaryById(any())).thenReturn(Mono.just(pluginSummary));
        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(UUID.class), anyInt())).thenReturn(Flux.empty());
        when(pluginGateway.persistVersion(any(PluginVersion.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.persistManifest(any(PluginManifest.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.persistView(any(ManifestView.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.persistSummary(any(PluginSummary.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.fetchAllPluginVersionsById(any())).thenReturn(Flux.just(new PluginVersion()
                .setMajor(2)
                .setMinor(0)
                .setPatch(0)));
        when(pluginGateway.fetchPluginManifestByIdVersion(any(), anyString())).thenReturn(Mono.just(new PluginManifest()));
        when(pluginParserService.parse(any(),any(),anyString(),any())).thenReturn(getPluginPublishFields());

        ClassLoader classLoader = getClass().getClassLoader();
        File plugin = load(classLoader, pluginPackageFileName);
        assertNotNull(plugin);
        try (FileInputStream fileInputStream = new FileInputStream(plugin)) {
            PublishedPlugin publishedPlugin = pluginService.publish(fileInputStream, pluginPackageFileName, publisherId, pluginId);

            verify(pluginGateway).fetchPluginSummaryById(any());
            verify(pluginGateway).persistVersion(any(PluginVersion.class));
            verify(pluginGateway).persistManifest(any(PluginManifest.class));
            verify(pluginGateway).persistView(any(ManifestView.class));
            verify(pluginGateway).persistSummary(any(PluginSummary.class));

            assertNotNull(publishedPlugin);
            assertNotNull(publishedPlugin.getManifestView());
            assertEquals(1, publishedPlugin.getManifestView().size());
            assertNotNull(publishedPlugin.getPluginManifest());
            assertEquals("outputSchema", publishedPlugin.getPluginManifest().getOutputSchema());
        } catch (PluginPublishException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get plugin manifest , views and searchable fields from parse method
     * @return PluginPublishFields
     */
    private PluginParsedFields getPluginPublishFields() {
        Map<String, Object> views = new LinkedHashMap<>();
        Map<String,Object> learner= new LinkedHashMap<>();
        learner.put("contentType","javascript");
        learner.put("entryPoint","index.js");
        learner.put("publicDir","");
        views.put("LEARNER", learner);

        return new PluginParsedFields()
                .setPluginManifest(new PluginManifest()
                        .setName("TextInput")
                        .setTags(Arrays.asList("smart","sparrow"))
                        .setVersion("1.2.0")
                        .setType(PluginType.COMPONENT)
                        .setConfigurationSchema(searchableConfigSchema)
                        .setPluginId(UUID.fromString("93e2771f-6e4a-47d2-921c-ee6ab3ddb6aa"))
                        .setPublisherId(UUID.randomUUID())
                        .setZipHash("134")
                        .setScreenshots(new HashSet<>())
                        .setThumbnail("thumbnail"))
                .setSearchableFields(Json.toJsonNode(searchable))
                .setViews(views)
                .setPluginFilters(new ArrayList<>());
    }

    @Test
    public void validateMinorOrPatch_buildPluginVersion() throws PluginPublishException {
        PluginManifest pluginManifest = new PluginManifest();
        pluginManifest.setVersion("2.1.0");
        pluginManifest.setPluginId(PLUGIN_ID);
        pluginManifest.setConfigurationSchema("");

        when(pluginGateway.fetchAllPluginVersionByIdMajor(any(), eq(2))).thenReturn(Flux.just(new PluginVersion()
                .setMajor(2)
                .setMinor(0)
                .setPatch(0)));
        when(pluginGateway.fetchPluginManifestByIdVersion(any(), anyString())).thenReturn(Mono.just(pluginManifest));

        PluginVersion result = pluginService.buildPluginVersion(pluginManifest);

        verify(pluginGateway).fetchAllPluginVersionByIdMajor(any(), eq(2));
        verify(pluginGateway).fetchPluginManifestByIdVersion(any(), anyString());
        verify(schemaValidationService).validateLatestSchemaAgainstManifestSchema(anyString(), anyString());

        assertEquals(2, result.getMajor());
        assertEquals(1, result.getMinor());
        assertEquals(0, result.getPatch());
        assertNull(result.getPreRelease());
        assertNull( result.getBuild());
    }

    @Test
    void createLTIProviderCredentials_invalidArgs() {
        // It should throw when the key is not supplied
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class, () -> pluginService
                .createLTIProviderCredential(null, null, null, null));

        assertEquals("key is required", f1.getMessage());

        // it should throw when the key is an empty string
        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class, () -> pluginService
                .createLTIProviderCredential("", null, null, null));

        assertEquals("key is required", f2.getMessage());

        // it should throw when the secretKey is not supplied
        IllegalArgumentFault f3 = assertThrows(IllegalArgumentFault.class, () -> pluginService
                .createLTIProviderCredential("key", null, null, null));

        assertEquals("secretKey is required", f3.getMessage());

        // it should throw when the secretKey is an empty string
        IllegalArgumentFault f4 = assertThrows(IllegalArgumentFault.class, () -> pluginService
                .createLTIProviderCredential("key", "", null, null));

        assertEquals("secretKey is required", f4.getMessage());

        // it should throw when the plugin id is not supplied
        IllegalArgumentFault f5 = assertThrows(IllegalArgumentFault.class, () -> pluginService
                .createLTIProviderCredential("key", "secretKey", null, null));

        assertEquals("pluginId is required", f5.getMessage());

        // it should throw when the fields has map is not supplied
        IllegalArgumentFault f6 = assertThrows(IllegalArgumentFault.class, () -> pluginService
                .createLTIProviderCredential("key", "secretKey", UUID.randomUUID(), null));

        assertEquals("fields is required", f6.getMessage());

        verify(pluginGateway, never()).persistLTIPluginCredentials(any(LTIProviderCredential.class));
    }

    @Test
    void createLTIProviderCredentials_alreadyExists() {
        final String key = "key";
        when(pluginGateway.fetchLTIProviderCredential(pluginId, key))
                .thenReturn(Mono.just(new LTIProviderCredential()));

        ConflictFault f = assertThrows(ConflictFault.class,
                () -> pluginService.createLTIProviderCredential(key, "secret", pluginId, new HashSet<>())
                        .block());

        assertEquals("lti credentials already exist", f.getMessage());

        verify(pluginGateway, never()).persistLTIPluginCredentials(any(LTIProviderCredential.class));
    }

    @Test
    void createLTIProviderCredentials() {
        ArgumentCaptor<LTIProviderCredential> captor = ArgumentCaptor.forClass(LTIProviderCredential.class);

        final String key = "key";
        final String secretKey = "secretKey";
        final Set<String> fields = Sets.newHashSet("foo", "bar");

        when(pluginGateway.fetchLTIProviderCredential(pluginId, key))
                .thenReturn(Mono.empty());
        when(pluginGateway.persistLTIPluginCredentials(any(LTIProviderCredential.class)))
                .thenReturn(Flux.just(new Void[]{}));


        final LTIProviderCredential credentials = pluginService.createLTIProviderCredential(key, secretKey, pluginId, fields)
                .block();

        assertNotNull(credentials);

        verify(pluginGateway).persistLTIPluginCredentials(captor.capture());

        final LTIProviderCredential persisted = captor.getValue();

        assertNotNull(persisted);
        assertAll(() -> {
            assertNotNull(persisted.getId());
            assertEquals(key, persisted.getKey());
            assertEquals(secretKey, persisted.getSecret());
            assertEquals(pluginId, persisted.getPluginId());
            assertEquals(fields, persisted.getAllowedFields());

            assertEquals(persisted, credentials);
        });
    }

    @Test
    void deleteLTIProviderCredentials_invalidArgs() {
        // it should throw when the key is not supplied
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                () -> pluginService.deleteLTIProviderCredential(null, null));

        assertEquals("key is required", f1.getMessage());

        // it should throw when the key is an empty string
        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class,
                () -> pluginService.deleteLTIProviderCredential("", null));

        assertEquals("key is required", f2.getMessage());

        // it should throw when the plugin id is not supplied
        IllegalArgumentFault f3 = assertThrows(IllegalArgumentFault.class,
                () -> pluginService.deleteLTIProviderCredential("key", null));

        assertEquals("pluginId is required", f3.getMessage());

        verify(pluginGateway, never()).deleteLTIProviderCredential(any(LTIProviderCredential.class));
    }

    @Test
    void deleteLTIProviderCredentials() {
        ArgumentCaptor<LTIProviderCredential> captor = ArgumentCaptor.forClass(LTIProviderCredential.class);
        final String key = "key";
        when(pluginGateway.deleteLTIProviderCredential(any(LTIProviderCredential.class)))
                .thenReturn(Flux.just(new Void[]{}));

        pluginService.deleteLTIProviderCredential(key, pluginId)
                .block();

        verify(pluginGateway).deleteLTIProviderCredential(captor.capture());

        final LTIProviderCredential deleted = captor.getValue();

        assertAll(() -> {
            assertNotNull(deleted);
            assertEquals(pluginId, deleted.getPluginId());
            assertEquals(key, deleted.getKey());
        });
    }

    @Test
    void resolvePluginVersion_flag_enabled() throws VersionParserFault {
        final String wildcard = "1.*";
        final String expected_version = "1.2.0";

        when(pluginGateway.fetchAllPluginVersionsById(WRONG_PLUGIN_ID)).thenReturn(Flux.just(new PluginVersion()
                                                                                                     .setMajor(1)
                                                                                                     .setMinor(2)
                                                                                                     .setPatch(0)));
        String resolvedVersion = pluginService.resolvePluginVersion(PLUGIN_ID, wildcard, true);
        assertNotNull(resolvedVersion);
        assertEquals(expected_version, resolvedVersion);
    }

    @Test
    void resolvePluginVersion_flag_disabled() throws VersionParserFault {
        final String wildcard = "1.*";
        final String expected_version = "1.2.0";

        when(pluginGateway.fetchAllPluginVersionsById(WRONG_PLUGIN_ID)).thenReturn(Flux.just(new PluginVersion()
                .setMajor(1)
                .setMinor(2)
                .setPatch(0)));
        String pluginVersion = pluginService.resolvePluginVersion(PLUGIN_ID, wildcard, false);
        assertNotNull(pluginVersion);
        assertEquals(wildcard, pluginVersion);
    }

    @Test
    void parse_Error_missingConfigSchemaField() throws IOException, PluginPublishException {
        ClassLoader classLoader = getClass().getClassLoader();
        File plugin = load(classLoader, pluginFileName);

        when(pluginParserService.parse(any(),any(),anyString(),any())).thenReturn(new PluginParsedFields()
                .setPluginManifest(new PluginManifest()
                        .setName("TextInput")
                        .setVersion("1.2.0")
                        .setConfigurationSchema(searchableConfigSchema)
                        .setPluginId(UUID.fromString("93e2771f-6e4a-47d2-921c-ee6ab3ddb6aa")))
                .setSearchableFields(Json.toJsonNode(missingContentSearchable)));
        assertNotNull(plugin);
        Throwable t = assertThrows(PluginPublishException.class, () -> {
            try (FileInputStream fileInputStream = new FileInputStream(plugin)) {
                pluginService.publish(fileInputStream, pluginFileName, publisherId, null);
            }
        });

        assertEquals("Searchable object is missing contentType field", t.getMessage());
    }

    @Test
    void searchableField_missing_configSchema_Error() throws IOException, PluginPublishException {


        ClassLoader classLoader = getClass().getClassLoader();
        File plugin = load(classLoader, pluginFileName);

        when(pluginParserService.parse(any(),any(),anyString(),any())).thenReturn(new PluginParsedFields()
                .setPluginManifest(new PluginManifest()
                        .setName("TextInput")
                        .setVersion("1.2.0")
                        .setConfigurationSchema(missingFieldConfigSchema)
                        .setPluginId(UUID.fromString("93e2771f-6e4a-47d2-921c-ee6ab3ddb6aa")))
                .setSearchableFields(Json.toJsonNode(searchable)));
        assertNotNull(plugin);
        Throwable t = assertThrows(PluginPublishException.class, () -> {
            try (FileInputStream fileInputStream = new FileInputStream(plugin)) {
                pluginService.publish(fileInputStream, pluginFileName, publisherId, null);
            }
        });

        assertTrue(t.getMessage().contains("Searchable field doesn't exist in configuration schema"), t.getMessage());
    }

    @Test
    void searchableField_missingType_configSchema_Error() throws IOException, PluginPublishException {

        ClassLoader classLoader = getClass().getClassLoader();
        File plugin = load(classLoader, pluginFileName);

        when(pluginParserService.parse(any(),any(),anyString(),any())).thenReturn(new PluginParsedFields()
                .setPluginManifest(new PluginManifest()
                        .setName("TextInput")
                        .setVersion("1.2.0")
                        .setConfigurationSchema(missingFieldTypeConfigSchema)
                        .setPluginId(UUID.fromString("93e2771f-6e4a-47d2-921c-ee6ab3ddb6aa")))
                .setSearchableFields(Json.toJsonNode(searchable)));
        assertNotNull(plugin);
        Throwable t = assertThrows(PluginPublishException.class, () -> {
            try (FileInputStream fileInputStream = new FileInputStream(plugin)) {
                pluginService.publish(fileInputStream, pluginFileName, publisherId, null);
            }
        });

        assertTrue(t.getMessage().contains("Schema definition of searchable field 'selection' is missing 'type'"), t.getMessage());
    }

    @Test
    void updatePluginSummary_success(){
        when(pluginGateway.updatePluginSummary(any(PluginSummary.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.fetchPluginSummaryById(any())).thenReturn(Mono.just(new PluginSummary()
        .setId(pluginId)
        .setPublishMode(PublishMode.STRICT)));

        PluginSummary pluginSummaryMono = pluginService.updatePluginSummary(pluginId, PublishMode.STRICT).block();

        assertNotNull(pluginSummaryMono);
        assertEquals(PublishMode.STRICT.name(), pluginSummaryMono.getPublishMode().toString());
        ArgumentCaptor<PluginSummary> pluginSummaryCaptor = ArgumentCaptor.forClass(PluginSummary.class);

        assertNotNull(pluginSummaryCaptor);
        verify(pluginGateway).updatePluginSummary(pluginSummaryCaptor.capture());
        verify(pluginGateway).fetchPluginSummaryById(pluginSummaryCaptor.getValue().getId());
        assertEquals(pluginId, pluginSummaryCaptor.getValue().getId());
        assertEquals(PublishMode.STRICT, pluginSummaryCaptor.getValue().getPublishMode());


    }

    @Test
    void test_unPublishPluginVersion_missing_required(){
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                () -> pluginService.unPublishPluginVersion(null, null, null, null));

        assertEquals("pluginId is required", f1.getMessage());

        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class,
                () -> pluginService.unPublishPluginVersion(pluginId, null, null, null));

        assertEquals("major version is required", f2.getMessage());

        IllegalArgumentFault f3 = assertThrows(IllegalArgumentFault.class,
                () -> pluginService.unPublishPluginVersion(pluginId, major, null, null));

        assertEquals("minor version is required", f3.getMessage());

        IllegalArgumentFault f4 = assertThrows(IllegalArgumentFault.class,
                () -> pluginService.unPublishPluginVersion(pluginId, major, minor, null));

        assertEquals("patch version is required", f4.getMessage());
    }

    @Test
    void test_unPublishPluginVersion_valid_notlatest(){

        when(pluginGateway
                .fetchAllPluginVersionByIdMajorMinorPatch(any(UUID.class),
                        any(Integer.class),
                        any(Integer.class),
                        any(Integer.class)))
                .thenReturn(Flux.just(pluginVersion1,pluginVersion2));
        when(pluginGateway.unPublishPluginVersion(any(PluginVersion.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.fetchPluginSummaryById(pluginId)).thenReturn(Mono.just(pluginSummary));
        pluginService.unPublishPluginVersion(pluginId, major, minor, patch);
    }

    @Test
    void test_unPublishPluginVersion_valid_latest() {
        UUID creatorId = UUID.randomUUID();
        PluginSummary pluginSummary = new PluginSummary()
                .setId(pluginId)
                .setLatestVersion("2.0.0")
                .setCreatorId(creatorId)
                .setDescription("Some screen plugin")
                .setName("Screen Plugin");

        PluginVersion pluginVersion = new PluginVersion()
                .setPluginId(pluginId)
                .setBuild("123")
                .setMajor(2)
                .setMinor(0)
                .setPatch(0)
                .setPreRelease("alpha")
                .setUnpublished(false);

        when(pluginGateway
                .fetchAllPluginVersionByIdMajorMinorPatch(any(UUID.class),
                        any(Integer.class),
                        any(Integer.class),
                        any(Integer.class)))
                .thenReturn(Flux.just(pluginVersion));
        when(pluginGateway.unPublishPluginVersion(any(PluginVersion.class))).thenReturn(Flux.just(new Void[]{}));
        when(pluginGateway.fetchPluginSummaryById(pluginId)).thenReturn(Mono.just(pluginSummary));
        when(pluginGateway.fetchAllPluginVersionsById(pluginId))
                .thenReturn(Flux.just(unPublishedPluginVersion, pluginVersion1, pluginVersion2));
        when(pluginGateway.persistSummary(any(PluginSummary.class))).thenReturn(Flux.just(new Void[]{}));

        PluginSummary result = pluginService.unPublishPluginVersion(pluginId, 2, 0, 0).block();

        assertNotNull(result);
        assertNotNull(result.getLatestVersion());
        assertEquals("1.22.144", result.getLatestVersion());
    }

    @Test
    void test_deletePluginVersion_invalidArgs() {
        IllegalArgumentFault f1 = assertThrows(IllegalArgumentFault.class,
                                               () -> pluginService.deletePluginVersion(null, null));

        assertEquals("pluginId is required", f1.getMessage());

        IllegalArgumentFault f2 = assertThrows(IllegalArgumentFault.class,
                                               () -> pluginService.deletePluginVersion(pluginId, null));

        assertEquals("plugin version is required", f2.getMessage());
    }

    @Test
    void test_deletePluginVersion_notUnpublished() {
        PluginSummary pluginSummary = mock(PluginSummary.class);
        when(pluginSummary.getType()).thenReturn(PluginType.COMPONENT);
        when(pluginGateway.fetchPluginSummaryById(pluginId)).thenReturn(Mono.just(pluginSummary));
        when(pluginGateway.fetchPluginSummaryById(eq(PLUGIN_ID))).thenReturn(Mono.just(pluginSummary));
        when(pluginGateway.fetchAllPluginVersionByIdMajorMinorPatch(any(UUID.class), any(Integer.class),
                                                                    any(Integer.class), any(Integer.class)))
                .thenReturn(Flux.just(pluginVersion1));
        when(activityGateway.findActivityIdsByPluginIdAndVersion(pluginId, PLUGIN_VERSION)).thenReturn(Flux.empty());
        when(interactiveGateway.findInteractiveIdsByPluginIdAndVersion(pluginId, PLUGIN_VERSION)).thenReturn(Flux.empty());
        when(componentGateway.findComponentIdsByPluginIdAndVersion(pluginId, PLUGIN_VERSION)).thenReturn(Flux.empty());
        when(pluginGateway.deletePluginVersion(pluginVersion1)).thenReturn(Flux.empty());
        when(pluginGateway.fetchAllPluginVersionsById(pluginId))
                .thenReturn(Flux.just(unPublishedPluginVersion, pluginVersion1, pluginVersion2));

        MethodNotAllowedFault f = assertThrows(MethodNotAllowedFault.class,
                                               () -> pluginService.deletePluginVersion(pluginId, PLUGIN_VERSION).blockFirst());

        assertEquals("Unpublishing the plugin version is required to delete", f.getMessage());
    }

    @Test
    void test_deletePluginVersion_referenced() {
        PluginSummary pluginSummary = mock(PluginSummary.class);
        when(pluginSummary.getType()).thenReturn(PluginType.UNIT);
        when(pluginGateway.fetchPluginSummaryById(pluginId)).thenReturn(Mono.just(pluginSummary));
        when(pluginGateway.fetchPluginSummaryById(eq(PLUGIN_ID))).thenReturn(Mono.just(pluginSummary));
        when(pluginGateway.fetchAllPluginVersionByIdMajorMinorPatch(any(UUID.class), any(Integer.class),
                                                                    any(Integer.class), any(Integer.class)))
                .thenReturn(Flux.just(unPublishedPluginVersion));
        when(activityGateway.findActivityIdsByPluginIdAndVersion(pluginId, PLUGIN_VERSION)).thenReturn(Flux.just(UUIDs.random(), UUIDs.random()));
        when(interactiveGateway.findInteractiveIdsByPluginIdAndVersion(pluginId, PLUGIN_VERSION)).thenReturn(Flux.empty());
        when(componentGateway.findComponentIdsByPluginIdAndVersion(pluginId, PLUGIN_VERSION)).thenReturn(Flux.empty());
        when(pluginGateway.deletePluginVersion(pluginVersion1)).thenReturn(Flux.just(new Void[]{}));

        MethodNotAllowedFault f = assertThrows(MethodNotAllowedFault.class,
                                               () -> pluginService.deletePluginVersion(pluginId, PLUGIN_VERSION).blockLast());

        assertEquals("Plugin version has been referenced to courseware element", f.getMessage());
    }

    @Test
    void test_deletePluginVersion_success() {
        PluginSummary pluginSummary = mock(PluginSummary.class);
        when(pluginSummary.getType()).thenReturn(PluginType.COMPONENT);
        when(pluginGateway.fetchPluginSummaryById(pluginId)).thenReturn(Mono.just(pluginSummary));
        when(pluginGateway.fetchAllPluginVersionByIdMajorMinorPatch(any(UUID.class), any(Integer.class),
                                                                    any(Integer.class), any(Integer.class)))
                .thenReturn(Flux.just(unPublishedPluginVersion));
        when(activityGateway.findActivityIdsByPluginIdAndVersion(pluginId, PLUGIN_VERSION)).thenReturn(Flux.empty());
        when(interactiveGateway.findInteractiveIdsByPluginIdAndVersion(pluginId, PLUGIN_VERSION)).thenReturn(Flux.empty());
        when(componentGateway.findComponentIdsByPluginIdAndVersion(pluginId, PLUGIN_VERSION)).thenReturn(Flux.empty());
        when(pluginGateway.deletePluginVersion(unPublishedPluginVersion)).thenReturn(Flux.just(new Void[]{}));

        pluginService.deletePluginVersion(pluginId, PLUGIN_VERSION);

        verify(pluginGateway).fetchAllPluginVersionByIdMajorMinorPatch(pluginId, major, minor, patch-1);
    }

    @Test
    void test_fetchPluginFiltersByIdVersionExpr_emptyList() {
        UUID pluginId = UUID.randomUUID();
        String version = "1.2.0";
        PluginVersion pluginVersion = new PluginVersion().setPluginId(pluginId)
                .setMajor(1)
                .setMinor(2)
                .setPatch(0);
        when(pluginGateway.fetchAllPluginVersionsById(any())).thenReturn(Flux.just(pluginVersion));
        when(pluginGateway.fetchPluginFiltersByIdVersion(any(), any())).thenReturn(Flux.empty());
        List<PluginFilter> pluginFilterList = pluginService.fetchPluginFiltersByIdVersionExpr(pluginId,
                                                                                              version).block();
        assertNotNull(pluginFilterList);
        assertTrue(pluginFilterList.isEmpty());
    }

    @Test
    void test_fetchPluginFiltersByIdVersionExpr() {
        UUID pluginId = UUID.randomUUID();
        String version = "1.2.0";
        PluginVersion pluginVersion = new PluginVersion().setPluginId(pluginId)
                .setMajor(1)
                .setMinor(2)
                .setPatch(0);
        when(pluginGateway.fetchAllPluginVersionsById(any())).thenReturn(Flux.just(pluginVersion));
        when(pluginGateway.fetchPluginFiltersByIdVersion(any(), any())).thenReturn(Flux.just(new PluginFilter()
                                                                                                     .setPluginId(
                                                                                                             pluginId)
                                                                                                     .setFilterType(
                                                                                                             PluginFilterType.ID)));
        List<PluginFilter> pluginFilterList = pluginService.fetchPluginFiltersByIdVersionExpr(pluginId,
                                                                                              version).block();
        assertNotNull(pluginFilterList);
        assertNotNull(pluginFilterList.get(0).getFilterType());
        assertNotNull(pluginFilterList.get(0).getPluginId());
    }

    @Test
    void test_success_FilterSummaryPayloadsWithPluginFilters() {
        UUID plugin_id_ONE = UUID.randomUUID();

        List<PluginSummaryPayload> pluginSummaryPayloads = getPluginSummaryPayloads(plugin_id_ONE);

        List<PluginFilter> pluginFilterList = new ArrayList<>();
        Set<String> filterValues_one = new HashSet<>();
        filterValues_one.add(plugin_id_ONE.toString());

        PluginFilter pluginFilter = new PluginFilter()
                .setPluginId(plugin_id_ONE)
                .setFilterType(PluginFilterType.ID)
                .setFilterValues(filterValues_one);

        pluginFilterList.add(pluginFilter);

        List<PluginSummaryPayload> filteredPayload = pluginService.filterPluginSummaryPayloads(
                pluginSummaryPayloads,
                pluginFilterList);

        assertNotNull(filteredPayload);
        assertEquals(1, filteredPayload.size());

    }

    @Test
    void test_FilterPayloadsWithPluginFilters_NoResultFound() {
        UUID plugin_id_ONE = UUID.randomUUID();
        UUID plugin_id_TWO = UUID.randomUUID();

        List<PluginSummaryPayload> pluginSummaryPayloads = getPluginSummaryPayloads(plugin_id_ONE);

        List<PluginFilter> pluginFilterList = new ArrayList<>();
        Set<String> filterValues_one = new HashSet<>();
        filterValues_one.add(plugin_id_TWO.toString());

        PluginFilter pluginFilter = new PluginFilter()
                .setPluginId(plugin_id_ONE)
                .setFilterType(PluginFilterType.ID)
                .setFilterValues(filterValues_one);

        pluginFilterList.add(pluginFilter);

        List<PluginSummaryPayload> filteredPayload = pluginService.filterPluginSummaryPayloads(
                pluginSummaryPayloads,
                pluginFilterList);

        assertNotNull(filteredPayload);
        assertEquals(0, filteredPayload.size());

    }

    @Test
    void testFilterSummaryPayloadsWith_No_PluginFilters() {
        UUID plugin_id_ONE = UUID.randomUUID();
        List<PluginSummaryPayload> pluginSummaryPayloads = getPluginSummaryPayloads(plugin_id_ONE);

        List<PluginSummaryPayload> filteredPayload = pluginService.filterPluginSummaryPayloads(
                pluginSummaryPayloads,
                new ArrayList<>());

        assertNotNull(filteredPayload);
        assertEquals(1, filteredPayload.size());

    }

    private List<PluginSummaryPayload> getPluginSummaryPayloads(final UUID plugin_id) {
        List<PluginSummaryPayload> pluginSummaryPayloads = new ArrayList<>();
        PluginSummaryPayload pluginSummaryPayload_ONE = new PluginSummaryPayload()
                .setPluginId(plugin_id)
                .setLatestVersion("1.1")
                .setType(PluginType.COURSE);

        pluginSummaryPayloads.add(pluginSummaryPayload_ONE);

        return pluginSummaryPayloads;
    }

    @Test
    void findPluginSummary_noPluginId() {
        Throwable t = assertThrows(IllegalArgumentException.class, ()-> pluginService.findPlugin(null, null));
        assertEquals("pluginId is required", t.getMessage());
    }

    @Test
    void findPluginSummary_versionNotFound() {
        when(pluginGateway.fetchAllPluginVersionsById(PLUGIN_ID)).thenReturn(Flux.empty());

        Throwable t = assertThrows(PluginNotFoundFault.class, () -> pluginService.findPlugin(PLUGIN_ID, VERSION).block());
        assertEquals("Version '1.2.0' for plugin_id='" + PLUGIN_ID + "' does not exist", t.getMessage());
    }

    @Test
    void findPluginSummary_pluginNotFound() {
        when(pluginGateway.fetchAllPluginVersionsById(WRONG_PLUGIN_ID)).thenReturn(Flux.just(new PluginVersion()
                                                                                                     .setMajor(1)
                                                                                                     .setMinor(2)
                                                                                                     .setPatch(0)));
        when(pluginGateway.fetchPluginSummaryById(WRONG_PLUGIN_ID)).thenReturn(Mono.empty());
        when(pluginGateway.fetchViews(WRONG_PLUGIN_ID, VERSION)).thenReturn(Flux.empty());
        when(pluginGateway.fetchPluginManifestByIdVersion(eq(WRONG_PLUGIN_ID), anyString())).thenReturn(Mono.just(new PluginManifest()));

        Throwable t = assertThrows(PluginNotFoundFault.class, () -> pluginService.findPlugin(WRONG_PLUGIN_ID, VERSION).block());
        assertEquals(String.format("summary not found for plugin_id='%s'", WRONG_PLUGIN_ID), t.getMessage());
    }

    @Test
    void test_fetchPluginFiltersTagsByVersionExpr() {
        UUID pluginId = UUID.randomUUID();
        String version = "1.2.0";
        PluginVersion pluginVersion = new PluginVersion().setPluginId(pluginId)
                .setMajor(1)
                .setMinor(2)
                .setPatch(0);
        when(pluginGateway.fetchAllPluginVersionsById(any())).thenReturn(Flux.just(pluginVersion));
        when(pluginGateway.fetchPluginFiltersByIdVersion(any(), any())).thenReturn(Flux.just(new PluginFilter()
                                                                                                     .setPluginId(
                                                                                                             pluginId)
                                                                                                     .setFilterType(
                                                                                                             PluginFilterType.TAGS)));
        List<PluginFilter> pluginFilterList = pluginService.fetchPluginFiltersByIdVersionExpr(pluginId,
                                                                                              version).block();
        assertNotNull(pluginFilterList);
        assertNotNull(pluginFilterList.get(0).getFilterType());
        assertNotNull(pluginFilterList.get(0).getPluginId());
    }

    @Test
    void test_success_FilterSummaryPayloadsWithPluginFilters_tags() {
        UUID plugin_id_ONE = UUID.randomUUID();

        List<PluginSummaryPayload> pluginSummaryPayloads = getPluginSummaryPayloadsETextAllowed(plugin_id_ONE);
        List<PluginFilter> pluginFilterList = new ArrayList<>();
        Set<String> filterValues_one = new HashSet<>();
        filterValues_one.add("eTextAllowed");

        PluginFilter pluginFilter = new PluginFilter()
                .setPluginId(plugin_id_ONE)
                .setFilterValues(filterValues_one)
                .setFilterType(PluginFilterType.TAGS);
        pluginFilterList.add(pluginFilter);

        List<PluginSummaryPayload> filteredPayload = pluginService.filterPluginSummaryPayloads(
                pluginSummaryPayloads,
                pluginFilterList);

        assertNotNull(filteredPayload);
        assertEquals(3, filteredPayload.size());

    }

    @Test
    void publish_success_no_images_eText() throws PluginPublishException, S3BucketUploadException, IOException {
        PublishedPlugin publishedPlugin = successUpload(pluginFileNameWitheText, new PluginSummary());
        assertTrue(publishedPlugin.getPluginManifest().getScreenshots().isEmpty());
        assertNotNull(publishedPlugin.getPluginManifest().getThumbnail());
    }

    @Test
    void publish_success_latestVersionUpdated_eText() throws PluginPublishException, IOException, S3BucketUploadException {
        final String expectedVersion = "1.2.0"; //version from zip file
        successUpload(pluginFileNameWitheText, new PluginSummary());
        ArgumentCaptor<PluginSummary> pluginSummaryCaptor = ArgumentCaptor.forClass(PluginSummary.class);
        verify(pluginGateway).persistSummary(pluginSummaryCaptor.capture());
        assertEquals(expectedVersion, pluginSummaryCaptor.getValue().getLatestVersion());
    }

    /**
     * Get plugin manifest , views and searchable fields from parse method
     *
     * @return PluginPublishFields
     */
    private PluginParsedFields getPluginPublishFieldseText() {
        Map<String, Object> views = new LinkedHashMap<>();
        Map<String, Object> learner = new LinkedHashMap<>();
        learner.put("contentType", "javascript");
        learner.put("entryPoint", "index.js");
        learner.put("publicDir", "");
        views.put("LEARNER", learner);

        return new PluginParsedFields()
                .setPluginManifest(new PluginManifest()
                        .setName("TextInput")
                        .setTags(Arrays.asList("eTextAllowed"))
                        .setVersion("1.2.0")
                        .setType(PluginType.COMPONENT)
                        .setConfigurationSchema(searchableConfigSchema)
                        .setPluginId(UUID.fromString("93e2771f-6e4a-47d2-921c-ee6ab3ddb6aa"))
                        .setPublisherId(UUID.randomUUID())
                        .setZipHash("134")
                        .setScreenshots(new HashSet<>())
                        .setThumbnail("thumbnail"))
                .setSearchableFields(Json.toJsonNode(searchable))
                .setViews(views)
                .setPluginFilters(new ArrayList<>());
    }

    @Test
    void publish_success_with_tags_eText() throws PluginPublishException, IOException, S3BucketUploadException {
        when(pluginSummary.getLatestVersion()).thenReturn(null);
        PublishedPlugin publishedPlugin = successUpload(pluginFileNameWitheText, new PluginSummary().setId(PLUGIN_ID));
        ArgumentCaptor<PluginSummary> pluginSummaryCaptor = ArgumentCaptor.forClass(PluginSummary.class);
        verify(pluginGateway).persistSummary(pluginSummaryCaptor.capture());
        PluginManifest manifest = publishedPlugin.getPluginManifest();
        assertNotNull(manifest);
        PluginSummary capturedPluginSummary = pluginSummaryCaptor.getValue();
        assertNotNull(capturedPluginSummary);
        assertEquals(manifest.getTags(), capturedPluginSummary.getTags());
        assertNotNull(manifest.getTags());
        assertEquals(1, manifest.getTags().size());
        assertEquals("eTextAllowed", manifest.getTags().get(0));
    }

    private List<PluginSummaryPayload> getPluginSummaryPayloadsETextAllowed(final UUID plugin_id) {

        UUID plugin_id_ONE = UUID.randomUUID();
        UUID plugin_id_TWO = UUID.randomUUID();
        UUID plugin_id_THREE = UUID.randomUUID();
        UUID plugin_id_FOUR = UUID.randomUUID();
        UUID plugin_id_FIVE = UUID.randomUUID();
        UUID plugin_id_SIX = UUID.randomUUID();

        List<PluginSummaryPayload> pluginSummaryPayloads = new ArrayList<>();
        PluginSummaryPayload pluginSummaryPayload_ONE = new PluginSummaryPayload()
                .setPluginId(plugin_id_ONE)
                .setLatestVersion("1.1")
                .setTags(Arrays.asList("eTextAllowed"));

        PluginSummaryPayload pluginSummaryPayload_TWO = new PluginSummaryPayload()
                .setPluginId(plugin_id_TWO)
                .setLatestVersion("1.1")
                .setTags(Arrays.asList("eTextAllowed"));

        PluginSummaryPayload pluginSummaryPayload_THREE = new PluginSummaryPayload()
                .setPluginId(plugin_id_THREE)
                .setLatestVersion("1.1")
                .setTags(Arrays.asList("smart"));

        PluginSummaryPayload pluginSummaryPayload_FOUR = new PluginSummaryPayload()
                .setPluginId(plugin_id_FOUR)
                .setLatestVersion("1.1")
                .setTags(Arrays.asList("eTextAllowed", "Media", "tags"));

        PluginSummaryPayload pluginSummaryPayload_FIVE = new PluginSummaryPayload()
                .setPluginId(plugin_id_FIVE)
                .setLatestVersion("1.1")
                .setTags(Arrays.asList("Media", "tags"));

        PluginSummaryPayload pluginSummaryPayload_SIX = new PluginSummaryPayload()
                .setPluginId(plugin_id_SIX)
                .setLatestVersion("1.1")
                .setTags(Arrays.asList("Media"));

        pluginSummaryPayloads.add(pluginSummaryPayload_ONE);
        pluginSummaryPayloads.add(pluginSummaryPayload_TWO);
        pluginSummaryPayloads.add(pluginSummaryPayload_THREE);
        pluginSummaryPayloads.add(pluginSummaryPayload_FOUR);
        pluginSummaryPayloads.add(pluginSummaryPayload_FIVE);
        pluginSummaryPayloads.add(pluginSummaryPayload_SIX);

        return pluginSummaryPayloads;
    }

}