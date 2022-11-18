package com.smartsparrow.plugin.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;
import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.ActivityGateway;
import com.smartsparrow.courseware.data.ComponentGateway;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.InteractiveGateway;
import com.smartsparrow.exception.ConflictFault;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.MethodNotAllowedFault;
import com.smartsparrow.exception.NotAllowedException;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
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
import com.smartsparrow.plugin.data.PluginSearchableField;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginTeamCollaborator;
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
import com.smartsparrow.plugin.semver.SemVerExpression;
import com.smartsparrow.plugin.semver.SemVersion;
import com.smartsparrow.plugin.wiring.PluginConfig;
import com.smartsparrow.util.Files;
import com.smartsparrow.util.Hashing;
import com.smartsparrow.util.Urls;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
@SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "JDK 11 changes broke this rule")
// https://github.com/spotbugs/spotbugs/issues/756 - The whole class is suppressed because problem is in a lambda
public class PluginService {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(PluginService.class);
    private static final ObjectMapper om = new ObjectMapper();

    private final PluginGateway pluginGateway;
    private final Provider<PluginConfig> pluginConfig;
    private final S3Bucket s3Bucket;
    private final PluginAccessGateway pluginAccessGateway;
    private final AccountService accountService;
    private final PluginPermissionService pluginPermissionService;
    private final TeamService teamService;
    private final SchemaValidationService schemaValidationService;
    private final PluginSchemaParser pluginSchemaParser;
    private final PluginParserService pluginParserService;
    private final ActivityGateway activityGateway;
    private final InteractiveGateway interactiveGateway;
    private final ComponentGateway componentGateway;


    @Inject
    public PluginService(final PluginGateway pluginGateway,
                         final Provider<PluginConfig> pluginConfig,
                         final S3Bucket s3Bucket,
                         final PluginAccessGateway pluginAccessGateway,
                         final AccountService accountService,
                         final PluginPermissionService pluginPermissionService,
                         final TeamService teamService,
                         final SchemaValidationService schemaValidationService,
                         final PluginSchemaParser pluginSchemaParser,
                         final PluginParserService pluginParserService,
                         final ActivityGateway activityGateway,
                         final InteractiveGateway interactiveGateway,
                         final ComponentGateway componentGateway) {
        this.pluginGateway = pluginGateway;
        this.pluginConfig = pluginConfig;
        this.s3Bucket = s3Bucket;
        this.pluginAccessGateway = pluginAccessGateway;
        this.accountService = accountService;
        this.pluginPermissionService = pluginPermissionService;
        this.teamService = teamService;
        this.schemaValidationService = schemaValidationService;
        this.pluginSchemaParser = pluginSchemaParser;
        this.pluginParserService = pluginParserService;
        this.activityGateway = activityGateway;
        this.interactiveGateway = interactiveGateway;
        this.componentGateway = componentGateway;
    }

    /**
     * Create a new plugin summary if not exists and save creator with permission level OWNER for this plugin
     *
     * @param name       the name of the plugin
     * @param account    the plugin owner's account
     * @param pluginType type of plugin
     * @param pluginId   the plugin id
     * @return a newly created plugin summary
     * @throws IllegalArgumentException when either name or type are missing
     * @throws PluginAlreadyExistsFault if already exists a plugin with same id
     */
    public Mono<PluginSummary> createPluginSummary(@Nonnull final String name, final PluginType pluginType, @Nullable final UUID pluginId,
                                                   @Nullable final PublishMode mode, @Nonnull final Account account) {
        checkArgument(!Strings.isNullOrEmpty(name), "plugin name is required");
        affirmArgument(pluginId != null, "plugin id is required");

        return pluginGateway.fetchPluginSummaryById(pluginId)
                .hasElement()
                .filter(hasElement -> !hasElement)
                .switchIfEmpty(Mono.error(new PluginAlreadyExistsFault(pluginId)))
                .flatMap(pluginIdNotFound -> {
                      return createPluginSummaryLatest(name, pluginType, pluginId, mode, account);
                });
    }

    /**
     * setting the pluginId when plugin created
     *
     * @param name       the name of the plugin
     * @param account    the plugin owner's account
     * @param pluginType type of plugin
     * @return a newly created plugin summary
     * @throws IllegalArgumentException when either name or type are missing
     */
    public Mono<PluginSummary> createPluginSummary(@Nonnull final String name, final PluginType pluginType,
                                                   @Nullable final PublishMode mode, @Nonnull final Account account) {
        checkArgument(!Strings.isNullOrEmpty(name), "plugin name is required");
        return createPluginSummaryLatest(name, pluginType, UUIDs.timeBased(), mode, account);

    }

    /**
     * Create a new plugin summary and save creator with permission level OWNER for this plugin
     *
     * @param name       the name of the plugin
     * @param account    the plugin owner's account
     * @param pluginType type of plugin
     * @param pluginId   the plugin id
     * @return a newly created plugin summary
     * @throws IllegalArgumentException when either name or id are missing
     */
    public Mono<PluginSummary> createPluginSummaryLatest(@Nonnull final String name, final PluginType pluginType,  final UUID pluginId,
                                                         @Nullable final PublishMode mode, @Nonnull final Account account) {
        checkArgument(!Strings.isNullOrEmpty(name), "plugin name is required");
        affirmArgument(pluginId != null, "missing pluginId");

        Mono<PluginSummary> pluginSummary = Mono.just( new PluginSummary()
                                                               .setId(pluginId)
                                                               .setName(name)
                                                               .setCreatorId(account.getId())
                                                               .setType(pluginType)
                                                               .setSubscriptionId(account.getSubscriptionId())
                                                               .setPublishMode(mode));

        return pluginSummary
                .flatMap(_pluginSummary ->
                                 pluginGateway.persistSummary(_pluginSummary)
                                         .thenMany(pluginPermissionService.saveAccountPermission(account.getId(), _pluginSummary.getId(), PermissionLevel.OWNER))
                                         .then(Mono.just(_pluginSummary))
                );

    }

    /**
     * Fetches plugin summary and plugin manifest (if plugin was published).
     * If {@param versionExpr} contains wildcard the latest stable version satisfying the expression will be returned, <br/>
     * otherwise search by full match will be done. <br/>
     * If {@param versionExpr} is empty, the latest stable version will be return.
     * For example: if plugin has two versions: '2.0.0-alpha' and '1.10.0', the '1.10.0' will be returned.
     *
     * @param pluginId    plugin id
     * @param versionExpr version expression to fetch, can be empty
     * @return plugin info
     * @throws VersionParserFault  if version expression has invalid format.
     * @throws PluginNotFoundFault if plugin with given pluginId and version doesn't exist
     */
    public Mono<PluginPayload> findPluginInfo(final UUID pluginId, final String versionExpr) {
        checkArgument(pluginId != null, "pluginId is required");

        Mono<PluginSummary> pluginSummaryMono = fetchById(pluginId)
                .single()
                .doOnError(NoSuchElementException.class, t -> {
                    throw new PluginNotFoundFault(String.format("Plugin with id '%s' is not found", pluginId));
                });

        Mono<String> version = pluginSummaryMono.filter(PluginSummary::isPublished)
                .flatMap(pluginSummary -> findLatestVersion(pluginId, versionExpr))
                .doOnSuccess(v -> {
                    if (v == null) {
                        logger.info("Plugin '{}' doesn't have versions. Returning only summary", pluginId);
                    }
                });

        Mono<PluginManifest> manifestMono = version
                .flatMap(v -> pluginGateway.fetchPluginManifestByIdVersion(pluginId, v))
                .defaultIfEmpty(new PluginManifest());

        Mono<PluginSummaryPayload> summaryPayloadMono = pluginSummaryMono.flatMap(this::getPluginSummaryPayload);

        return Mono.zip(summaryPayloadMono, manifestMono)
                .map(tuple2 -> new PluginPayload()
                        .setPluginSummaryPayload(tuple2.getT1())
                        .setManifest(tuple2.getT2().getPluginId() == null ? null : tuple2.getT2()))
                .map(this::updateUrls);
    }

