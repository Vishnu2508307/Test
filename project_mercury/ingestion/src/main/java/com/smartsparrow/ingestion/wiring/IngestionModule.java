package com.smartsparrow.ingestion.wiring;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;

import static com.google.common.base.Strings.isNullOrEmpty;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.ingestion.route.IngestionRoute;
import com.smartsparrow.util.log.MercuryLoggerFactory;


public class IngestionModule extends AbstractModule {

    private final static Logger log = MercuryLoggerFactory.getLogger(IngestionModule.class);

    @Override
    protected void configure() {
        bind(IngestionRoute.class);
    }

    @Provides
    @Singleton
    @SuppressFBWarnings(value = "DM_EXIT", justification = "should exit on missing configuration props")
    public IngestionConfig provideIngestionConfig(ConfigurationService configurationService) {
        IngestionConfig ingestionConfig = configurationService.get(IngestionConfig.class, "self_ingestion");
        log.info("ingestion config is {}", ingestionConfig);

        // Allow for override or setting of the config by using:
        //   -Dself_ingestion.adapterEpubQueueNameOrArn=adapter-topic-name
        ingestionConfig.setAdapterEpubQueueNameOrArn(System.getProperty("self_ingestion.adapterEpubQueueNameOrArn",
                                                                   ingestionConfig.getAdapterEpubQueueNameOrArn()));
        //   -Dself_ingestion.adapterEpubQueueNameOrArn=adapter-topic-name
        ingestionConfig.setAdapterDocxQueueNameOrArn(System.getProperty("self_ingestion.adapterDocxQueueNameOrArn",
                                                                        ingestionConfig.getAdapterDocxQueueNameOrArn()));
        //   -Dself_ingestion.ambrosiaIngestionQueueNameOrArn=ambrosia-queue-name
        ingestionConfig.setAmbrosiaIngestionQueueNameOrArn(System.getProperty("self_ingestion.ambrosiaIngestionQueueNameOrArn",
                                                                  ingestionConfig.getAmbrosiaIngestionQueueNameOrArn()));
        //   -Dself_ingestion.adapterEpubQueueNameOrArn=adapter-topic-name
        ingestionConfig.setIngestionCancelQueueNameOrArn(System.getProperty("self_ingestion.ingestionCancelQueueNameOrArn",
                ingestionConfig.getIngestionCancelQueueNameOrArn()));
        //   -Dself_ingestion.bucketName=workspace.ap-southeast-2.bronte.dev-prsn.com/ingestion
        ingestionConfig.setBucketName(System.getProperty("self_ingestion.bucketName",
                                                         ingestionConfig.getBucketName()));
        //   -Dself_ingestion.bucketUrl=https://workspace-bronte-dev.pearson.com/ingestion
        ingestionConfig.setBucketUrl(System.getProperty("self_ingestion.bucketUrl",
                                                        ingestionConfig.getBucketUrl()));

        // sanity check that arguments exist.
        boolean fatal = false;
        if (isNullOrEmpty(ingestionConfig.getAdapterEpubQueueNameOrArn())) {
            log.error("missing configuration value: self_ingestion.adapterEpubQueueNameOrArn");
            fatal = true;
        }

        if (isNullOrEmpty(ingestionConfig.getAdapterDocxQueueNameOrArn())) {
            log.error("missing configuration value: self_ingestion.adapterDocxQueueNameOrArn");
            fatal = true;
        }

        if (isNullOrEmpty(ingestionConfig.getAmbrosiaIngestionQueueNameOrArn())) {
            log.error("missing configuration value: self_ingestion.ambrosiaIngestionQueueNameOrArn");
            fatal = true;
        }

        if (isNullOrEmpty(ingestionConfig.getIngestionCancelQueueNameOrArn())) {
            log.error("missing configuration value: self_ingestion.ingestionCancelQueueNameOrArn");
            fatal = true;
        }

        if (isNullOrEmpty(ingestionConfig.getBucketName())) {
            log.error("missing configuration value: self_ingestion.bucketName");
            fatal = true;
        }

        if (isNullOrEmpty(ingestionConfig.getBucketUrl())) {
            log.error("missing configuration value: self_ingestion.bucketUrl");
            fatal = true;
        }

        // die if missing properties.
        if (fatal) {
            System.exit(-1);
        }

        return ingestionConfig;
    }
}
