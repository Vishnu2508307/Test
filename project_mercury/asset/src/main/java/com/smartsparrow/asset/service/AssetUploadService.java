package com.smartsparrow.asset.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.asset.route.AssetRoute.CONTENT_TYPE;
import static com.smartsparrow.asset.route.AssetRoute.FILE_NAME;
import static com.smartsparrow.asset.route.AssetRoute.UPLOAD_ASSET_ROUTE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.apache.tika.Tika;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.asset.data.AllowedAsset;
import com.smartsparrow.asset.data.AssetConstant;
import com.smartsparrow.asset.data.AssetGateway;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetSummary;
import com.smartsparrow.asset.data.AssetTemplate;
import com.smartsparrow.asset.data.AssetUrn;
import com.smartsparrow.asset.data.AudioSourceName;
import com.smartsparrow.asset.data.IconSourceName;
import com.smartsparrow.asset.data.ImageSourceName;
import com.smartsparrow.asset.data.VideoSourceName;
import com.smartsparrow.asset.lang.AssetUploadException;
import com.smartsparrow.asset.lang.AssetUploadValidationException;
import com.smartsparrow.asset.lang.UnsupportedAssetException;
import com.smartsparrow.util.Hashing;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

@Singleton
public class AssetUploadService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AssetUploadService.class);

    private final ProducerTemplate producerTemplate;
    private final AssetGateway assetGateway;
    private final BronteAssetService bronteAssetService;
    private final BronteAssetTypeHandler bronteAssetTypeHandler;

    @Inject
    public AssetUploadService(final ProducerTemplate producerTemplate,
                              final AssetGateway assetGateway,
                              final BronteAssetService bronteAssetService,
                              final BronteAssetTypeHandler bronteAssetTypeHandler) {
        this.producerTemplate = producerTemplate;
        this.assetGateway = assetGateway;
        this.bronteAssetService = bronteAssetService;
        this.bronteAssetTypeHandler = bronteAssetTypeHandler;
    }

    /**
     * Save a new asset. A new asset summary object is created then uploaded to the configured s3 bucket. If the upload
     * succeeds the summary entry is saved to the database and returned.
     * FIXME currently the method does not check for an asset being uploaded twice
     * TODO what is the file size limit for an upload? required validation
     *
     * @param assetTemplate the asset template to be uploaded
     * @return a mono including the asset payload
     * @throws UnsupportedAssetException      when the uploaded file has an unsupported mimeType (Requires a stronger validation)
     * @throws AssetUploadException           when a failure occurs while uploading an asset
     * @throws AssetUploadValidationException when trying to upload an asset from an invalid asset template
     */
    public Mono<AssetPayload> save(final AssetTemplate assetTemplate) {
        return save(UUIDs.timeBased(), assetTemplate);
    }

    /**
     * Save an asset. An asset summary object is created then uploaded to the configured s3 bucket. If the upload
     * succeeds the summary entry is saved to the database and returned.
     * Should the assetId argument belong to an existing asset, all the data related to that asset will be overwritten
     * with the new asset data.
     * TODO what is the file size limit for an upload? required validation
     *
     * @param assetId       the id of the asset to create
     * @param assetTemplate the asset template to be uploaded
     * @return a mono including the asset payload
     * @throws UnsupportedAssetException      when the uploaded file has an unsupported mimeType (Requires a stronger validation)
     * @throws AssetUploadException           when a failure occurs while uploading an asset
     * @throws AssetUploadValidationException when trying to upload an asset from an invalid asset template
     */
    public Mono<AssetPayload> save(final UUID assetId, final AssetTemplate assetTemplate) {
        return Mono.just(1)
                .map(ignored -> {
                    return create(assetId, assetTemplate);
                })
                .flatMap(one -> one.map(assetSummary -> {
                    return bronteAssetService.getAssetPayload(assetSummary.getId());
                }))
                .flatMap(one -> one);
    }

    public Mono<AssetSummary> create(final UUID assetId, final AssetTemplate assetTemplate) {
        try {
            checkArgument(assetTemplate.getInputStream() != null, "inputStream is required");
            checkArgument(assetTemplate.getFileExtension() != null, "fileExtension is required");
            checkArgument(assetTemplate.getOwnerId() != null, "ownerId is required");
            checkArgument(assetTemplate.getSubscriptionId() != null, "subscriptionId is required");
            checkArgument(assetTemplate.getProvider() != null, "assetProvider is required");
            checkArgument(assetTemplate.getVisibility() != null, "assetVisibility is required");
        } catch (IllegalArgumentException e) {
            throw new AssetUploadValidationException(e.getMessage());
        }

        java.nio.file.Path tmpFilePath = null;
        try {
            // save asset to temp location
            tmpFilePath = Files.createTempFile("mercury-asset-", null);
            Files.copy(assetTemplate.getInputStream(), tmpFilePath, StandardCopyOption.REPLACE_EXISTING);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Created temporary file %s for asset %s ", tmpFilePath, assetTemplate.getOriginalFileName()));
            }
            String hash = Hashing.file(tmpFilePath.toFile());
            String mimeType = new Tika().detect(assetTemplate.getInputStream(), assetTemplate.getOriginalFileName());

            // get the asset media type from the file extension
            AssetMediaType assetMediaType = getAssetMediaType(assetTemplate.getFileExtension(),
                                                              assetTemplate.getMetadata());

            final String templateUrn = assetTemplate.getUrn();
            String assetUrn = templateUrn != null ? templateUrn : new AssetUrn(assetId, assetTemplate.getProvider()).toString();
            AssetSummary assetSummary = new AssetSummary()
                    // set the urn from the template or create a new urn when the urn template is null
                    .setUrn(assetUrn)
                    .setHash(hash)
                    .setId(assetId)
                    .setOwnerId(assetTemplate.getOwnerId())
                    .setVisibility(assetTemplate.getVisibility())
                    .setProvider(assetTemplate.getProvider())
                    .setSubscriptionId(assetTemplate.getSubscriptionId())
                    .setMediaType(assetMediaType);

            final File file = tmpFilePath.toFile();
            final String assetUrl = getAssetUrl(assetSummary, hash, assetTemplate.getFileExtension());

            // upload asset to S3
            Exchange response = producerTemplate
                    .request(UPLOAD_ASSET_ROUTE, exchange -> {
                        Message m = exchange.getIn();
                        m.setBody(file);
                        m.setHeader(FILE_NAME, assetUrl);
                        m.setHeader(CONTENT_TYPE, mimeType);
                    });
            if (response.isFailed()) {
                Exception exception = (Exception) response.getProperty(Exchange.EXCEPTION_CAUGHT);
                log.jsonError("Asset upload failed ", new HashMap<String, Object>() {
                    {
                        put("assetId", assetId);
                    }
                }, exception);

                throw new AssetUploadException(exception.getMessage());
            }

            // asset metadata
            HashMap<String, Object> metadata = new HashMap<String, Object>();
            if (assetTemplate.getMetadata() != null) {
                for (String key : assetTemplate.getMetadata().keySet())
                    metadata.put(key, assetTemplate.getMetadata().get(key));
            }
            metadata.put("assetId", assetId);
            log.jsonInfo("Asset upload Saving metadata ", metadata);
            Mono<Void> metadataResponse = assetTemplate.getMetadata() == null ? Mono.empty() : bronteAssetService.saveMetadata(assetSummary.getId(), assetTemplate.getMetadata());
            // persist all the asset data
            return assetGateway.persist(assetSummary)
                    .doOnEach(log.reactiveInfoSignal("Asset upload service: asset summary saved"))
                    .singleOrEmpty()
                    // persist the asset source
                    .then(bronteAssetService.saveAssetSource(assetSummary, file, assetUrl, mimeType)
                                  .doOnEach(log.reactiveInfoSignal("Asset upload service: asset source saved"))
                            // this handle implementation based on asset media types.
                                  .flatMap(assetSource -> bronteAssetTypeHandler.handle(assetSource,
                                                                                        assetUrn,
                                                                                        assetTemplate.getMetadata(),
                                                                                        assetMediaType)
                                          .doOnEach(log.reactiveInfoSignal(
                                                  "Asset upload service: asset optimization event sent"))))
                    .then(metadataResponse)
                    .doOnEach(log.reactiveInfoSignal("Asset upload service: asset metadata saved"))
                    .thenReturn(assetSummary);

        } catch (IOException e) {
            log.error(String.format("Error while saving asset %s to the temporary file", assetTemplate.getOriginalFileName()), e);
            throw new AssetUploadException("error while saving asset");
        } finally {
            if (tmpFilePath != null) {
                try {
                    Files.delete(tmpFilePath);
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Deleted temporary file %s", tmpFilePath));
                    }
                } catch (IOException e) {
                    log.error("Error while deleting the temporary file: ", e);
                }
            }
        }
    }

    /**
     * Get the asset media type based on the file extension
     *
     * @param fileExtension the file extension to derive the media type from
     * @return the asset media type
     * @throws UnsupportedAssetException when the file extension is not supported
     */
    public AssetMediaType getAssetMediaType(final String fileExtension, final Map<String, String> metadata) {
        final String extension = fileExtension.replace(".", "");

        String mediaType = metadata != null ? metadata.get(AssetConstant.ICON_MEDIA_TYPE) : null;

        if (mediaType != null && mediaType.equalsIgnoreCase(AssetMediaType.ICON.name())
                && AllowedAsset.ICON.allows(extension)) {
            return AssetMediaType.ICON;
        }

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
            case ICON:
                return IconSourceName.ORIGINAL.name().toLowerCase();
            default:
                throw new UnsupportedAssetException("invalid media type");
        }
    }
}
