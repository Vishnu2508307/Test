package com.smartsparrow.wiring;

import static com.google.common.base.Strings.isNullOrEmpty;

import javax.inject.Named;
import javax.inject.Singleton;

import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.smartsparrow.asset.data.Asset;
import com.smartsparrow.asset.data.AssetGateway;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.service.AlfrescoAssetService;
import com.smartsparrow.asset.service.AssetService;
import com.smartsparrow.asset.service.BronteAssetResponse;
import com.smartsparrow.asset.service.BronteAssetService;
import com.smartsparrow.asset.service.BronteAssetType;
import com.smartsparrow.asset.service.ExternalAssetService;
import com.smartsparrow.config.AssetConfig;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.data.AbstractModuleDecorator;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Base definition of the asset module. This class initializes the common binding required by asset
 * but delegates the required implementations and any additional binding to the child class.
 * This allows to diversify the wiring of apis for workspace, learnspace or default instances
 */
public abstract class AbstractAssetModule extends AbstractModuleDecorator {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AbstractAssetModule.class);

    protected MapBinder<AssetProvider, Asset> assetTypes;
    protected MapBinder<AssetMediaType, BronteAssetType<? extends BronteAssetResponse>> assetMediaType;

    @Override
    public abstract void decorate();

    @Override
    protected void configure() {
        assetTypes = MapBinder.newMapBinder(binder(),//
                new TypeLiteral<AssetProvider>(){
                }, //
                new TypeLiteral<Asset>(){
                }); //

        // setup binding for asset type consumers
        assetMediaType = MapBinder.newMapBinder(binder(), new TypeLiteral<AssetMediaType>(){
                }, //
                new TypeLiteral<BronteAssetType<? extends BronteAssetResponse>>() {
                }); //
        this.decorate();
    }

    @Provides
    @Singleton
    @Named("ExternalAssetService")
    AssetGateway provideAssetGateway(AssetGateway assetGateway) {
        return assetGateway;
    }

    @Provides
    @Singleton
    @Named("ExternalAssetService")
    AssetService provideAssetService(ExternalAssetService externalAssetService) {
        return externalAssetService;
    }

    @Provides
    @Singleton
    @Named("BronteAssetService")
    AssetService provideAssetService(BronteAssetService bronteAssetService) {
        return bronteAssetService;
    }

    @Provides
    @Singleton
    @Named("AlfrescoAssetService")
    AssetService provideAssetService(AlfrescoAssetService alfrescoAssetService) {
        return alfrescoAssetService;
    }

    @Provides
    @Singleton
    @SuppressFBWarnings(value = "DM_EXIT", justification = "should exit on missing configuration props")
    public AssetConfig provideCoursewareAssetConfig(ConfigurationService configurationService) {
        AssetConfig assetConfig = configurationService.get(AssetConfig.class, "assets");
        log.info("Courseware asset config is {}", assetConfig);
        // Allow for override or setting of the config by using:
        //   -Dassets.submitTopicNameOrArn=submit-topic-name
        assetConfig.setSubmitTopicNameOrArn(System.getProperty("assets.submitTopicNameOrArn",
                assetConfig.getSubmitTopicNameOrArn()));
        //   -Dassets.delayQueueNameOrArn=delay-queue-name
        assetConfig.setDelayQueueNameOrArn(System.getProperty("assets.delayQueueNameOrArn",
                assetConfig.getDelayQueueNameOrArn()));

        // sanity check that arguments exist.
        boolean fatal = false;
        if (isNullOrEmpty(assetConfig.getSubmitTopicNameOrArn())) {
            log.error("missing configuration value: assets.submitTopicNameOrArn");
            fatal = true;
        }

        if (isNullOrEmpty(assetConfig.getDelayQueueNameOrArn())) {
            log.error("missing configuration value: assets.delayQueueNameOrArn");
            fatal = true;
        }

        // die if missing properties.
        if (fatal) {
            System.exit(-1);
        }

        return assetConfig;
    }
}
