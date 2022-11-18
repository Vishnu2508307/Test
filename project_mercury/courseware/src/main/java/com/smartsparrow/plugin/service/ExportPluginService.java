package com.smartsparrow.plugin.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.plugin.data.PluginGateway;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.lang.PluginManifestNotFoundFault;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.payload.ExportPluginPayload;
import com.smartsparrow.plugin.payload.PluginSummaryPayload;
import com.smartsparrow.plugin.semver.SemVerExpression;
import com.smartsparrow.plugin.semver.SemVersion;
import com.smartsparrow.plugin.wiring.PluginConfig;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

@Singleton
public class ExportPluginService {

    private static final MercuryLogger logger = MercuryLoggerFactory.getLogger(ExportPluginService.class);
    private static final ObjectMapper om = new ObjectMapper();

    private final PluginGateway pluginGateway;
    private final Provider<PluginConfig> pluginConfig;


    @Inject
    public ExportPluginService(PluginGateway pluginGateway,
                               Provider<PluginConfig> pluginConfig) {
        this.pluginGateway = pluginGateway;
        this.pluginConfig = pluginConfig;

    }



    /**
     * Fetches plugin summary and plugin repository path (if plugin was published).
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
    @Trace(async = true)
    public Mono<ExportPluginPayload> findExportPluginPayload(final UUID pluginId, final String versionExpr) {
        checkArgument(pluginId != null, "pluginId is required");
        checkArgument(versionExpr != null, "versionExpr is required");

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
        Mono<PluginSummaryPayload> summaryPayloadMono = getPluginSummaryPayload(pluginId)
                .doOnEach(ReactiveTransaction.linkOnNext());


        Mono<String> repositoryPath = version
                .flatMap(v -> pluginGateway.fetchPluginManifestByIdVersion(pluginId, v))
                .switchIfEmpty(Mono.error(new PluginManifestNotFoundFault(pluginId)))
                .map(pluginManifest -> pluginManifest.buildPluginRepositoryUrl(pluginConfig.get().getRepositoryPublicUrl()));

        return Mono.zip(summaryPayloadMono, repositoryPath)
                .map(tuple2 -> new ExportPluginPayload()
                        .setPluginSummaryPayload(tuple2.getT1())
                        .setPluginRepositoryPath(tuple2.getT2()));
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
    public Mono<String> findLatestVersion(UUID pluginId, String versionExpr) {
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
     * Fetch a plugin summary by id
     *
     * @param pluginId the id of the plugin to fetch
     * @return {@link Mono} of {@link PluginSummary}
     */
    @Trace(async = true)
    private Mono<PluginSummary> fetchById(UUID pluginId) {
        return pluginGateway.fetchPluginSummaryById(pluginId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }


    /**
     * Builds a PluginSummaryPayload for plugin
     *
     * @param pluginId the plugin id to build a payload object for
     * @return Mono with PluginSummaryPayload, always not empty
     * @throws PluginNotFoundFault when summary for plugin doesn't exist
     */
    @Trace(async = true)
    private Mono<PluginSummaryPayload> getPluginSummaryPayload(UUID pluginId) {
        checkArgument(pluginId != null, "pluginId can not be null");

        return fetchById(pluginId)
                .flatMap(pluginSummary -> Mono.just(PluginSummaryPayload.from(pluginSummary)))
                .single()
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnError(NoSuchElementException.class, t -> {
                    String error = String.format("summary not found for plugin_id='%s'", pluginId);
                    throw new PluginNotFoundFault(error);
                });
    }
}
