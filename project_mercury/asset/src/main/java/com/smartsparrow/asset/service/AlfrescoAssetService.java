package com.smartsparrow.asset.service;

import static com.smartsparrow.data.Headers.ALFRESCO_AZURE_AUTHORIZATION_HEADER;
import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_CHILDREN;
import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_CONTENT;
import static com.smartsparrow.dataevent.RouteUri.ALFRESCO_NODE_INFO;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.asset.data.AlfrescoAssetData;
import com.smartsparrow.asset.data.AlfrescoImageNode;
import com.smartsparrow.asset.data.AlfrescoNode;
import com.smartsparrow.asset.data.AllowedAsset;
import com.smartsparrow.asset.data.AssetBuilder;
import com.smartsparrow.asset.data.AssetGateway;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetMetadata;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.data.AssetTemplate;
import com.smartsparrow.asset.data.AssetVisibility;
import com.smartsparrow.asset.data.AudioSourceName;
import com.smartsparrow.asset.data.ImageSource;
import com.smartsparrow.asset.data.ImageSourceName;
import com.smartsparrow.asset.data.VideoSourceName;
import com.smartsparrow.asset.eventmessage.AlfrescoNodeChildrenEventMessage;
import com.smartsparrow.asset.eventmessage.AlfrescoNodeContentEventMessage;
import com.smartsparrow.asset.eventmessage.AlfrescoNodeInfoEventMessage;
import com.smartsparrow.asset.lang.AssetURNParseException;
import com.smartsparrow.asset.lang.AssetUploadException;
import com.smartsparrow.asset.lang.UnsupportedAssetException;
import com.smartsparrow.asset.data.AssetIdByUrn;
import com.smartsparrow.config.AssetConfig;
import com.smartsparrow.exception.ExternalServiceFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.exception.UnprocessableEntityException;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.util.ClockProvider;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.json.JSONException;
import org.json.JSONObject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class AlfrescoAssetService implements AssetService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AlfrescoAssetService.class);

    private final AssetGateway assetGateway;
    private final BronteAssetService bronteAssetService;
    private final AssetUploadService assetUploadService;
    private final ClockProvider clockProvider;
    private final AssetBuilder assetBuilder;
    private final CamelReactiveStreamsService camelReactiveStreamsService;
    private final AssetConfig assetConfig;

    @Inject
    public AlfrescoAssetService(AssetGateway assetGateway,
                                BronteAssetService bronteAssetService,
                                AssetUploadService assetUploadService,
                                ClockProvider clockProvider,
                                AssetBuilder assetBuilder,
                                CamelReactiveStreamsService camelReactiveStreamsService,
                                AssetConfig assetConfig) {
        this.assetGateway = assetGateway;
        this.bronteAssetService = bronteAssetService;
        this.assetUploadService = assetUploadService;
        this.clockProvider = clockProvider;
        this.assetBuilder = assetBuilder;
        this.camelReactiveStreamsService = camelReactiveStreamsService;
        this.assetConfig = assetConfig;
    }

    /**
     * Send an event to the {@link com.smartsparrow.dataevent.RouteUri#ALFRESCO_NODE_CHILDREN} route which instructs camel
     * to perform an http request to the Alfresco service to fetch the children of an Alfresco node.
     *
     * @param nodeId the nodeId to find the Alfresco node children for
     * @param myCloudToken a valid myCloud token
     * @return a mono with the Alfresco node children when found
     * @throws UnauthorizedFault when the the request failed for any reason
     */
    public Mono<AlfrescoNodeChildren> getNodeChildren(@Nonnull final String nodeId,
                                                      @Nonnull final String myCloudToken) {
        return Mono.just(new AlfrescoNodeChildrenEventMessage(nodeId, myCloudToken)) //
                .doOnEach(log.reactiveInfo("handling alfresco node children get"))
                .map(event -> camelReactiveStreamsService.toStream(ALFRESCO_NODE_CHILDREN, event, AlfrescoNodeChildrenEventMessage.class)) //
                .flatMap(Mono::from)
                .map(alfrescoNodeChildrenEventMessage -> {
                    if (alfrescoNodeChildrenEventMessage.hasError()) {
                        throw new ExternalServiceFault(alfrescoNodeChildrenEventMessage.getErrorMessage());
                    }

                    return alfrescoNodeChildrenEventMessage.getAlfrescoNodeChildren();
                });
    }

    /**
     * Send an event to the {@link com.smartsparrow.dataevent.RouteUri#ALFRESCO_NODE_INFO} route which instructs camel
     * to perform an http request to the Alfresco service to fetch the info of an Alfresco node.
     *
     * @param nodeId the nodeId to find the Alfresco node info for
     * @param myCloudToken a valid myCloud token
     * @return a mono with the Alfresco node info when found
     * @throws UnauthorizedFault when the the request failed for any reason
     */
    public Mono<AlfrescoNodeInfo> getNodeInfo(@Nonnull final String nodeId,
                                              @Nonnull final String myCloudToken) {
        return Mono.just(new AlfrescoNodeInfoEventMessage(nodeId, myCloudToken)) //
                .doOnEach(log.reactiveInfo("handling alfresco node info get"))
                .map(event -> camelReactiveStreamsService.toStream(ALFRESCO_NODE_INFO, event, AlfrescoNodeInfoEventMessage.class)) //
                .flatMap(Mono::from)
                .map(alfrescoNodeGetEventMessage -> {
                    final AlfrescoNodeInfo nodeInfo = alfrescoNodeGetEventMessage.getAlfrescoNodeInfo();
                    if (nodeInfo != null) {
                        log.debug(nodeInfo.toString());
                        return nodeInfo;
                    }
                    throw new UnauthorizedFault("Invalid token supplied");
                });
    }

    public Mono<AlfrescoNodeInfo> debugGetNodeInfo(@Nonnull final String nodeId,
                                                   @Nonnull final String myCloudToken) {
        return Mono.defer(() -> {
            AlfrescoNodeInfo nodeInfo = new AlfrescoNodeInfo().setId(nodeId);

            HttpURLConnection conn = null;
            try {
                URL url = new URL(assetConfig.getAlfrescoUrl() + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" + nodeId);
                conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(false);

                // set request headers
                conn.setRequestMethod("GET");
                setCommonHeaders(conn);
                conn.setRequestProperty(ALFRESCO_AZURE_AUTHORIZATION_HEADER, myCloudToken);

                conn.connect();

                // check response code
                int responseCode = conn.getResponseCode();
                if (responseCode < 200 || responseCode > 299) {
                    String data = readResponse(conn);
                    log.error(String.format("Unexpected response code %s, data: %s", responseCode, data));
                    return Mono.error(new ExternalServiceFault("Unexpected response code " + responseCode));
                }

                // parse identity profile
                String data = readResponse(conn);
                JSONObject json = new JSONObject(data);
                JSONObject entry = json.getJSONObject("entry");
                JSONObject content = entry.getJSONObject("content");
                JSONObject props = entry.getJSONObject("properties");

                // convert modifiedAt epoch string to long
                String modifiedAt = entry.getString("modifiedAt");
                modifiedAt = modifiedAt.substring(0, modifiedAt.indexOf('.')); // '2021-02-03T02:05:47.372+0000' -> '2021-02-03T02:05:47'
                long modifiedAtEpoch = LocalDateTime.parse(modifiedAt).toEpochSecond(ZoneOffset.UTC);

                nodeInfo.setName(entry.getString("name"))
                        .setModifiedAt(modifiedAtEpoch)
                        .setVersion(props.getString("cm:versionLabel"))
                        .setMimeType(content.getString("mimeType"))
                        .setWidth(props.getDouble("exif:pixelXDimension"))
                        .setHeight(props.getDouble("exif:pixelYDimension"))
                        .setAltText((props.has("cplg:altText")) ? props.getString("cplg:altText") : "")
                        .setLongDesc((props.has("cplg:longDescription")) ? props.getString("cplg:longDescription") : "");
            } catch (MalformedURLException ex) {
                log.error(ex.getMessage());
                return Mono.error(ex);
            } catch (IOException ex) {
                log.error(ex.getMessage());
                return Mono.error(ex);
            } catch (JSONException ex) {
                log.error(ex.getMessage());
                return Mono.error(new UnprocessableEntityException("Unexpected object received from myCloud service"));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return Mono.just(nodeInfo);
        });
    }

    /**
     * Send an event to the {@link com.smartsparrow.dataevent.RouteUri#ALFRESCO_NODE_CONTENT} route which instructs camel
     * to perform an http request to the Alfresco service to fetch the content of an Alfresco node.
     *
     * @param nodeId the nodeId to find the Alfresco node content for
     * @param myCloudToken a valid myCloud token
     * @return a mono with the Alfresco node info when found
     * @throws UnauthorizedFault when the the request failed for any reason
     */
    public Mono<Path> getNodeContent(@Nonnull final String nodeId,
                                     @Nonnull final String myCloudToken) {
        return getNodeContentStream(nodeId, myCloudToken)
                .map(is -> {
                    try {
                        Path tmpFilePath = Files.createTempFile("mercury-asset-", null);
                        Files.copy(is, tmpFilePath, StandardCopyOption.REPLACE_EXISTING);
                        log.debug(String.format("Created temporary file %s", tmpFilePath));

                        return tmpFilePath;
                    } catch (IOException e) {
                        log.error("Error while saving asset to the temporary file", e);
                        throw new AssetUploadException("error while saving asset");
                    }
                });
    }

    /**
     * Send an event to the {@link com.smartsparrow.dataevent.RouteUri#ALFRESCO_NODE_CONTENT} route which instructs camel
     * to perform an http request to the Alfresco service to fetch the content stream of an Alfresco node.
     *
     * @param nodeId the nodeId to find the Alfresco node content for
     * @param myCloudToken a valid myCloud token
     * @return a mono with the Alfresco node info when found
     * @throws UnauthorizedFault when the the request failed for any reason
     */
    public Mono<InputStream> getNodeContentStream(@Nonnull final String nodeId,
                                                  @Nonnull final String myCloudToken) {
        return Mono.just(new AlfrescoNodeContentEventMessage(nodeId, myCloudToken)) //
                .doOnEach(log.reactiveInfo("handling alfresco node get"))
                .map(event -> camelReactiveStreamsService.toStream(ALFRESCO_NODE_CONTENT, event, AlfrescoNodeContentEventMessage.class)) //
                .flatMap(Mono::from)
                .map(alfrescoNodeContentEventMessage -> {
                    final InputStream is = alfrescoNodeContentEventMessage.getContentStream();
                    if (is != null) {
                        return is;
                    }
                    throw new UnauthorizedFault("Invalid token supplied");
                });
    }

    public Mono<InputStream> debugGetNodeContentStream(@Nonnull final String nodeId,
                                                       @Nonnull final String myCloudToken) {
        return Mono.defer(() -> {
            InputStream is = null;

            HttpURLConnection conn = null;
            try {
                URL url = new URL(assetConfig.getAlfrescoUrl() + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/" + nodeId + "/content");
                conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(false);

                // set request headers
                conn.setRequestMethod("GET");
                setCommonHeaders(conn);
                conn.setRequestProperty(ALFRESCO_AZURE_AUTHORIZATION_HEADER, myCloudToken);

                conn.connect();

                // check response code
                int responseCode = conn.getResponseCode();
                if (responseCode < 200 || responseCode > 299) {
                    String data = readResponse(conn);
                    log.error(String.format("Unexpected response code %s, data: %s", responseCode, data));
                    return Mono.error(new ExternalServiceFault("Unexpected response code " + responseCode));
                }

                is = conn.getInputStream();
            } catch (MalformedURLException ex) {
                log.error(ex.getMessage());
                return Mono.error(ex);
            } catch (IOException ex) {
                log.error(ex.getMessage());
                return Mono.error(ex);
            } catch (JSONException ex) {
                log.error(ex.getMessage());
                return Mono.error(new UnprocessableEntityException("Unexpected object received from Alfresco service"));
            } finally {
                if (is == null) {
                    if (conn != null) {
                        conn.disconnect();
                    }

                    return Mono.error(new ExternalServiceFault("Unexpected error occurred during service call"));
                }
            }

            return Mono.just(is);
        });
    }

    /**
     * Save an Alfresco asset. An asset summary object is created then uploaded to the configured s3 bucket. If
     * successful the summary entry is saved to the database and returned.
     *
     * @param alfrescoNodeId  the alfresco node id
     * @param creator         the account creating the asset
     * @param myCloudToken    a valid myCloud token
     * @return a mono with the payload of the created asset
     * @throws com.smartsparrow.exception.IllegalArgumentFault when the supplied provider is not supported
     */
    public Mono<AssetPayload> save(@Nonnull final String alfrescoNodeId,
                                   @Nonnull final Account creator,
                                   @Nonnull final String myCloudToken) {
        Mono<AlfrescoNodeInfo> alfrescoInfoMono = getNodeInfo(alfrescoNodeId, myCloudToken);
        Mono<InputStream> alfrescoContentStreamMono = getNodeContentStream(alfrescoNodeId, myCloudToken);

        return Mono.zip(alfrescoInfoMono, alfrescoContentStreamMono)
                .flatMap(tuple2 -> {
                    AlfrescoNodeInfo alfrescoNodeInfo = tuple2.getT1();
                    InputStream contentStream = tuple2.getT2();

                    UUID assetId = UUIDs.timeBased();
                    String filename = alfrescoNodeInfo.getName();

                    // set asset metadata
                    Map<String, String> metadata = new HashMap<>();
                    metadata.put("altText", alfrescoNodeInfo.getAltText());
                    metadata.put("longDesc", alfrescoNodeInfo.getLongDesc());
                    metadata.put("alfrescoPath", getAlfrescoPath(alfrescoNodeInfo.getPathName()));
                    metadata.put("workURN", alfrescoNodeInfo.getWorkURN());

                    // set Alfresco asset data
                    final AlfrescoAssetData alfrescoAssetData = new AlfrescoAssetData()
                            .setAssetId(assetId)
                            .setAlfrescoId(UUID.fromString(alfrescoNodeInfo.getId()))
                            .setName(alfrescoNodeInfo.getName())
                            .setVersion(alfrescoNodeInfo.getVersion())
                            .setLastModifiedDate(alfrescoNodeInfo.getModifiedAt())
                            .setLastSyncDate(clockProvider.get().instant().getEpochSecond());

                    final AssetTemplate assetTemplate = new AssetTemplate(filename)
                            .setInputStream(contentStream)
                            .setMetadata(metadata)
                            .setOwnerId(creator.getId())
                            .setProvider(AssetProvider.ALFRESCO)
                            .setSubscriptionId(creator.getSubscriptionId())
                            .setVisibility(AssetVisibility.GLOBAL);

                    return assetUploadService.create(assetId, assetTemplate)
                            .then(assetGateway.persist(alfrescoAssetData)
                                    .doOnEach(log.reactiveInfoSignal("Alfresco asset service: asset alfresco data saved"))
                                    .singleOrEmpty()
                            )
                            .thenReturn(assetId)
                            .flatMap(id -> getAssetPayload(id));
                });
    }

    public Mono<AssetPayload> debugSave(@Nonnull final String alfrescoNodeId,
                                        @Nonnull final Account creator,
                                        @Nonnull final String myCloudToken) {
        Mono<AlfrescoNodeInfo> alfrescoInfoMono = debugGetNodeInfo(alfrescoNodeId, myCloudToken);
        Mono<InputStream> alfrescoContentStreamMono = debugGetNodeContentStream(alfrescoNodeId, myCloudToken);

        return Mono.zip(alfrescoInfoMono, alfrescoContentStreamMono)
                .flatMap(tuple2 -> {
                    AlfrescoNodeInfo alfrescoNodeInfo = tuple2.getT1();
                    InputStream contentStream = tuple2.getT2();

                    UUID assetId = UUIDs.timeBased();
                    String filename = alfrescoNodeInfo.getName();

                    // set asset metadata
                    Map<String, String> metadata = new HashMap<>();
                    metadata.put("altText", alfrescoNodeInfo.getAltText());
                    metadata.put("longDesc", alfrescoNodeInfo.getLongDesc());

                    // set Alfresco asset data
                    final AlfrescoAssetData alfrescoAssetData = new AlfrescoAssetData()
                            .setAssetId(assetId)
                            .setAlfrescoId(UUID.fromString(alfrescoNodeInfo.getId()))
                            .setName(alfrescoNodeInfo.getName())
                            .setVersion(alfrescoNodeInfo.getVersion())
                            .setLastModifiedDate(alfrescoNodeInfo.getModifiedAt())
                            .setLastSyncDate(clockProvider.get().instant().getEpochSecond());

                    final AssetTemplate assetTemplate = new AssetTemplate(filename)
                            .setInputStream(contentStream)
                            .setMetadata(metadata)
                            .setOwnerId(creator.getId())
                            .setProvider(AssetProvider.ALFRESCO)
                            .setSubscriptionId(creator.getSubscriptionId())
                            .setVisibility(AssetVisibility.GLOBAL);

                    return assetUploadService.create(assetId, assetTemplate)
                            .then(assetGateway.persist(alfrescoAssetData)
                                    .doOnEach(log.reactiveInfoSignal("Alfresco asset service: asset alfresco data saved"))
                                    .singleOrEmpty()
                            )
                            .thenReturn(assetId)
                            .flatMap(id -> getAssetPayload(id));
                });
    }

    /**
     * Update the asset data after syncing with Alfresco and saving Alfresco data. The asset data will be updated in
     * asset summary and image source tables. Alfresco data will be saved in alfresco data table
     *
     * @param assetId           the asset id
     * @param ownerId           the asset owner id
     * @param alfrescoImageNode the Alfresco image data to be saved
     * @return a mono of void
     * @throws NotFoundFault if asset id cannot be found in asset summary and image source
     */
    public Mono<Void> saveAlfrescoImageData(UUID assetId, UUID ownerId, AlfrescoImageNode alfrescoImageNode) {
        affirmArgument(assetId != null, "assetId is required");
        affirmArgument(ownerId != null, "ownerId is required");
        affirmArgument(alfrescoImageNode != null, "alfrescoImageNode is required");

        // get asset summary by asset id, then update asset provider and owner id
        // return NotFoundFault if it cannot find asset summary
        Mono<AssetSummary> assetSummaryMono = assetGateway.fetchAssetById(assetId)
                .switchIfEmpty(Mono.error(new NotFoundFault(String.format("cannot find asset summary by asset id: %s", assetId))))
                .map(summary ->
                        summary.setProvider(AssetProvider.ALFRESCO) // after syncing with Alfresco, change provider to Alfresco
                                .setOwnerId(ownerId)                // change owner id to who trigger Alfresco asset sync process
                );

        // get original image source by asset id , then update url, width, and height
        // return NotFoundFault if it cannot find image source
        Mono<ImageSource> imageSourceMono = assetGateway.fetchImageSources(assetId)
                .filter(image -> image.getName().equals(ImageSourceName.ORIGINAL)).singleOrEmpty() // Only sync original image to Alfresco
                .switchIfEmpty(Mono.error(new NotFoundFault(String.format("cannot find image source by asset id: %s", assetId))))
                .map(imageSource ->
                        imageSource.setUrl(alfrescoImageNode.getSource())
                                .setWidth(alfrescoImageNode.getWidth())
                                .setHeight(alfrescoImageNode.getHeight()));

        return Mono.zip(assetSummaryMono, imageSourceMono)
                .flatMap(tuple2 -> {

                    final AssetSummary assetSummary = tuple2.getT1();
                    final ImageSource imageSource = tuple2.getT2();

                    // set Alfresco image metadata
                    Map<String, String> metadata = new HashMap<>();
                    metadata.put("altText", alfrescoImageNode.getAltText());
                    metadata.put("longDesc", alfrescoImageNode.getLongDescription());
                    metadata.put("alfrescoPath", alfrescoImageNode.getPath());
                    metadata.put("workURN", alfrescoImageNode.getWorkURN());

                    HashMap<String, Object> metadataLog = new HashMap<String, Object>();
                    metadataLog.put("altText", alfrescoImageNode.getAltText());
                    metadataLog.put("longDesc", alfrescoImageNode.getLongDescription());
                    metadataLog.put("alfrescoPath", alfrescoImageNode.getPath());
                    metadataLog.put("workURN", alfrescoImageNode.getWorkURN());
                    metadataLog.put("assetId", assetSummary.getId());
                    metadataLog.put("imageSourceUrl", imageSource.getUrl());
                    log.jsonInfo("Alfresco Service asset save metadata ",metadataLog);

                    // set Alfresco asset data
                    final AlfrescoAssetData alfrescoAssetData = new AlfrescoAssetData()
                            .setAssetId(assetId)
                            .setAlfrescoId(alfrescoImageNode.getAlfrescoId())
                            .setName(alfrescoImageNode.getName())
                            .setVersion(alfrescoImageNode.getVersion())
                            .setLastModifiedDate(alfrescoImageNode.getLastModifiedDate())
                            .setLastSyncDate(clockProvider.get().instant().getEpochSecond());

                    return assetGateway.persist(assetSummary)
                            .then(assetGateway.persist(imageSource).singleOrEmpty())
                            .then(assetGateway.persist(alfrescoAssetData).singleOrEmpty())
                            .then(bronteAssetService.saveMetadata(assetId, metadata));
                })
                .doOnEach(log.reactiveErrorThrowable("error saving alfresco image data"));
    }

    /**
     * Find an alfresco asset data object by assetId
     *
     * @param assetId the asset id to find the alfresco data for
     * @return a mono of alfresco asset data or an empty mono when not found
     */
    public Mono<AlfrescoAssetData> getAlfrescoAssetData(final UUID assetId) {
        affirmArgument(assetId != null, "assetId is required");
        return assetGateway.fetchAlfrescoAssetById(assetId);
    }

    /**
     * Build an alfresco image node for an asset summary. The method only supports asset summaries with
     * {@link AssetProvider#ALFRESCO}
     *
     * @param assetSummary the asset summary to build the alfresco node for
     * @return a mono of alfresco node
     */
    public Mono<AlfrescoNode> getAlfrescoImageNode(final AssetSummary assetSummary) {
        affirmArgument(assetSummary != null, "assetSummary is required");
        affirmArgument(assetSummary.getProvider().equals(AssetProvider.ALFRESCO),
                "assetSummary with ALFRESCO provider required");

        final UUID assetId = assetSummary.getId();
        // find the alfresco asset data
        final Mono<AlfrescoAssetData> alfrescoAssetDataMono = getAlfrescoAssetData(assetId);
        // find the original image source
        final Mono<ImageSource> originalImageSourceMono = bronteAssetService.getImageSource(assetId)
                .filter(imageSource -> imageSource.getName().equals(ImageSourceName.ORIGINAL))
                .singleOrEmpty()
                .switchIfEmpty(Mono.error(new NotFoundFault(String.format("cannot find image source by asset id: %s",
                        assetId))));
        // find the asset metadata
        Mono<Map<String, String>> assetMetadataMono = bronteAssetService.getAssetMetadata(assetId)
                .collectMap(AssetMetadata::getKey, AssetMetadata::getValue);

        // zip all the sources together
        return Mono.zip(originalImageSourceMono, alfrescoAssetDataMono, assetMetadataMono).map(tuple3 -> {

            final ImageSource imageSource = tuple3.getT1();
            final AlfrescoAssetData alfrescoAssetData = tuple3.getT2();
            final Map<String, String> assetMetadata = tuple3.getT3();

            // build the alfresco image node
            return new AlfrescoImageNode()
                    .setMimeType(assetSummary.getMediaType().getLabel())
                    .setWidth(imageSource.getWidth())
                    .setHeight(imageSource.getHeight())
                    .setSource(imageSource.getUrl())
                    .setAltText(assetMetadata.get("altText"))
                    .setLongDescription(assetMetadata.get("longDesc"))
                    .setAlfrescoId(alfrescoAssetData.getAlfrescoId())
                    .setName(alfrescoAssetData.getName())
                    .setVersion(alfrescoAssetData.getVersion())
                    .setLastModifiedDate(alfrescoAssetData.getLastModifiedDate());
        });
    }

    /**
     * Fetch asset payload by URN
     *
     * @param urn the uniform resource name which is uniquely identifying the asset
     * @return mono with asset info, or empty mono if no asset for the urn
     * @throws AssetURNParseException if urn can not be parsed
     */
    @Override
    @SuppressWarnings("Duplicates")
    public Mono<AssetPayload> getAssetPayload(final String urn) {
        // find the latest asset id associated to this urn
        return assetGateway.findAssetId(urn)
                .map(AssetIdByUrn::getAssetId)
                .flatMap(this::getAssetPayload)
                // log a line if anything goes wrong, thank you
                .doOnEach(log.reactiveErrorThrowable("failed to get asset payload", throwable -> new HashMap<String, Object>(){
                    {put("assetUrn", urn);}
                }));
    }

    @Override
    public Mono<AssetPayload> getAssetPayload(final UUID assetId) {
        Mono<AssetSummary> summary = assetGateway.fetchAssetById(assetId);
        Mono<AlfrescoAssetData> alfrescoAssetData = getAlfrescoAssetData(assetId);
        Mono<Map<String, String>> metadata = assetGateway.fetchMetadata(assetId)
                .collectMap(AssetMetadata::getKey, AssetMetadata::getValue);

        Mono<Map<String, Object>> sources = summary.flatMap(asset -> {
            affirmArgument(asset.getProvider().equals(AssetProvider.ALFRESCO),
                    String.format("asset provider %s not supported in AlfrescoAssetService", asset.getProvider()));

            switch (asset.getMediaType()) {
                case IMAGE:
                    return bronteAssetService.getImageSourcePayload(assetId);
                case VIDEO:
                    return bronteAssetService.getVideoSourcePayload(assetId);
                case DOCUMENT:
                    return bronteAssetService.getDocumentSourcePayload(assetId);
                case AUDIO:
                    return bronteAssetService.getAudioSourcePayload(assetId);
                default:
                    return Mono.error(new UnsupportedOperationException("Asset fetching is not supported for " + asset.getMediaType()));
            }
        });

        return Mono.zip(summary, metadata, sources, alfrescoAssetData).map(tuple4 -> new AssetPayload()
                .setUrn(tuple4.getT1().getUrn())
                .setAsset(assetBuilder.setAssetSummary(tuple4.getT1())
                        .setAlfrescoAssetData(tuple4.getT4())
                        .build(tuple4.getT1().getProvider())) //
                .putAllMetadata(tuple4.getT2())
                .putAllSources(tuple4.getT3()));
    }

    /**
     * Get an asset summary by id
     *
     * @param id the asset id
     * @return mono with AssetSummary, empty mono if asset is not found
     */
    public Mono<AssetSummary> getAssetById(final UUID id) {
        return assetGateway.fetchAssetById(id);
    }

    /**
     * Get all image sources for asset
     *
     * @param assetId the asset id
     * @return flux with ImageSource, empty flux if no image sources for the asset
     */
    public Flux<ImageSource> getImageSources(UUID assetId) {
        return assetGateway.fetchImageSources(assetId);
    }

    /**
     *  Get Alfresco path
     *
     * @param pathName the path name
     * @return Alfresco path
     */
    public String getAlfrescoPath(final String pathName) {
        // return empty string if pathName is null or empty
        if(pathName == null || pathName.isEmpty()){
            return "";
        }

        String[] pathNameArr = pathName.split("/", 4); // remove company home and sites from the path name

        return pathNameArr[pathNameArr.length - 1];
    }

    /**
     * Build an asset url. The built url is a relative path to the asset. The url is built with the following format:
     * <br>
     * `22147060-0243-11e9-b3db-a1a055ed7abf/original/ad74930865f01d2ba8d27122e07ed953.png`
     *
     * @param assetSummary  the asset summary to build the url for
     * @param hash          the hash of the file
     * @param fileExtension the file extension
     * @return the asset relative url
     */
    private String getAssetUrl(final AssetSummary assetSummary, final String hash, final String fileExtension) {
        return String.format("%s/%s/%s%s",
                assetSummary.getId(), getName(assetSummary.getMediaType()), hash, fileExtension);
    }

    /**
     * Get the name source for a media type. Normally this is represented by an {@link Enum} describing the file size.
     * In most cases the size type will be represented with ORIGINAL as value, meaning the file is being uploaded with
     * its original size. This string value is used for building the asset url
     *
     * @param mediaType the type of asset
     * @return a string representation of the media type source name
     * @throws UnsupportedAssetException when the media type is not supported
     */
    private String getName(final AssetMediaType mediaType) {
        switch (mediaType) {
            case IMAGE:
                return ImageSourceName.ORIGINAL.name().toLowerCase();
            case AUDIO:
                return AudioSourceName.ORIGINAL.name().toLowerCase();
            case VIDEO:
                return VideoSourceName.ORIGINAL.name().toLowerCase();
            case DOCUMENT:
                // FIXME this value is hardcoded, is it ok or is it not? posterity will judge
                return "original";
            default:
                throw new UnsupportedAssetException("invalid media type");
        }
    }

    /**
     * Get the asset media type based on the file extension
     *
     * @param fileExtension the file extension to derive the media type from
     * @return the asset media type
     * @throws UnsupportedAssetException when the file extension is not supported
     */
    private AssetMediaType getAssetMediaType(final String fileExtension) {
        final String extension = fileExtension.replace(".", "");

        if (AllowedAsset.SUBTITLE.allows(extension)) {
            return AssetMediaType.DOCUMENT;
        }

        if (AllowedAsset.AUDIO.allows(extension)) {
            return AssetMediaType.AUDIO;
        }

        if (AllowedAsset.IMAGES.allows(extension)) {
            return AssetMediaType.IMAGE;
        }

        throw new UnsupportedAssetException(String.format("File extension %s not supported", fileExtension));
    }

    private void setCommonHeaders(HttpURLConnection conn) {
        conn.setRequestProperty("User-Agent", "HttpURLConnection/11.0 Java");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Connection", "close");
    }

    private String readResponse(final HttpURLConnection conn) throws IOException {
        InputStream is = null;
        try {
            is = conn.getInputStream();
        } catch (IOException ex) {
            is = conn.getErrorStream();
            if (is == null) {
                log.error("No error stream available");
                throw ex;
            }
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            return response.toString();
        }
    }
}
