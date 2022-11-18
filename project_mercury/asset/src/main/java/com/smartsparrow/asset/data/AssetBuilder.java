package com.smartsparrow.asset.data;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import com.smartsparrow.exception.IllegalArgumentFault;

public class AssetBuilder {

    private final Map<AssetProvider, Provider<Asset>> assetProviders;

    private AssetSummary assetSummary;
    private AlfrescoAssetData alfrescoAssetData;
    private MathAssetData mathAssetData;

    @Inject
    public AssetBuilder(final Map<AssetProvider, Provider<Asset>> assetProviders) {
        this.assetProviders = assetProviders;
    }

    public AssetBuilder setAssetSummary(AssetSummary assetSummary) {
        this.assetSummary = assetSummary;
        return this;
    }

    public AssetBuilder setAlfrescoAssetData(AlfrescoAssetData alfrescoAssetData) {
        this.alfrescoAssetData = alfrescoAssetData;
        return this;
    }

    public AssetBuilder setMathAssetData(MathAssetData mathAssetData) {
        this.mathAssetData = mathAssetData;
        return this;
    }

    public Asset build(final AssetProvider assetProvider) {
        final Provider<Asset> provider = assetProviders.get(assetProvider);

        switch (assetProvider) {
            case AERO:
                BronteAsset bronteAsset = (BronteAsset) provider.get();
                return bronteAsset.setId(assetSummary.getId())
                        .setOwnerId(assetSummary.getOwnerId())
                        .setSubscriptionId(assetSummary.getSubscriptionId())
                        .setAssetMediaType(assetSummary.getMediaType())
                        .setAssetVisibility(assetSummary.getVisibility())
                        .setHash(assetSummary.getHash());
            case EXTERNAL:
                ExternalAsset externalAsset = (ExternalAsset) provider.get();
                return externalAsset.setId(assetSummary.getId())
                        .setOwnerId(assetSummary.getOwnerId())
                        .setSubscriptionId(assetSummary.getSubscriptionId())
                        .setAssetMediaType(assetSummary.getMediaType())
                        .setAssetVisibility(assetSummary.getVisibility());
            case ALFRESCO:
                AlfrescoAsset alfrescoAsset = (AlfrescoAsset) provider.get();
                return alfrescoAsset.setId(assetSummary.getId())
                        .setOwnerId(assetSummary.getOwnerId())
                        .setSubscriptionId(assetSummary.getSubscriptionId())
                        .setAssetMediaType(assetSummary.getMediaType())
                        .setAssetVisibility(assetSummary.getVisibility())
                        .setAlfrescoId(alfrescoAssetData.getAlfrescoId())
                        .setName(alfrescoAssetData.getName())
                        .setLastModifiedDate(alfrescoAssetData.getLastModifiedDate())
                        .setLastSyncDate(alfrescoAssetData.getLastSyncDate())
                        .setVersion(alfrescoAssetData.getVersion());
            case MATH:
                MathAsset mathAsset = (MathAsset) provider.get();
                return mathAsset.setId(mathAssetData.getId())
                        .setHash(mathAssetData.getHash())
                        .setAltText(mathAssetData.getAltText())
                        .setMathML(mathAssetData.getMathML())
                        .setSvgShape(mathAssetData.getSvgShape())
                        .setSvgText(mathAssetData.getSvgText());
            default:
                throw new IllegalArgumentFault(String.format("Invalid asset provider: %s", assetProvider));
        }
    }
}
