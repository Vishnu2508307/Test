package com.smartsparrow.plugin.data;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.reactivestreams.Publisher;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.dse.api.Mutators;
import com.smartsparrow.dse.api.ResultSets;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Persist layer api for plugins
 */
@Singleton
public class PluginGateway {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PluginGateway.class);

    private final Session session;

    private final ViewByPluginVersionMaterializer viewByPluginVersionMaterializer;
    private final ViewByPluginVersionMutator viewByPluginVersionMutator;
    private final VersionByPluginMaterializer versionByPluginMaterializer;
    private final VersionByPluginMutator versionByPluginMutator;
    private final ManifestByPluginMaterializer manifestByPluginMaterializer;
    private final ManifestByPluginMutator manifestByPluginMutator;
    private final PluginSummaryMaterializer pluginSummaryMaterializer;
    private final PluginSummaryMutator pluginSummaryMutator;
    private final DeletedPluginMutator deletedPluginMutator;
    private final SearchableFieldByPluginMutator searchableFieldByPluginMutator;
    private final SearchableFieldByPluginMaterializer searchableFieldByPluginMaterializer;
    private final LTIProviderCredentialByPluginMaterializer ltiProviderCredentialByPluginMaterializer;
    private final LTIProviderCredentialByPluginMutator ltiProviderCredentialByPluginMutator;
    private final FilterByPluginVersionMutator filterByPluginVersionMutator;
    private final FilterByPluginVersionMaterializer filterByPluginVersionMaterializer;

    @Inject
    public PluginGateway(Session session,
                         ViewByPluginVersionMaterializer viewByPluginVersionMaterializer,
                         ViewByPluginVersionMutator viewByPluginVersionMutator,
                         VersionByPluginMaterializer versionByPluginMaterializer,
                         VersionByPluginMutator versionByPluginMutator,
                         ManifestByPluginMaterializer manifestByPluginMaterializer,
                         ManifestByPluginMutator manifestByPluginMutator,
                         PluginSummaryMaterializer pluginSummaryMaterializer,
                         PluginSummaryMutator pluginSummaryMutator,
                         DeletedPluginMutator deletedPluginMutator,
                         LTIProviderCredentialByPluginMaterializer ltiProviderCredentialByPluginMaterializer,
                         LTIProviderCredentialByPluginMutator ltiProviderCredentialByPluginMutator,
                         SearchableFieldByPluginMutator searchableFieldByPluginMutator,
                         SearchableFieldByPluginMaterializer searchableFieldByPluginMaterializer,
                         FilterByPluginVersionMutator filterByPluginVersionMutator,
                         FilterByPluginVersionMaterializer filterByPluginVersionMaterializer) {
        this.session = session;
        this.viewByPluginVersionMaterializer = viewByPluginVersionMaterializer;
        this.viewByPluginVersionMutator = viewByPluginVersionMutator;
        this.versionByPluginMaterializer = versionByPluginMaterializer;
        this.versionByPluginMutator = versionByPluginMutator;
        this.manifestByPluginMaterializer = manifestByPluginMaterializer;
        this.manifestByPluginMutator = manifestByPluginMutator;
        this.pluginSummaryMaterializer = pluginSummaryMaterializer;
        this.pluginSummaryMutator = pluginSummaryMutator;
        this.deletedPluginMutator = deletedPluginMutator;
        this.searchableFieldByPluginMutator = searchableFieldByPluginMutator;
        this.searchableFieldByPluginMaterializer = searchableFieldByPluginMaterializer;
        this.ltiProviderCredentialByPluginMaterializer = ltiProviderCredentialByPluginMaterializer;
        this.ltiProviderCredentialByPluginMutator = ltiProviderCredentialByPluginMutator;
        this.filterByPluginVersionMutator = filterByPluginVersionMutator;
        this.filterByPluginVersionMaterializer = filterByPluginVersionMaterializer;
    }

    /**
     * Persist the plugin summary
     *
     * @param pluginSummary the plugin summary to persist
     */
    @Trace(async = true)
    public Flux<Void> persistSummary(final PluginSummary pluginSummary) {
        Flux<? extends Statement> iter = Mutators.upsert(pluginSummaryMutator, pluginSummary);
        return Mutators.execute(session, iter)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving plugin summary %s", pluginSummary.toString()),
                              throwable);
                    throw Exceptions.propagate(throwable);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist the plugin summary
     *
     * @param pluginSummary publisher that emits the plugin summaries to persist
     */
    public Flux<Void> persistSummary(final Mono<PluginSummary> pluginSummary) {
        Flux<? extends Statement> iter = Mutators.upsert(pluginSummaryMutator, pluginSummary);
        return Mutators.execute(session, iter)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving plugin summary %s", pluginSummary.toString()),
                              throwable);
                    throw Exceptions.propagate(throwable);
                });
    }


    /**
     * Persist the plugin manifest
     *
     * @param pluginManifest the plugin manifest to persist
     */
    public Flux<Void> persistManifest(final PluginManifest pluginManifest) {
        Flux<? extends Statement> iter = Mutators.upsert(manifestByPluginMutator, pluginManifest);
        return Mutators.execute(session, iter)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving a plugin manifest %s", pluginManifest.toString()),
                              throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist the plugin version
     *
     * @param pluginVersion the plugin version to persist
     */
    public Flux<Void> persistVersion(final PluginVersion pluginVersion) {
        Flux<? extends Statement> iter = Mutators.upsert(versionByPluginMutator, pluginVersion);
        return Mutators.execute(session, iter)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving a plugin version %s", pluginVersion.toString()),
                              throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    public Flux<Void> persistVersion(final Mono<PluginVersion> pluginVersion) {
        Flux<? extends Statement> iter = Mutators.upsert(versionByPluginMutator, pluginVersion);
        return Mutators.execute(session, iter)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving a plugin version %s", pluginVersion.toString()),
                              throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Un publish a plugin version
     * @param pluginVersion the plugin version to un publish
     * @return
     */
    public Flux<Void> unPublishPluginVersion(final PluginVersion pluginVersion) {
        return Mutators.execute(session, Flux.just(versionByPluginMutator.unPublishPluginVersion(pluginVersion)))
                .doOnError(throwable -> {
                    log.error(String.format("error while unpublishing a plugin version %s",
                            pluginVersion), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist the manifest view
     *
     * @param manifestView the manifest view to persist
     */
    public Flux<Void> persistView(final ManifestView... manifestView) {
        Flux<? extends Statement> iter = Mutators.upsert(viewByPluginVersionMutator, manifestView);
        return Mutators.execute(session, iter)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving %s manifest views", manifestView.length), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Persist the manifest view
     *
     * @param manifestView the manifest view to persist
     */
    public Flux<Void> persistView(final Publisher<ManifestView> manifestView) {
        Flux<? extends Statement> iter = Mutators.upsert(viewByPluginVersionMutator, manifestView);
        return Mutators.execute(session, iter)
                .doOnError(throwable -> {
                    log.error("error while saving %s manifest views", throwable);
                    throw Exceptions.propagate(throwable);
                });
    }


    /**
     * Persist the plugin to a list of deleted plugins
     *
     * @param deletedPlugin the details about plugin deletion
     */
    public Flux<Void> persist(final DeletedPlugin deletedPlugin) {
        Flux<? extends Statement> iter = Mutators.upsert(deletedPluginMutator, deletedPlugin);
        return Mutators.execute(session, iter)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving plugin to a list of deleted plugins %s", deletedPlugin),
                              throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch a plugin summary by id
     *
     * @param id the id of the plugin
     * @return a {@link Mono} of plugin summary
     */
    @Trace(async = true)
    public Mono<PluginSummary> fetchPluginSummaryById(final UUID id) {
        return ResultSets.query(session, pluginSummaryMaterializer.fetchById(id))
                .flatMapIterable(row -> row)
                .map(pluginSummaryMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching plugin summary with id %s", id), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch all plugin manifests given a plugin id
     *
     * @param id the id of the plugin
     * @return a {@link Flux} of plugin manifest
     */
    public Flux<PluginManifest> fetchPluginManifestById(final UUID id) {
        return ResultSets.query(session, manifestByPluginMaterializer.fetchAllManifestByPlugin(id))
                .flatMapIterable(row -> row)
                .map(manifestByPluginMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching plugin manifest with pluginId %s", id), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch a plugin manifest by id and version
     *
     * @param id the id of the plugin
     * @param version the version of the plugin
     * @return a {@link Mono} of plugin manifest
     */
    @Trace(async = true)
    public Mono<PluginManifest> fetchPluginManifestByIdVersion(final UUID id, final String version) {
        return ResultSets.query(session, manifestByPluginMaterializer.fetchManifestByVersion(id, version))
                .flatMapIterable(row -> row)
                .map(manifestByPluginMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching plugin manifest with pluginId %s and version %s",
                                            id, version), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch a published plugin view by context
     *
     * @param id the id of the plugin
     * @param version the version of the published plugin
     * @param context the context of the view
     * @return a {@link Mono} of view manifest
     */
    @Trace(async = true)
    public Mono<ManifestView> fetchManifestViewByIdVersionContext(final UUID id, final String version, final String context) {
        return ResultSets.query(session, viewByPluginVersionMaterializer
                .fetchByPluginVersionContext(id, version, context))
                .flatMapIterable(row -> row)
                .map(this::mapRowToManifestView)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching published plugin with pluginId %s, version %s " +
                                                    "and context %s", id, version, context));
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch all views for a plugin
     *
     * @param pluginId the plugin id to search the views for
     * @param version the version of the plugin
     * @return
     */
    @Trace(async = true)
    public Flux<ManifestView> fetchViews(final UUID pluginId, final String version) {
        return ResultSets.query(session, viewByPluginVersionMaterializer
                .fetchViews(pluginId, version))
                .flatMapIterable(row -> row)
                .map(this::mapRowToManifestView)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching views for plugin with id %s and version %s",
                                            pluginId, version), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch all published version for a plugin
     * The method returns a collection ordered by DESC major, minor, patch and release date
     *
     * @param id the id of the plugin
     * @return a {@link Flux} of plugin version
     */
    @Trace(async = true)
    public Flux<PluginVersion> fetchAllPluginVersionsById(final UUID id) {
        return ResultSets.query(session, versionByPluginMaterializer.fetchAllPluginVersions(id))
                .flatMapIterable(row -> row)
                .map(this::mapRowToPluginVersion)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching versions for plugin with id %s", id), throwable);
                    throw Exceptions.propagate(throwable);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all plugin minor releases by major release
     * The method returns a collection ordered by DESC major, minor, patch and release date
     *
     * @param id the id of the plugin
     * @param major the major version number
     * @return a {@link Flux} of plugin version
     */
    public Flux<PluginVersion> fetchAllPluginVersionByIdMajor(final UUID id, final int major) {
        return ResultSets.query(session, versionByPluginMaterializer.fetchAllPluginMinorReleases(id, major))
                .flatMapIterable(row -> row)
                .map(this::mapRowToPluginVersion)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching minor releases by major release %s for pluginId %s",
                                            major, id), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch all released patches for a plugin given major and minor release
     * The method returns a collection ordered by DESC major, minor, patch and release date
     *
     * @param id the id of the plugin
     * @param major the major version number
     * @param minor the minor version number
     * @return a {@link Flux} of plugin version
     */
    public Flux<PluginVersion> fetchAllPluginVersionByIdMajorMinor(final UUID id, final int major, final int minor) {
        return ResultSets.query(session, versionByPluginMaterializer.fetchAllPluginPatches(id, major, minor))
                .flatMapIterable(row -> row)
                .map(this::mapRowToPluginVersion)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching patches for pluginId %s, major version %s and" +
                                                    "minor version %s", id, major, minor), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch all the available releases of a plugin given the version number
     * The method returns a collection ordered by DESC major, minor, patch and release date
     *
     * @param id the id of the plugin
     * @param major the major version number
     * @param minor the minor version number
     * @param patch the patch version number
     * @return a {@link Flux} of plugin version
     */
    @Trace(async = true)
    public Flux<PluginVersion> fetchAllPluginVersionByIdMajorMinorPatch(final UUID id, final int major, final int minor, final int patch) {
        return ResultSets.query(session, versionByPluginMaterializer.fetchAllPluginPreReleases(id, major, minor, patch))
                .flatMapIterable(row -> row)
                .map(this::mapRowToPluginVersion)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching pre-releases for pluginId %s, major version %s, " +
                                                    "minor version %s and patch version %s", id, major, minor, patch),
                              throwable);
                    throw Exceptions.propagate(throwable);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Fetch all the plugin summary.
     *
     * @return a {@link Flux} of plugin summary
     */
    public Flux<PluginSummary> fetchAllPluginSummary() {
        return ResultSets.query(session, pluginSummaryMaterializer.fetchAll())
                .flatMapIterable(row -> row)
                .map(pluginSummaryMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error("error while fetching all plugin summaries", throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Maps a row to a plugin version
     *
     * @param row the row to convert
     * @return {@link PluginVersion}
     */
    private PluginVersion mapRowToPluginVersion(Row row) {
        return new PluginVersion()
                .setPluginId(row.getUUID("plugin_id"))
                .setMajor(row.getInt("major"))
                .setMinor(row.getInt("minor"))
                .setPatch(row.getInt("patch"))
                .setPreRelease(row.getString("pre_release"))
                .setBuild(row.getString("build"))
                .setReleaseDate(row.getLong("release_date"))
                .setUnpublished(row.getBool("unpublished"));
    }

    /**
     * Maps a row to a manifest view
     *
     * @param row the row to convert
     * @return {@link ManifestView}
     */
    private ManifestView mapRowToManifestView(Row row) {
        return new ManifestView()
                .setPluginId(row.getUUID("plugin_id"))
                .setVersion(row.getString("version"))
                .setContext(row.getString("context"))
                .setEntryPointPath(row.getString("entry_point_path"))
                .setEntryPointData(row.getString("entry_point_data"))
                .setContentType(row.getString("content_type"))
                .setPublicDir(row.getString("public_dir"))
                .setEditorMode(row.getString("editor_mode"));
    }

    /**
     * Persist the plugin searchable field
     *
     * @param pluginSearchableField the plugin searchable field to persist
     */
    public Flux<Void> persistSearchableFieldByPlugin(final Flux<PluginSearchableField> pluginSearchableField) {
        Flux<? extends Statement> iter = Mutators.upsert(searchableFieldByPluginMutator, pluginSearchableField);
        return Mutators.execute(session, iter)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving searchable field by plugin %s",
                                            pluginSearchableField), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch searchable fields by plugin id.
     *
     * @param pluginId the plugin id
     */
    public Flux<PluginSearchableField> fetchSearchableFieldByPlugin(final UUID pluginId, final String version) {
        return ResultSets.query(session,
                                searchableFieldByPluginMaterializer.findSearchableFieldByPlugin(pluginId, version))
                .flatMapIterable(row -> row)
                .map(searchableFieldByPluginMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching searchable fields %s", pluginId), throwable);
                    throw Exceptions.propagate(throwable);
                });

    }

    /**
     * Fetch an LTI Provider Credential
     *
     * @param pluginId the plugin id
     * @param key the lti provider key
     * @return the matching LTIProviderCredential
     */
    @Trace(async = true)
    public Mono<LTIProviderCredential> fetchLTIProviderCredential(final UUID pluginId, final String key) {
        return ResultSets.query(session, ltiProviderCredentialByPluginMaterializer.fetch(pluginId, key))
                .flatMapIterable(row -> row)
                .map(ltiProviderCredentialByPluginMaterializer::fromRow)
                .singleOrEmpty()
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist the LTI Credentials
     *
     * @param ltiProviderCredential holds the data of lti provider configuration to persist in the datastore
     */
    @Trace(async = true)
    public Flux<Void> persistLTIPluginCredentials(final LTIProviderCredential ltiProviderCredential) {
        Flux<? extends Statement> iter = Mutators.upsert(ltiProviderCredentialByPluginMutator, ltiProviderCredential);
        return Mutators.execute(session, iter)
                .doOnError(throwable -> {
                    log.error("error persisting lti provider credentials", throwable);
                    throw Exceptions.propagate(throwable);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Delete LTI Provider Credential from datastore
     *
     * @param ltiProviderCredential is data to be deleted from the datastore
     */
    @Trace(async = true)
    public Flux<Void> deleteLTIProviderCredential(final LTIProviderCredential ltiProviderCredential) {
        Flux<? extends Statement> stmt = Mutators.delete(ltiProviderCredentialByPluginMutator, ltiProviderCredential);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error("error deleting lti provider credentials", throwable);
                    throw Exceptions.propagate(throwable);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Update plugin summary details
     *
     * @param pluginSummary the plugin summary object
     * @return flux of void
     */
    public Flux<Void> updatePluginSummary(final PluginSummary pluginSummary) {
        return Mutators.execute(session, Flux.just(pluginSummaryMutator
                                                           .updatePluginSummary(pluginSummary)))
                .doOnEach(log.reactiveErrorThrowable("error while updating plugin summary",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("pluginId", pluginSummary.getId());
                                                         }
                                                     }));
    }

    /**
     * Delete the plugin version
     *
     * @param pluginVersion the plugin version object
     * @return flux of void
     */
    @Trace(async = true)
    public Flux<Void> deletePluginVersion(final PluginVersion pluginVersion) {
        Flux<? extends Statement> stmt = Mutators.delete(versionByPluginMutator, pluginVersion);
        return Mutators.execute(session, stmt)
                .doOnError(throwable -> {
                    log.error("error deleting the plugin version", throwable);
                    throw Exceptions.propagate(throwable);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Persist the plugin filters
     *
     * @param pluginFilters the plugin filters to persist
     */
    public Flux<Void> persistPluginFilters(final List<PluginFilter> pluginFilters) {
        Flux<? extends Statement> iter = Mutators.upsert(filterByPluginVersionMutator,
                                                         Flux.fromIterable(pluginFilters));
        return Mutators.execute(session, iter)
                .doOnError(throwable -> {
                    log.error(String.format("error while saving filters for plugin %s",
                                            pluginFilters), throwable);
                    throw Exceptions.propagate(throwable);
                });
    }

    /**
     * Fetch plugin filters by id and version
     *
     * @param pluginId the plugin id
     * @param version the plugin version
     */
    @Trace(async = true)
    public Flux<PluginFilter> fetchPluginFiltersByIdVersion(final UUID pluginId, final String version) {
        return ResultSets.query(session,
                                filterByPluginVersionMaterializer.findFilterByPluginVersion(pluginId, version))
                .flatMapIterable(row -> row)
                .map(filterByPluginVersionMaterializer::fromRow)
                .doOnError(throwable -> {
                    log.error(String.format("error while fetching plugin filters %s", pluginId), throwable);
                    throw Exceptions.propagate(throwable);
                })
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

}