    /**
     * Fetches plugin by plugin id, view and version if provided. If no version provided, fetches the latest stable.
     * Supports SemVer expressions and wildcards described in @see https://github.com/zafarkhaja/jsemver#external-dsl
     *
     * @param pluginId    plugin id
     * @param view        view
     * @param versionExpr SemVer version or expression with wildcards
     * @return {@link PluginPayload}
     * @throws PluginNotFoundFault if plugin with given id, version or view doesn't exist
     * @throws VersionParserFault  if version contains unexpected symbols
     */
    @Trace(async = true)
    public Mono<PluginPayload> findPluginByIdAndView(final UUID pluginId, final String view, final String versionExpr) {
        checkArgument(pluginId != null, "pluginId is required");
        checkArgument(!Strings.isNullOrEmpty(view), "view is required");

        Mono<PluginSummaryPayload> summaryPayloadMono = getPluginSummaryPayload(pluginId)
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<String> version = findLatestVersion(pluginId, versionExpr)
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<PluginManifest> manifestMono = version
                .flatMap(v -> pluginGateway.fetchPluginManifestByIdVersion(pluginId, v))
                .defaultIfEmpty(new PluginManifest())
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<ManifestView> viewMono = version
                .flatMap(v -> pluginGateway.fetchManifestViewByIdVersionContext(pluginId, v, view))
                .single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, t -> {
                    throw new PluginNotFoundFault(String.format("view '%s' for plugin '%s' and version '%s' does not exist", view, pluginId, versionExpr));
                });

