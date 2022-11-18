package com.smartsparrow.wiring.asset;

import com.smartsparrow.asset.data.AlfrescoAsset;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.BronteAsset;
import com.smartsparrow.asset.data.ExternalAsset;
import com.smartsparrow.asset.data.MathAsset;
import com.smartsparrow.asset.route.AlfrescoAssetRoute;
import com.smartsparrow.asset.route.AssetRoute;
import com.smartsparrow.asset.service.BronteAssetEmptyTypeHandler;
import com.smartsparrow.asset.service.BronteAssetIconTypeHandler;
import com.smartsparrow.asset.service.BronteAssetImageTypeHandler;
import com.smartsparrow.wiring.AbstractAssetModule;

/**
 * Specify the asset bindings that are required in the workspace
 */
public class WorkspaceAssetModule extends AbstractAssetModule {

    @Override
    protected void configure() {
        super.configure();
    }

    @Override
    public void decorate() {
        bind(AssetRoute.class);
        bind(AlfrescoAssetRoute.class);

        assetTypes.addBinding(AssetProvider.AERO).to(BronteAsset.class);
        assetTypes.addBinding(AssetProvider.EXTERNAL).to(ExternalAsset.class);
        assetTypes.addBinding(AssetProvider.ALFRESCO).to(AlfrescoAsset.class);
        assetTypes.addBinding(AssetProvider.MATH).to(MathAsset.class);

        assetMediaType.addBinding(AssetMediaType.IMAGE).to(BronteAssetImageTypeHandler.class);
        assetMediaType.addBinding(AssetMediaType.ICON).to(BronteAssetIconTypeHandler.class);
        assetMediaType.addBinding(AssetMediaType.AUDIO).to(BronteAssetEmptyTypeHandler.class);
        assetMediaType.addBinding(AssetMediaType.VIDEO).to(BronteAssetEmptyTypeHandler.class);
        assetMediaType.addBinding(AssetMediaType.DOCUMENT).to(BronteAssetEmptyTypeHandler.class);
    }
}