        return Mono.zip(summaryPayloadMono, manifestMono, viewMono)
                .map(tuple4 -> new PluginPayload()
                        .setPluginSummaryPayload(tuple4.getT1())
                        .setManifest(tuple4.getT2().getPluginId() == null ? null : tuple4.getT2())
                        .addEntryPoints(tuple4.getT3())
                        .setPluginRepositoryPath(tuple4.getT2().buildPluginRepositoryUrl(pluginConfig.get().getRepositoryPublicUrl())))
                .map(this::updateUrls);
    }

    /**
     * Find a published plugin with all the entry points
     *
     * @param pluginId    the id of the plugin to find
     * @param versionExpr SemVer version or expression with wildcards
     * @return a {@link PluginPayload}
     * @throws PluginNotFoundFault      when any of the {@link Mono} and {@link Flux} streams are empty
     * @throws VersionParserFault       if version contains unexpected symbols
     * @throws IllegalArgumentException when the supplied pluginId is null
     */
    @Trace(async = true)
    public Mono<PluginPayload> findPlugin(final UUID pluginId, final String versionExpr) {
        checkArgument(pluginId != null, "pluginId is required");

        Mono<PluginSummaryPayload> summaryPayloadMono = getPluginSummaryPayload(pluginId)
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<String> version = findLatestVersion(pluginId, versionExpr)
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<PluginManifest> manifestMono = version
                .flatMap(v -> pluginGateway.fetchPluginManifestByIdVersion(pluginId, v))
                .defaultIfEmpty(new PluginManifest())
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<List<ManifestView>> viewsMono = version
                .flatMap(v -> pluginGateway.fetchViews(pluginId, v).collectList())
                .doOnEach(ReactiveTransaction.linkOnNext());

        return Mono.zip(summaryPayloadMono, manifestMono, viewsMono)
                .map(tuple4 -> new PluginPayload()
                        .setPluginSummaryPayload(tuple4.getT1())
                        .setManifest(tuple4.getT2().getPluginId() == null ? null : tuple4.getT2())
                        .setEntryPoints(tuple4.getT3())
                        .setPluginRepositoryPath(tuple4.getT2().buildPluginRepositoryUrl(pluginConfig.get().getRepositoryPublicUrl())))
                .map(this::updateUrls);
    }

    /**
     * Builds the plugin repository path url for a plugin
     *
     * @param pluginId the plugin id
     * @param version the plugin version
     * @param zipHash the zipHash for a plugin
     * @return Mono of plugin path String
     */
    private Mono<String> buildPluginRepositoryUrl(final UUID pluginId, final String version, final String zipHash) {
        return Mono.just(String.format("%s/%s/%s/%s.zip", pluginConfig.get().getRepositoryPublicUrl(), pluginId, version, zipHash));
    }

    /**
     * Builds a PluginSummaryPayload for plugin
     *
     * @param pluginId the plugin id to build a payload object for
     * @return Mono with PluginSummaryPayload, always not empty
     * @throws PluginNotFoundFault when summary for plugin doesn't exist
     */
    @Trace(async = true)
    public Mono<PluginSummaryPayload> getPluginSummaryPayload(final UUID pluginId) {
        checkArgument(pluginId != null, "pluginId can not be null");

        return fetchById(pluginId)
                .flatMap(this::getPluginSummaryPayload)
                .single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, t -> {
                    String error = String.format("summary not found for plugin_id='%s'", pluginId);
                    throw new PluginNotFoundFault(error);
                });
    }

    /**
     * Builds a PluginSummaryPayload for plugin.
     *
     * @param pluginSummary the plugin to build a payload object for
     * @return Mono with PluginSummaryPayload, always not empty
     */
    @Trace(async = true)
    public Mono<PluginSummaryPayload> getPluginSummaryPayload(final PluginSummary pluginSummary) {
        checkArgument(pluginSummary != null, "pluginSummary can not be null");

        Mono<AccountPayload> accountPayload =
                accountService.getAccountPayload(pluginSummary.getCreatorId()).defaultIfEmpty(new AccountPayload())
                        .doOnEach(ReactiveTransaction.linkOnNext());

        return Mono.zip(Mono.just(pluginSummary), accountPayload)
                .map(tuple2 -> PluginSummaryPayload.from(tuple2.getT1(), tuple2.getT2()))
                .single();
    }

    /**
     * "Sync" back from the repository a plugin that was already published by another environment and files are present
     * in S3.
     * <p>
     * This downloads the plugin zip distro and recreates the necessary rows in the local DB, as if plugin was published
     * for the first time in this environment.
     * <p>
     * This is a local/sandbox only feature to facilitate bootstrapping new test environments and needs
     * configuration key plugin.allowSync set to true.
     * <p>
     * There's no need to verify the manifest, as checking of consistency was done at original publish.
     *
     * @return
     */
    public Mono<PluginPayload> syncFromRepo(final UUID pluginId, final String hash, final Account account) throws S3BucketLoadFileException, IOException {

        // retrieve published manifest file from S3
        String manifestContent;
        PluginManifest tempManifest;

        // create plugin manifest object
        JsonNode searchableNode;

        try{
            manifestContent = s3Bucket.getPluginFileContent(pluginId, hash, "manifest.json");
            tempManifest = om.readValue(manifestContent, PluginManifest.class);
            searchableNode = om.readTree(manifestContent).at("/searchable");
        } catch (S3BucketLoadFileException ex){
            manifestContent = s3Bucket.getPluginFileContent(pluginId, hash, "package.json");;
            Map<String, String> packageMap = om.readValue(manifestContent, Map.class);
            tempManifest = om.readValue(packageMap.get("bronte"), PluginManifest.class);
            searchableNode = om.readTree(manifestContent).at("/bronte/searchable");
        }

        // fill in missing fields
        tempManifest.setPluginId(pluginId)
                .setZipHash(hash)
                .setPublisherId(account.getId());
        if (tempManifest.getScreenshots() == null) {
            tempManifest.setScreenshots(new HashSet<>());
        }

        // replace manifest configuration_schema file path with actual content.
        // this is a horrible pattern, but needs to be improved in the publishing itself eventually.
        String configurationSchemaContent = s3Bucket.getPluginFileContent(pluginId, hash, tempManifest.getConfigurationSchema());
        tempManifest.setConfigurationSchema(configurationSchemaContent);

        //extract output schema and save to manifest
        try {
            String outputSchema = pluginSchemaParser.extractOutputSchema(tempManifest.getConfigurationSchema());
            tempManifest.setOutputSchema(outputSchema);
        } catch (JSONException e) {
            throw new PluginSchemaParserFault("Invalid plugin schema: " + e.getMessage());
        }

        // Create version obj - skip regular method because it validates incoming version as always higher,
        // which could break syncing
        PluginVersion pluginVersion = SemVersion.from(tempManifest.getVersion())
                .toPluginVersion(pluginId, Instant.now().toEpochMilli());

        // Does plugin exist yet?
        final PluginManifest manifest = tempManifest;
        Mono<PluginSummary> summary = find(pluginId)
                .single()
                // it does, no need to recreate summary, just updated object fields as necessary if new version is higher
                // newer
                .map(pluginSummary -> updateSummaryVersion(manifest, pluginVersion, pluginSummary))
                // It doesn't, create a new summary obj
                .onErrorResume(ignored -> {
                    // Create new plugin via service method to setup permissions and update some newly created obj fields
                    return createPluginSummary(manifest.getName(), manifest.getType(), pluginId, PublishMode.DEFAULT, account)
                            .map(s -> s.setName(manifest.getName())
                                    .setDescription(manifest.getDescription())
                                    .setLatestVersion(manifest.getVersion())
                                    .setLatestVersionReleaseDate(pluginVersion.getReleaseDate())
                                    .setThumbnail(manifest.getThumbnail())
                                    .setTags(manifest.getTags()));
                });


        // create views with content
        Flux<ManifestView> views = Flux.fromIterable(getDistinctEntryPoints(manifestContent).entrySet())
                .handle((entry, sink) -> {
                    try {
                        ManifestView manifestView = om.readValue(om.writeValueAsString(entry.getValue()), ManifestView.class)
                                .setContext(entry.getKey())
                                .setPluginId(pluginId)
                                .setVersion(manifest.getVersion());

                        // Read entry point content from published files in repository
                        String entryPointData = s3Bucket.getPluginFileContent(pluginId, hash, String.format("%s/%s", manifestView.getPublicDir(),
                                manifestView.getEntryPointPath()));
                        manifestView.setEntryPointData(entryPointData);
                        sink.next(manifestView);
                    } catch (IOException | S3BucketLoadFileException e) {
                        logger.error("Failed to parse the entry point `{}` {}", entry.getKey(), e.getMessage());
                        sink.error(e);
                    }
                });

        List<PluginSearchableField> searchable = Lists.newArrayList();
        try {
            searchable = parseSearchableJson(searchableNode, manifest);
        } catch (PluginPublishException e) {
            logger.error("error syncing plugin", e);
            Exceptions.propagate(e);
        }

        return Flux.merge(
                pluginGateway.persistVersion(pluginVersion),
                pluginGateway.persistSummary(summary),
                pluginGateway.persistManifest(manifest),
                pluginGateway.persistView(views),
                pluginGateway.persistSearchableFieldByPlugin(Flux.fromIterable(searchable))
        )
                .then(findPlugin(pluginId, manifest.getVersion()));

    }

    /**
     * Compares an existing pluginSummary with data from manifest being published, if manifest's version is the latest,
     * updates its fields when necessary.
     * <p>
     * This DOES NOT persist the change to storage.
     *
     * @param newManifest           new manifest being published
     * @param newVersion            the version object created from newManifest with buildPluginVersion
     * @param existingPluginSummary existing summary retrieved from DB
     * @return existingPluginSummary with updated fields if new version is more recent than existing one or
     * unchanged otherwise
     */
    private PluginSummary updateSummaryVersion(final PluginManifest newManifest, final PluginVersion newVersion, final PluginSummary existingPluginSummary) {
        if (isUpdateLatestVersion(existingPluginSummary.getLatestVersion(), newManifest.getVersion())) {
            existingPluginSummary.setLatestVersion(newManifest.getVersion());
            existingPluginSummary.setName(newManifest.getName());
            existingPluginSummary.setDescription(newManifest.getDescription());
            existingPluginSummary.setLatestVersionReleaseDate(newVersion.getReleaseDate());
            String thumbnail = newManifest.getThumbnail();
            existingPluginSummary.setThumbnail(thumbnail != null ? buildPublicUrl(newManifest, newManifest.getThumbnail()) : null);
            existingPluginSummary.setTags(newManifest.getTags());
            existingPluginSummary.setLatestGuide(newManifest.getGuide() != null ? buildPublicUrl(newManifest, newManifest.getGuide()) : null);
            existingPluginSummary.setDefaultHeight(newManifest.getDefaultHeight());
        }
        return existingPluginSummary;
    }

    /**
     * Publish a plugin version: parse and validate zip file, persist to DB, upload to S3 bucket.
     * <p>
     * If pluginId is specified, then the plugin id from manifest file will be ignored.
     * Note: the zip file and manifest file will uploaded unchanged.
     *
     * @param inputStream the uploaded file
     * @param fileName    the file name
     * @param publisherId the publisher account id
     * @param pluginId    the plugin id to override id from manifest file, can be null
     * @return a {@link PublishedPlugin} object
     * @throws PluginPublishException   when something goes wrong while publishing the plugin
     * @throws IllegalArgumentException when any of the supplied arguments or file content have unexpected values
     * @throws PluginSchemaParserFault  when plugin schema is invalid and output schema can not be extracted
     */
    public PublishedPlugin publish(final InputStream inputStream, final String fileName, final UUID publisherId, final UUID pluginId)
            throws IllegalArgumentException, PluginPublishException {

        Path tmpFilePath = null;
        File folder = null;
        try {
            // save the stream to a temporary file
            tmpFilePath = java.nio.file.Files.createTempFile(null, null);
            File tmpFile = tmpFilePath.toFile();
            java.nio.file.Files.copy(inputStream, tmpFilePath, StandardCopyOption.REPLACE_EXISTING);

            //throw exception if it is not a zip format
            if (!Files.isAZip(tmpFile)) {
                throw new IllegalArgumentException("zip format required");
            }

            // compute the hash of the file
            String hash = Hashing.file(tmpFile);

            // save the zip file
            File zippedPlugin = Files.saveZip(tmpFile, hash, fileName);
            folder = zippedPlugin.getParentFile();

            // unzip the uploaded archive
            final Map<String, File> files = Files.unzip(zippedPlugin, hash);

            logger.info("Files saved at {} - number of files {}", zippedPlugin.getAbsolutePath(), files.size());

            // parse manifest or package json file
            PluginParsedFields pluginParsedFields = pluginParserService.parse(files, pluginId, hash, publisherId);

            PluginManifest pluginManifest = pluginParsedFields.getPluginManifest();
           //parse searchable field
            List<PluginSearchableField> searchable = parseSearchableJson(pluginParsedFields.getSearchableFields(), pluginManifest);
            PluginSummary pluginSummary = pluginGateway.fetchPluginSummaryById(pluginManifest.getPluginId()).block();
            // throw exception if they are trying to publish a plugin that does not exists or was deleted
            if (pluginSummary == null || pluginSummary.isDeleted()) {
                throw new IllegalArgumentException(String.format("Plugin summary not found for id %s", pluginManifest.getPluginId()));
            }

            if (pluginSummary.getType() == null) {
                pluginSummary.setType(pluginManifest.getType());
            } else if (!pluginSummary.getType().equals(pluginManifest.getType())) {
                throw new IllegalArgumentException(String.format("Plugin type must be the same. Found `%s` expected `%s`.",
                        pluginManifest.getType(), pluginSummary.getType()));
            }

            final List<ManifestView> manifestViews = buildManifestViews(files, pluginManifest, pluginParsedFields.getViews());

            // if any failure in parsing throw an exception
            if (manifestViews.stream().anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("Failed to parse an entry point");
            }

            //validate and build version
            final PluginVersion pluginVersion = buildPluginVersion(pluginManifest);

            PermissionLevel permission = pluginPermissionService.findHighestPermissionLevel(publisherId, pluginManifest.getPluginId()).block();

            //Validating permission to publish plugin based on it's mode
            validatePermission(pluginSummary, permission);

            //extract output schema and save to manifest
            try {
                String outputSchema = pluginSchemaParser.extractOutputSchema(pluginManifest.getConfigurationSchema());
                pluginManifest.setOutputSchema(outputSchema);
            } catch (JSONException e) {
                throw new PluginSchemaParserFault("Invalid plugin schema: " + e.getMessage());
            }

            //upload to s3 bucket
            s3Bucket.uploadPlugin(pluginManifest, zippedPlugin, files);

            // update the latest version in the plugin summary if needed
            updateSummaryVersion(pluginManifest, pluginVersion, pluginSummary);

            // upload to s3 succeeded so persist to cassandra
            Flux.concat(
                    pluginGateway.persistSearchableFieldByPlugin(Flux.fromIterable(searchable)),
                    pluginGateway.persistView(manifestViews.toArray(new ManifestView[manifestViews.size()])),
                    pluginGateway.persistManifest(pluginManifest),
                    pluginGateway.persistVersion(pluginVersion),
                    pluginGateway.persistSummary(pluginSummary),
                    pluginGateway.persistPluginFilters(pluginParsedFields.getPluginFilters())
            ).blockLast();

            // return the published plugin
            return new PublishedPlugin()
                    .setPluginManifest(pluginManifest)
                    .setManifestView(manifestViews);
        } catch (IOException | VersionParserFault e) {
            logger.error("error during plugin publishing {}", e.getMessage());
            throw new PluginPublishException(e.getMessage());
        } catch (S3BucketUploadException e) {
            throw new PluginPublishException(e.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error(String.format("Error when closing the original input stream for plugin with fileName='%s'", fileName), e);
            }
            // delete the temp file
            if (tmpFilePath != null) {
                try {
                    java.nio.file.Files.delete(tmpFilePath);
                } catch (IOException e) {
                    logger.error(String.format("Error when deleting file '%s'", tmpFilePath), e);
                }
            }
            // delete all files from the system
            if (folder != null) {
                try {
                    Files.deleteAll(folder);
                    if (logger.isDebugEnabled()) {
                        logger.debug("`{}` folder removed from the system", folder.getAbsolutePath());
                    }
                } catch (IOException e) {
                    logger.error(String.format("Error when deleting files from folder '%s'", folder), e);
                }
            }
        }
    }

    /**
     * Validating permission to publish plugin based on it's mode
     *
     * @param pluginSummary the plugin Summary
     * @param permission the permission
     */
    private void validatePermission(final PluginSummary pluginSummary, final PermissionLevel permission) {
        // if the account has no existing permission over the plugin then do not allow the publishing
        if (permission == null) {
            throw new NotAllowedException("User must have valid permission to publish a plugin");
        }

        // Allow Contributor or higher to publish new version in DEFAULT mode
        if (PublishMode.DEFAULT.equals(pluginSummary.getPublishMode()) && permission.isLowerThan(PermissionLevel.CONTRIBUTOR)) {
            throw new NotAllowedException(
                    "Only plugin contributor or higher permission level can publish a new version in DEFAULT mode");
        }

        // Only owner can publish a new version in STRICT mode
        if (PublishMode.STRICT.equals(pluginSummary.getPublishMode()) && !PermissionLevel.OWNER.equals(permission)) {
            throw new NotAllowedException("Only plugin owner can publish a new version in STRICT mode");
        }
    }

    /**
     * Method to un-publish a particular plugin version
     * @param pluginId - identifier for the plugin to be un-published
     * @param major - major version for the plugin to be un-published
     * @param minor - minor version for the plugin to be un-published
     * @param patch - patch version for the plugin to be un-published
     * @return {@link Mono<PluginSummary>} - Mono of pluginSummary
     */
    @Trace(async = true)
    public Mono<PluginSummary> unPublishPluginVersion(final UUID pluginId, final Integer major, final Integer minor, final Integer patch) {
        affirmNotNull(pluginId, "pluginId is required");
        affirmNotNull(major, "major version is required");
        affirmNotNull(minor, "minor version is required");
        affirmNotNull(patch, "patch version is required");

        String currentUnPublishedVersion = String.format("%d.%d.%d", major, minor, patch);

        Mono<Void> unpublished = pluginGateway.fetchAllPluginVersionByIdMajorMinorPatch(pluginId, major, minor, patch)
                .flatMap(pluginGateway::unPublishPluginVersion)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());

        Mono<PluginSummary> pluginSummaryMono = pluginGateway.fetchPluginSummaryById(pluginId)
                .flatMap(currentPluginSummary -> {
                   if (currentUnPublishedVersion.equals(currentPluginSummary.getLatestVersion())) {
                       // find the prev version and update plugin summary
                       PluginVersion prevPluginVersion = pluginGateway
                               .fetchAllPluginVersionsById(pluginId)
                               .takeUntil(pluginVersion -> !pluginVersion.getUnpublished())
                               .doOnEach(ReactiveTransaction.linkOnNext())
                               .blockLast();

                       if(prevPluginVersion != null) {
                           String prevPublishedVersion = String.format("%d.%d.%d", prevPluginVersion.getMajor(),
                                                                       prevPluginVersion.getMinor(), prevPluginVersion.getPatch());

                           PluginSummary updatedPluginSummary = currentPluginSummary.setLatestVersionReleaseDate(prevPluginVersion.getReleaseDate())
                                   .setLatestVersion(prevPublishedVersion);

                           return pluginGateway
                                   .persistSummary(updatedPluginSummary)
                                   .singleOrEmpty()
                                   .thenReturn(updatedPluginSummary)
                                   .doOnEach(ReactiveTransaction.linkOnNext());
                       }
                   }
                   return Mono.just(currentPluginSummary);
               })
                .doOnEach(ReactiveTransaction.linkOnNext());
        return unpublished.then(pluginSummaryMono);
    }

    /**
     * Verifies if PluginSummary.latestVersion should be updated.
     * Rules:
     * 1. If existing latestVersion is empty - update
     * 2. If existing latestVersion is unstable and new version is stable - update
     * 3. If both existing and new versions are unstable and new version is greater then existing latest - update
     * 4. If both existing and new versions are stable and new version is greater then existing later - update
     *
     * @param latestVersion    existing latest version, can be empty or null
     * @param publishedVersion new published version
     * @return {@code true} - if latest version should be updated, otherwise {@code false}
     * @throws VersionParserFault if versions strings can't be parsed
     */
    boolean isUpdateLatestVersion(final String latestVersion, final String publishedVersion) throws VersionParserFault {
        SemVersion newVersion = SemVersion.from(publishedVersion);
        if (Strings.isNullOrEmpty(latestVersion)) {
            return true;
        } else {
            SemVersion oldVersion = SemVersion.from(latestVersion);
            if (newVersion.isStable()) {
                return !oldVersion.isStable() || newVersion.greaterThan(oldVersion);
            } else {
                return !oldVersion.isStable() && newVersion.greaterThan(oldVersion);
            }
        }
    }

    /**
     * Fetch a plugin summary by id
     *
     * @param pluginId the id of the plugin to fetch
     * @return {@link Mono} of {@link PluginSummary}
     */
    @Trace(async = true)
    public Mono<PluginSummary> fetchById(UUID pluginId) {
        return pluginGateway.fetchPluginSummaryById(pluginId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch a plugin summary by id
     *
     * @param pluginId the id of the plugin to fetch
     * @return {@link Mono} of {@link PluginSummary}
     * @throws PluginNotFoundFault when the plugin is not found
     */
    @Trace(async = true)
    public Mono<PluginSummary> find(final UUID pluginId) {
        return pluginGateway.fetchPluginSummaryById(pluginId)
                .single()
                .doOnError(NoSuchElementException.class, t -> {
                    throw new PluginNotFoundFault(String.format("Plugin with id '%s' is not found", pluginId));
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all plugin summaries based on the account permissions. This includes also all the plugins the account has
     * access via team permissions
     *
     * @param accountId the account to search the permissions for
     * @return a {@link Flux} of {@link PluginSummary}
     */
    @Trace(async = true)
    public Flux<PluginSummary> fetchPlugins(final UUID accountId) {
        return pluginAccessGateway.fetchPluginsByAccount(accountId)
                .map(PluginAccount::getPluginId)
                .mergeWith(fetchPluginsByTeamsFor(accountId))
                .distinct()
                .flatMap(this::fetchById)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetches all the pluginIds an account has access to via team permission. This will first find all the teams a user
     * has access to and foreach team all the plugins the team has access to.
     *
     * @param accountId the accountId to search the plugins by team for
     * @return a flux of uuid
     */
    private Flux<UUID> fetchPluginsByTeamsFor(final UUID accountId) {
        return teamService.findTeamsForAccount(accountId)
                .map(one -> pluginAccessGateway.fetchPluginsByTeam(one.getTeamId()))
                .flatMap(one -> one);
    }

    /**
     * Fetch all plugin summaries by the supplied account and filters them by plugin type. Plugin summaries with
     * <code>null</code> {@link PluginType} are filtered out
     *
     * @param accountId  the account id to search the plugins for
     * @param pluginType the plugin type to filter
     * @return a {@link Flux} of {@link PluginSummary}
     */
    @Trace(async = true)
    public Flux<PluginSummary> fetchPlugins(final UUID accountId, final PluginType pluginType) {
        return fetchPlugins(accountId)
                .filter(one -> one.getType() != null && one.getType().equals(pluginType))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all published plugins the account has access to. If a valid plugin type is supplied as argument,
     * the stream is further filtered leaving out all the plugin summaries that do not match the pluginType
     * argument
     *
     * @param accountId  the account to search the plugins for
     * @param pluginType the plugin type to filter
     * @return a {@link Flux} of {@link PluginSummary}
     */
    @Trace(async = true)
    public Flux<PluginSummary> fetchPublishedPlugins(final UUID accountId, final PluginType pluginType) {

        if (pluginType == null) {
            return fetchPlugins(accountId)
                    .filter(pluginSummary -> pluginSummary.getLatestVersion() != null)
                    .doOnEach(ReactiveTransaction.linkOnNext());
        }

        return fetchPlugins(accountId, pluginType)
                .filter(pluginSummary -> pluginSummary.getLatestVersion() != null)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Find the latest version suitable for given expression.
     * If {@param versionExpr} is empty or null, returns value from {@link PluginSummary#getLatestVersion()}.
     * <br/>
     * Supports SemVer expressions and wildcards described in
     * <a href="https://github.com/zafarkhaja/jsemver#external-dsl">Java SemVer - External DSL</a>
     * <br/>
     * Note: Expressions and wildcards are supported only for stable versions, ex '1.*' or '1.2.*'. For unstable versions
     * (versions with pre-release or build) it searches suitable version by full match.
     *
     * @param pluginId    plugin id
     * @param versionExpr SemVer version or expression with wildcards, can be empty or null
     * @return the latest suitable version
     * @throws PluginNotFoundFault if plugin with given plugin and version doesn't exist
     * @throws VersionParserFault  if version contains unexpected symbols
     */
    @Trace(async = true)
    public Mono<String> findLatestVersion(final UUID pluginId, final String versionExpr) {
        if (Strings.isNullOrEmpty(versionExpr)) {
            logger.info("Version is not defined in request. Fetching the latest version if exists. for pluginId: {}",
                    pluginId);
            return fetchById(pluginId)
                    .filter(p -> p.getLatestVersion() != null)
                    .single()
                    .map(PluginSummary::getLatestVersion)
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnError(NoSuchElementException.class, t -> {
                        throw new PluginNotFoundFault(String.format("Version '%s' for plugin_id='%s' does not exist",
                                versionExpr, pluginId));
                    });
        }

        return pluginGateway.fetchAllPluginVersionsById(pluginId)
                .map(SemVersion::from)
                .collectList()
                .map((List<SemVersion> list) -> {
                    SemVerExpression expr = SemVerExpression.from(versionExpr);
                    Optional<SemVersion> first = list.stream().filter(v -> v.satisfies(expr)).findFirst();
                    return first.orElseThrow(() -> new PluginNotFoundFault(String.format("Version '%s' for plugin_id='%s' does not exist",
                            versionExpr, pluginId)));

                })
                .map(SemVersion::toString);
    }

    /**
     * Appends paths (ex. entry points, screenshots urls) with host name, so these resources can be accessible using the paths.
     *
     * @param plugin the plugin
     * @return plugin object with updated paths
     */
    private PluginPayload updateUrls(final PluginPayload plugin) {
        PluginManifest manifest = plugin.getManifest();
        if (manifest == null) {
            return plugin;
        }
        if (plugin.getEntryPoints() != null) {
            plugin.getEntryPoints().forEach(view ->
                    view.setEntryPointPath(buildPublicUrl(manifest, Urls.concat(view.getPublicDir(), view.getEntryPointPath()))));
        }
        if (manifest.getScreenshots() != null) {
            manifest.setScreenshots(
                    manifest.getScreenshots().stream().map(scr -> buildPublicUrl(manifest, scr)).collect(Collectors.toSet())
            );
        }
        if (manifest.getThumbnail() != null) {
            manifest.setThumbnail(buildPublicUrl(manifest, manifest.getThumbnail()));
        }
        if (manifest.getGuide() != null) {
            manifest.setGuide(buildPublicUrl(manifest, manifest.getGuide()));
        }
        return plugin;
    }

    /**
     * Returns a path to access plugin file stored on S3 bucket
     *
     * @param manifest the given plugin
     * @param fileName file name
     * @return a path for a file
     */
    String buildPublicUrl(final PluginManifest manifest, final String fileName) {
        checkNotNull(this.pluginConfig.get(), "Plugin Configuration is not defined");
        return String.format("%s/%s", pluginConfig.get().getDistributionPublicUrl(), manifest.getBuildFilePath(manifest, fileName));
    }

    /**
     * Build the list of manifest view
     *
     * @param files          a map of file from which the entry point content is extracted
     * @param pluginManifest the plugin manifest object
     * @param views          map of distinct entry point views
     * @return a {@link List} of manifest view
     * @throws IOException when any I/O operation fails
     */
    private List<ManifestView> buildManifestViews(final Map<String, File> files,
                                                  final PluginManifest pluginManifest, final Map<String, Object> views) throws IOException {
        // create a list of manifest view objects
        return views.entrySet().stream().map(entry -> {
            try {
                ManifestView manifestView = om.readValue(om.writeValueAsString(entry.getValue()), ManifestView.class)
                        .setContext(entry.getKey())
                        .setPluginId(pluginManifest.getPluginId())
                        .setVersion(pluginManifest.getVersion());
                File entryPoint = files.get(Urls.concat(manifestView.getPublicDir(), manifestView.getEntryPointPath()));

                if (entryPoint == null) {
                    throw new IllegalArgumentException(String.format("File `%s` not found", manifestView.getEntryPointPath()));
                }

                try (Stream<String> lines = java.nio.file.Files.lines(Paths.get(entryPoint.getAbsolutePath()))) {
                    return manifestView.setEntryPointData(lines.collect(Collectors.joining()));
                }
            } catch (IOException e) {
                logger.error("Failed to parse the entry point `{}` {}", entry.getKey(), e.getMessage());
                return null;
            }
        }).collect(Collectors.toList());
    }

    /**
     * Extract a map of distinct entry point views from the manifest string content
     *
     * @param manifestContent the manifest file string content
     * @return a {@link Map} of context name -> entry point objects
     * @throws IOException              when any I/O operation fails
     * @throws IllegalArgumentException when the `views` entry is not present in the manifest
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getDistinctEntryPoints(final String manifestContent) throws IOException {
        Map<String, Object> manifestMap = om.readValue(manifestContent, Map.class);
        Object viewsObject = manifestMap.get("views");
        checkArgument(viewsObject != null, "entry point views required");
        String viewsContent = om.writeValueAsString(viewsObject);
        return om.readValue(viewsContent, Map.class);
    }

    /**
     * Build a plugin version from a version string. The version is extracted into a {@link SemVersion} object
     * then the {@link #validate} method checks that the version is valid.
     *
     * @param manifest the {@link PluginManifest} object that holds the version string
     * @return a {@link PluginVersion} object
     * @throws VersionParserFault when failing to parse the version or the version is not valid
     */
    PluginVersion buildPluginVersion(final PluginManifest manifest) throws VersionParserFault, PluginPublishException {
        SemVersion version = SemVersion.from(manifest.getVersion());

        int major = version.getMajorVersion();
        SemVersion latest = pluginGateway.fetchAllPluginVersionByIdMajor(manifest.getPluginId(), major)
                .take(1)
                .singleOrEmpty()
                .map(SemVersion::from)
                .block();

        validate(manifest, version, latest);
        // if manifest major version is less than 1, then ignore validating minor or patch release version
        if (latest != null && version.getMajorVersion() >= 1) {
            validateMinorOrPatchVersionRelease(manifest, latest);
        }
        return version.toPluginVersion(manifest.getPluginId(), System.currentTimeMillis());
    }

    /**
     * Validate a plugin version. The plugin version table is queried by major release and validated against the
     * manifest version. For an existing major release a valid version should always be greater than the latest.
     *
     * @param manifest the plugin manifest object that holds the version
     * @param version  the manifest version parsed to a {@link SemVersion} object
     * @param latest  the manifest latest version parsed to a {@link SemVersion} object
     * @throws IllegalArgumentException when the version does not meet the validation criteria
     */
    private void validate(final PluginManifest manifest, final SemVersion version, final SemVersion latest) {
        if (latest != null && !version.greaterThan(latest)) {
            throw new IllegalArgumentException(String.format("New version '%s' for plugin '%s' should be greater than " +
                    "the existing '%s'", manifest.getVersion(), manifest.getPluginId(), latest.toString()));
        }
    }

    /**
     * Returns list of all plugin versions in descending order (the latest is the first)
     *
     * @param pluginId a plugin id
     * @return {@link Flux} of plugin versions
     */
    public Flux<PluginVersion> getPluginVersions(UUID pluginId) {
        checkNotNull(pluginId);

        return pluginGateway.fetchAllPluginVersionsById(pluginId);
    }

    /**
     * Returns list of all accounts who have direct access to plugin
     *
     * @param pluginId plugin id
     * @return list of PluginAccountCollaborator
     */
    public Flux<PluginAccountCollaborator> findAccountCollaborators(final UUID pluginId) {
        checkNotNull(pluginId, "missing pluginId");
        return pluginAccessGateway.fetchAccounts(pluginId);
    }

    /**
     * Returns list of all teams who have access to plugin
     *
     * @param pluginId plugin id
     * @return list of PluginTeamCollaborator
     */
    public Flux<PluginTeamCollaborator> findTeamCollaborators(final UUID pluginId) {
        checkNotNull(pluginId, "missing pluginId");
        return pluginAccessGateway.fetchTeams(pluginId);
    }

    /**
     * Find a plugin account collaborator.
     *
     * @param pluginId  the plugin id to search for. Annotated with {@link Nonnull}
     * @param accountId the account id to find. Annotated with {@link Nonnull}
     * @return a {@link Mono} of {@link PluginAccountCollaborator}
     */
    public Mono<PluginAccountCollaborator> findAccountCollaborator(@Nonnull final UUID pluginId, @Nonnull final UUID accountId) {
        return pluginAccessGateway.fetchAccount(pluginId, accountId);
    }

    /**
     * Find a plugin team collaborator.
     *
     * @param pluginId the plugin id to search for. Annotated with {@link Nonnull}
     * @param teamId   the team id to find. Annotated with {@link Nonnull}
     * @return a {@link Mono} of {@link PluginTeamCollaborator}
     */
    public Mono<PluginTeamCollaborator> findTeamCollaborator(@Nonnull final UUID pluginId, @Nonnull final UUID teamId) {
        return pluginAccessGateway.fetchTeamCollaborator(pluginId, teamId);
    }

    /**
     * Delete a plugin. It deletes a plugin from plugin listings, removes all permissions for a plugin and marks a plugin as deleted.
     * @param accountId the account performing a deletion
     * @param pluginId the plugin to delete
     */
    public Flux<Void> deletePlugin(final UUID accountId, final UUID pluginId) {
        UUID deletedId = UUIDs.timeBased();

        DeletedPlugin deletionInfo = new DeletedPlugin()
                .setId(deletedId)
                .setPluginId(pluginId)
                .setAccountId(accountId);

        return pluginGateway.fetchPluginSummaryById(pluginId)
                .map(s -> s.setDeletedId(deletedId))
                .flatMapMany(pluginGateway::persistSummary)
                .thenMany(pluginGateway.persist(deletionInfo))
                .thenMany(pluginPermissionService.deleteAccountPermissions(pluginId))
                .thenMany(pluginPermissionService.deleteTeamPermissions(pluginId));
    }

    /**
     * Find plugin manifest by plugin id and plugin version expression.
     * Return the latest plugin version that matches the version expression.
     *
     * @param pluginId      the plugin id
     * @param pluginVersion the version expression
     * @return Mono of PluginManifest
     * @throws PluginNotFoundFault if plugin with given plugin and version doesn't exist
     * @throws VersionParserFault  if version contains unexpected symbols
     */
    @Trace(async = true)
    public Mono<PluginManifest> findPluginManifest(final UUID pluginId, final String pluginVersion) {
        checkArgument(pluginId != null, "missing pluginId");
        checkArgument(StringUtils.isNotEmpty(pluginVersion), "missing pluginVersion");

        return findLatestVersion(pluginId, pluginVersion)
                .flatMap(v -> pluginGateway.fetchPluginManifestByIdVersion(pluginId, v));
    }

    /**
     * Fetch output schema for plugin
     *
     * @param pluginId the plugin id
     * @param pluginVersion the plugin version expression
     * @return Mono of String, empty mono if output schema is empty
     */
    @Trace(async = true)
    public Mono<String> findOutputSchema(final UUID pluginId, final String pluginVersion) {
        return findPluginManifest(pluginId, pluginVersion)
                .filter(p -> p.getOutputSchema() != null)
                .map(PluginManifest::getOutputSchema);
    }

    /**
     * Validate minor or patch release plugin version to prevent destructive schema updates on plugin publish
     *
     * @param pluginManifest  the {@link PluginManifest} object that holds the plugin information
     * @param latestVersion the manifest latest version parsed to a {@link SemVersion} object
     */
    private void validateMinorOrPatchVersionRelease(final PluginManifest pluginManifest, final SemVersion latestVersion) throws PluginPublishException {
        String version = String.valueOf(latestVersion.getMajorVersion()) + "."
                + String.valueOf(latestVersion.getMinorVersion()) + "."
                + String.valueOf(latestVersion.getPatchVersion());

        String latestConfigSchema = pluginGateway.fetchPluginManifestByIdVersion(pluginManifest.getPluginId(), version)
                .map(PluginManifest::getConfigurationSchema)
                .block();
        try {
            schemaValidationService.validateLatestSchemaAgainstManifestSchema(latestConfigSchema, pluginManifest.getConfigurationSchema());
        } catch (JSONException ex) {
            logger.debug("Invalid json while validating minor and patch version" + ex.getMessage());
            throw new PluginPublishException(String.format("Invalid json while validating minor and patch version %s", ex.getMessage()));
        }
    }

    /**
     * Returns Set of PluginSearchableField representing vales of the 'searchable' field from plugin manifest.
     *
     * @param searchableNode JsonNode of the searchable object.
     *
     * @throws IOException when any I/O operation fails
     */
    private List<PluginSearchableField> parseSearchableJson(final JsonNode searchableNode, final PluginManifest manifest)
            throws IOException, PluginPublishException {
        List<PluginSearchableField> searchableFields = new ArrayList<>();

        // Return empty set if 'searchable' node itself is missing. Field is now optional.
        if(searchableNode.isMissingNode()) {
            return searchableFields;
        }

        // If present, must be array to keep manifest consistent
        if(!searchableNode.isArray()) {
            throw new PluginPublishException("Field 'searchable' should be an array");
        }

        String schema = manifest.getConfigurationSchema();

        for(JsonNode s: searchableNode) {
            PluginSearchableField field = new PluginSearchableField();

            // Extract the contentType
            JsonNode contentType = s.path("contentType");
            if(contentType.isMissingNode()) {
                throw new PluginPublishException("Searchable object is missing contentType field");
            }
            field.setContentType(contentType.asText());

            // Extract known SearchableFields defined in CSG bronte index mapping
            Set<String> summary = validateSearchbleValues(getSearchableFieldValue("summary", s), schema);
            Set<String> body = validateSearchbleValues(getSearchableFieldValue("body", s), schema);
            Set<String> source = validateSearchbleValues(getSearchableFieldValue("source", s), schema);
            Set<String> preview = validateSearchbleValues(getSearchableFieldValue("preview", s), schema);
            Set<String> tag = validateSearchbleValues(getSearchableFieldValue("tag", s), schema);

            field.setSummary(summary);
            field.setBody(body);
            field.setSource(source);
            field.setPreview(preview);
            field.setTag(tag);

            //add common fields
            field.setPluginId(manifest.getPluginId());
            field.setName(manifest.getName());
            field.setVersion(manifest.getVersion());
            field.setId(UUIDs.timeBased());

            searchableFields.add(field);
        }

        return searchableFields;
    }

    private Set<String> getSearchableFieldValue(final String fieldName, final JsonNode node) throws IOException {
        JsonNode f = node.path(fieldName);
        Set<String> set = new HashSet<>();

        if(f.isArray()) {
            ObjectReader reader = om.readerFor(new TypeReference<Set<String>>() {});
            set.addAll(reader.readValue(f));
        } else {
            String value = f.asText();
            if(!value.equals("")) {
                set.add(value);
            };
        }

        return set;
    }

    /**
     * Just wraps {@link PluginService#getConfigurationSchemaFieldType(String, String)} as a helper for validation
     * ignores the returned type but passes along checked exceptions.
     *
     * @returns searchableFields unchanged to emable builder pattern.
     */
    private Set<String> validateSearchbleValues(final Set<String> searchableFieldValues,
                                                final String configurationSchema)
            throws PluginPublishException {

        // This throws if it fails validation
        for (String searchableFieldValue : searchableFieldValues) {
            getConfigurationSchemaFieldType(searchableFieldValue, configurationSchema);
        }
        return searchableFieldValues;
    }

    /**
     * Validate searchable field exists in configuration schema or throws.
     * Returns the `type` field present in the schema definition of given searchableField for further validation.
     *
     * Validation and type discover is done by looking for the context path of the field declaration in
     * schema, this has to contain a inner field `type`.
     * In addition, schema path wraps nodes in a ".properties' object if the parent node is of type 'group'
     * So a manifest path 'options.foo' in the schema will be validated against:
     *
     * <p>
     * <pre>
     *   {
     *      options: {
     *         type: group,
     *           properties: {
     *              foo: {
     *              type: text,
     *                 otherFields: bar
     *           }
     *      }
     *   }
     * </pre>
     * </p>
     *
     *  therefore, ".options.properties.foo is the json context path that needs to exist in the schema
     *
     * @param searchableField     searchable field name or context path to lookup to lookup
     * @param configurationSchema the configuration schema string content
     * @return
     * @throws PluginPublishException when something if it fails to validate field against schema or schema is missing
     *                                type field.
     */
    private String getConfigurationSchemaFieldType(final String searchableField, final String configurationSchema)
            throws PluginPublishException {
        try {

            checkNotNull(searchableField, "searchableField is null");
            checkNotNull(configurationSchema, "configurationSchema is null");

            // Load up plugin schema into a navigable json tree
            JsonNode schema = om.readTree(configurationSchema);

            // Reminder: JsonNode.at() methods takes as parameter context path with `/` separator, so
            // `options.foo.type` is addressed as `/options/foo/type`
            Iterator<String> iter = Arrays.asList(searchableField.split("\\.")).iterator();
            StringBuilder schemaContextPath = new StringBuilder();
            while (iter.hasNext()) {
                String token = iter.next();

                JsonNode field = schema.at(schemaContextPath
                        .append("/")
                        .append(token)
                        .toString());
                JsonNode type = getJsonNodeInnerType(searchableField, field);

                // stage is a reserved schema field, no validation required for it
                if((schemaContextPath.toString().equals("/stage"))) {
                    return "stage";
                }

                if (type.asText().equalsIgnoreCase("list")) {

                    JsonNode listType = field.get("listType");
                    if (listType == null) {
                        throw new PluginPublishException(
                                String.format("field %s is of type list but does have a listType", token));
                    }
                    // listType can be either a string that contains the type of input or a JSON object that
                    //contains the property type
                    if (listType.isObject()) {
                        schemaContextPath.append("/listType/properties");
                    } else {
                        return listType.asText();
                    }
                }

                if (type.asText().equalsIgnoreCase("group")) {
                    schemaContextPath.append("/properties");
                }
            }

            JsonNode field = schema.at(schemaContextPath.toString());
            return getJsonNodeInnerType(searchableField, field).asText();

        } catch (NullPointerException | IllegalArgumentException e) {
            throw new PluginPublishException(String.format("Failed determining type of searchable field %s: %s",
                    searchableField, e.getMessage()));
        } catch (IOException ex) {
            throw new PluginPublishException(String.format("Unable to parse configuration schema. %s", ex.getMessage()));
        }
    }

    /**
     * Gets "type" field from json node.
     * Useful to valide that field actually exists in schema
     *
     * @param jsonPointer
     * @param field
     * @return
     * @throws PluginPublishException
     */
    private JsonNode getJsonNodeInnerType(final String jsonPointer, final JsonNode field) throws PluginPublishException {
        if (field.isMissingNode()) {
            throw new PluginPublishException(
                    String.format("Searchable field doesn't exist in configuration schema: %s", jsonPointer));

        }
        JsonNode type = field.get("type");
        if (type == null) {
            throw new PluginPublishException(
                    String.format("Schema definition of searchable field '%s' is missing 'type'", jsonPointer));
        }
        return type;
    }

    /**
     * Fetch the LTI Provider Credential
     *
     * @param pluginId       the plugin id
     * @param ltiProviderKey the specified provider key
     * @return Mono of LTIProviderCredentia, empty mono if key/plugin not found
     */
    public Mono<LTIProviderCredential> findLTIProviderCredential(final UUID pluginId, final String ltiProviderKey) {
        return pluginGateway.fetchLTIProviderCredential(pluginId, ltiProviderKey);
    }


     /** Creates an entry of LTI Provider Credential
     *
     * @param key the key from LTI Provider
     * @param secretKey the secret obtained from LTI from Provider
     * @param pluginId the plugin id the lti credentials are associated to
     * @param fields a set of allowed fields
     * @return a newly created LTIProviderCredential Object
     * @throws IllegalArgumentFault when any argument is invalid or not supplied
     * @throws ConflictFault if the credentials already exists
     */
    @Trace(async = true)
     public Mono<LTIProviderCredential> createLTIProviderCredential(final String key,
                                                                   final String secretKey,
                                                                   final UUID pluginId,
                                                                   final Set<String> fields) {
        affirmArgumentNotNullOrEmpty(key, "key is required");
        affirmArgumentNotNullOrEmpty(secretKey, "secretKey is required");
        affirmNotNull(pluginId, "pluginId is required");
        affirmNotNull(fields, "fields is required");

        return pluginGateway.fetchLTIProviderCredential(pluginId, key)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .hasElement()
                .flatMap(hasElement -> {
                    if (hasElement) {
                        return Mono.error(new ConflictFault("lti credentials already exist"));
                    }

                    final UUID id = UUIDs.timeBased();
                    final LTIProviderCredential ltiProviderCredential = new LTIProviderCredential()
                            .setId(id)
                            .setKey(key)
                            .setSecret(secretKey)
                            .setPluginId(pluginId)
                            .setAllowedFields(fields);

                    return pluginGateway.persistLTIPluginCredentials(ltiProviderCredential)
                            .then(Mono.just(ltiProviderCredential));
                });
    }

    /**
     * Deleting of LTI Existing LTI Provider Credential from datastore
     *
     * @param key the key from LTI Provider
     * @param pluginId plugin id
     * @return return empty Mono Object, if the delete is successful
     * @throws IllegalArgumentFault when either key or pluginId are missing
     */
    @Trace(async = true)
    public Mono<Void> deleteLTIProviderCredential(final String key,
                                                  final UUID pluginId) {

        affirmArgument(!Strings.isNullOrEmpty(key), "key is required");
        affirmNotNull(pluginId, "pluginId is required");
        return pluginGateway.deleteLTIProviderCredential(new LTIProviderCredential()
                .setKey(key)
                .setPluginId(pluginId))
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Returns resolved plugin version if lockPluginVersionEnabled is true otherwise returns pluginVersionExpr.
     * @param pluginId
     * @param pluginVersionExpr
     * @param lockPluginVersionEnabled
     * @return plugin version
     */
    public String resolvePluginVersion(UUID pluginId, String pluginVersionExpr, boolean lockPluginVersionEnabled) {
        if (lockPluginVersionEnabled) {
            return findLatestVersion(pluginId, pluginVersionExpr).block();
        }

        return pluginVersionExpr;
    }

    /**
     * Update plugin summary details
     * @param pluginId the plugin id
     * @param publishMode the publish mode
     * @return mono of plugin summary
     */
    public Mono<PluginSummary> updatePluginSummary(final UUID pluginId, final PublishMode publishMode) {
        affirmArgument(pluginId != null, "missing pluginId");
        affirmArgument(publishMode != null, "missing publish mode");

        PluginSummary pluginSummary = new PluginSummary()
                .setId(pluginId)
                .setPublishMode(publishMode);
        return pluginGateway.updatePluginSummary(pluginSummary)
                .then(pluginGateway.fetchPluginSummaryById(pluginId));
    }

    /**
     * Delete the plugin version
     *
     * @param pluginId the plugin id
     * @param version the version to be deleted
     * @return flux of void
     * @throws MethodNotAllowedFault when either plugin version is not unpublished or it is referenced to any element
     */
    @Trace(async = true)
    public Flux<Void> deletePluginVersion(final UUID pluginId, final String version) {
        affirmNotNull(pluginId, "pluginId is required");
        affirmNotNull(version, "plugin version is required");
        SemVersion versionToDelete = SemVersion.from(version);
        String pluginVersionExprWithMajorMinorPatch = String.format("%d.%d.%d", versionToDelete.getMajorVersion(),
                                                                    versionToDelete.getMinorVersion(),
                                                                    versionToDelete.getPatchVersion());
        String pluginVersionExprWithMajorMinor = String.format("%d.%d.*", versionToDelete.getMajorVersion(),
                                                               versionToDelete.getMinorVersion());
        String pluginVersionExprWithMajor = String.format("%d.*", versionToDelete.getMajorVersion());
        Mono<PluginSummary> pluginSummaryMono = pluginGateway.fetchPluginSummaryById(pluginId).doOnEach(ReactiveTransaction.linkOnNext());
        // Check that the plugin version is unpublished
        Flux<PluginVersion> pluginVersionByMajorMinorPatch = pluginGateway.fetchAllPluginVersionByIdMajorMinorPatch(pluginId,
                                                                                                                    versionToDelete.getMajorVersion(),
                                                                                                                    versionToDelete.getMinorVersion(),
                                                                                                                    versionToDelete.getPatchVersion())
                .doOnEach(ReactiveTransaction.linkOnNext())
                //All plugin versions with major, minor and patch will be considered for delete(across all release dates)
                .map(pluginVersion -> {
                    if(!pluginVersion.getUnpublished()) {
                        throw new MethodNotAllowedFault("Unpublishing the plugin version is required to delete");
                    }
                    return pluginVersion;
                });

        Flux<PluginVersion> pluginVersionByMajorMinorPatchFlux = pluginSummaryMono
                .flux()
                .flatMap(pluginSummary -> {
                    CoursewareElementType type = getCoursewareElementFromPluginType(pluginSummary.getType());
                    switch (type) {
                        case ACTIVITY:
                            return activityGateway.findActivityIdsByPluginIdAndVersion(pluginId, pluginVersionExprWithMajorMinorPatch);
                        case INTERACTIVE:
                            return interactiveGateway.findInteractiveIdsByPluginIdAndVersion(pluginId, pluginVersionExprWithMajorMinorPatch);
                        case COMPONENT:
                            return componentGateway.findComponentIdsByPluginIdAndVersion(pluginId, pluginVersionExprWithMajorMinorPatch);
                        default:
                            return Flux.error(new UnsupportedOperationException("Unsupported courseware element type for plugin"));
                    }
                })
                .hasElements()
                .flatMapMany(hasElement -> {
                    //throws an error when plugin version is referenced to any courseware element
                    if(hasElement) {
                        return Mono.error(new MethodNotAllowedFault("Plugin version has been referenced to courseware element"));
                    }
                    return pluginVersionByMajorMinorPatch;
                });

        Flux<Void> pluginVersionByMajorMinorFlux = pluginSummaryMono
                .flux()
                .flatMap(pluginSummary -> {
                    CoursewareElementType type = getCoursewareElementFromPluginType(pluginSummary.getType());
                    switch (type) {
                        case ACTIVITY:
                            return activityGateway.findActivityIdsByPluginIdAndVersion(pluginId,pluginVersionExprWithMajorMinor);
                        case INTERACTIVE:
                            return interactiveGateway.findInteractiveIdsByPluginIdAndVersion(pluginId, pluginVersionExprWithMajorMinor);
                        case COMPONENT:
                            return componentGateway.findComponentIdsByPluginIdAndVersion(pluginId, pluginVersionExprWithMajorMinor);
                        default:
                            return Flux.error(new UnsupportedOperationException("Unsupported courseware element type for plugin"));
                    }
                })
                .hasElements()
                .flatMapMany(hasElement -> findCountByVersionExpr(pluginId, pluginVersionExprWithMajorMinor)
                        .flatMap(count -> {
                            //throws an error when plugin version is referenced to any courseware element
                            if (hasElement && count < 1) {
                                return Mono.error(new MethodNotAllowedFault(String.format(
                                        "Plugin version has been referenced to courseware element with %d.%d.* version",
                                        versionToDelete.getMajorVersion(),
                                        versionToDelete.getMinorVersion())));
                            }
                            return Mono.empty();
                        }));

        Flux<Void> pluginVersionByMajorFlux = pluginSummaryMono
                .flux()
                .flatMap(pluginSummary -> {
                    CoursewareElementType type = getCoursewareElementFromPluginType(pluginSummary.getType());
                    switch (type) {
                        case ACTIVITY:
                            return activityGateway.findActivityIdsByPluginIdAndVersion(pluginId,pluginVersionExprWithMajor);
                        case INTERACTIVE:
                            return interactiveGateway.findInteractiveIdsByPluginIdAndVersion(pluginId, pluginVersionExprWithMajor);
                        case COMPONENT:
                            return componentGateway.findComponentIdsByPluginIdAndVersion(pluginId, pluginVersionExprWithMajor);
                        default:
                            return Flux.error(new UnsupportedOperationException("Unsupported courseware element type for plugin"));
                    }
                })
                .hasElements()
                .flatMapMany(hasElement -> findCountByVersionExpr(pluginId, pluginVersionExprWithMajor)
                        .flatMap(count -> {
                            //throws an error when plugin version is referenced to any courseware element
                            if (hasElement && count < 1) {
                                return Mono.error(new MethodNotAllowedFault(String.format(
                                        "Plugin version has been referenced to courseware element with %d.* version",
                                        versionToDelete.getMajorVersion())));
                            }
                            return Mono.empty();
                        }));

        return pluginVersionByMajorMinorPatchFlux
                .thenMany(pluginVersionByMajorMinorFlux)
                .thenMany(pluginVersionByMajorFlux)
                .thenMany(pluginVersionByMajorMinorPatch)
                .flatMap(pluginGateway::deletePluginVersion);
    }

    private CoursewareElementType getCoursewareElementFromPluginType(PluginType type) {
        switch(type) {
            case UNIT :
            case LESSON :
            case COURSE :
                return ACTIVITY;
            case COMPONENT :
                return COMPONENT;
            case SCREEN :
                return INTERACTIVE;
            default:
                throw new UnsupportedOperationException("Unsupported plugin type");
        }
    }

    public Mono<Long> findCountByVersionExpr(final UUID pluginId, final String versionExpr) {
        return pluginGateway.fetchAllPluginVersionsById(pluginId)
                .filter(version -> !version.getUnpublished())
                .map(SemVersion::from)
                .collectList()
                .map((List<SemVersion> list) -> {
                    SemVerExpression expr = SemVerExpression.from(versionExpr);
                    return  list.stream().filter(v -> v.satisfies(expr)).count();
                });
    }

    /**
     * Fetch plugin filters by plugin id and version expression
     * @param pluginId the plugin id
     * @param versionExpr the plugin version expression
     * @return flux of plugin filter object
     */
    @Trace(async = true)
    public Mono<List<PluginFilter>> fetchPluginFiltersByIdVersionExpr(final UUID pluginId, final String versionExpr) {
        return findLatestVersion(pluginId,
                                 versionExpr)
                .flatMap(version -> pluginGateway.fetchPluginFiltersByIdVersion(pluginId, version).collectList())
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Return Plugin summary payloads based on plugin filters, if it is empty then return payload as it is.
     * @param pluginSummaryPayloads the plugin summary payloads
     * @param pluginFilters the list of plugin filters
     * @return filtered list of plugin summary payloads
     */
    public List<PluginSummaryPayload> filterPluginSummaryPayloads(final List<PluginSummaryPayload> pluginSummaryPayloads,
                                                                  final List<PluginFilter> pluginFilters) {
        if (pluginFilters != null && pluginFilters.size() > 0) {

            List<String> pluginIds = pluginFilters.stream()
                    .filter(pluginFilter -> pluginFilter.getFilterType().equals(PluginFilterType.ID)
                            || pluginFilter.getFilterType().equals(PluginFilterType.TAGS))
                    .map(pluginFilter -> pluginFilter.getFilterValues())
                    .collect(Collectors.toList()).stream()
                    .flatMap(ids -> ids.stream())
                    .collect(Collectors.toList());

            return pluginSummaryPayloads.stream()
                    .filter(p -> (p.getPluginId() != null && pluginIds.contains(p.getPluginId().toString()))
                    || (p.getTags() != null && pluginIds.stream().anyMatch(element -> p.getTags().contains(element))))
                    .collect(Collectors.toList());
        }
        return pluginSummaryPayloads;
    }

}
