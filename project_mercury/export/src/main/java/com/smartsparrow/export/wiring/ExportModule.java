package com.smartsparrow.export.wiring;


import static com.google.common.base.Strings.isNullOrEmpty;

import javax.inject.Singleton;

import org.redisson.api.RedissonReactiveClient;
import org.slf4j.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.smartsparrow.config.OperationsRedisConfig;
import com.smartsparrow.config.service.ConfigurationService;
import com.smartsparrow.export.route.CoursewareExportRoute;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ExportModule extends AbstractModule {

    private final static Logger log = MercuryLoggerFactory.getLogger(ExportModule.class);

    @Override
    protected void configure() {
        bind(CoursewareExportRoute.class);
        bind(RedissonReactiveClient.class)
                .annotatedWith(Operations.class)
                .toProvider(OperationsRedissonClientProvider.class)
                .in(Singleton.class);
    }

    @Provides
    @Singleton
    @SuppressFBWarnings(value = "DM_EXIT", justification = "should exit on missing configuration props")
    public ExportConfig provideCoursewareExportConfig(ConfigurationService configurationService) {
        ExportConfig exportConfig = configurationService.get(ExportConfig.class, "courseware_export");
        log.info("Courseware export config is {}", exportConfig);
        // Allow for override or setting of the config by using:
        //   -Dcourseware_export.submitTopicNameOrArn=submit-topic-name
        exportConfig.setSubmitTopicNameOrArn(System.getProperty("courseware_export.submitTopicNameOrArn",
                exportConfig.getSubmitTopicNameOrArn()));
        //   -Dcourseware_export.resultQueueName=result-queue-name
        exportConfig.setResultQueueName(System.getProperty("courseware_export.resultQueueName",
                exportConfig.getResultQueueName()));
        //   -Dcourseware_export.errorQueueName=error-queue-name
        exportConfig.setErrorQueueName(System.getProperty("courseware_export.errorQueueName",
                exportConfig.getErrorQueueName()));
        //   -Dcourseware_export.delayQueueNameOrArn=delay-queue-name
        exportConfig.setDelayQueueNameOrArn(System.getProperty("courseware_export.delayQueueNameOrArn",
                exportConfig.getDelayQueueNameOrArn()));
        //   -Dcourseware_export.bucketName=workspace.ap-southeast-2.bronte.dev-prsn.com/export
        exportConfig.setBucketName(System.getProperty("courseware_export.bucketName",
                exportConfig.getBucketName()));
        //   -Dcourseware_export.snippetBucketName=workspace.ap-southeast-2.bronte.dev-prsn.com
        exportConfig.setSnippetBucketName(System.getProperty("courseware_export.snippetBucketName",
                exportConfig.getSnippetBucketName()));
        //   -Dcourseware_export.bucketUrl=https://workspace-bronte-dev.pearson.com/export
        exportConfig.setBucketUrl(System.getProperty("courseware_export.bucketUrl",
                exportConfig.getBucketUrl()));

        // sanity check that arguments exist.
        boolean fatal = false;
        if (isNullOrEmpty(exportConfig.getSubmitTopicNameOrArn())) {
            log.error("missing configuration value: courseware_export.submitTopicNameOrArn");
            fatal = true;
        }

        if (isNullOrEmpty(exportConfig.getResultQueueName())) {
            log.error("missing configuration value: courseware_export.resultQueueName");
            fatal = true;
        }

        if (isNullOrEmpty(exportConfig.getErrorQueueName())) {
            log.error("missing configuration value: courseware_export.errorQueueName");
            fatal = true;
        }

        if (isNullOrEmpty(exportConfig.getDelayQueueNameOrArn())) {
            log.error("missing configuration value: courseware_export.delayQueueNameOrArn");
            fatal = true;
        }

        if (isNullOrEmpty(exportConfig.getBucketName())) {
            log.error("missing configuration value: courseware_export.bucketName");
            fatal = true;
        }

        if (isNullOrEmpty(exportConfig.getBucketUrl())) {
            log.error("missing configuration value: courseware_export.bucketUrl");
            fatal = true;
        }

        // die if missing properties.
        if (fatal) {
            System.exit(-1);
        }

        return exportConfig;
    }

    @Provides
    @Singleton
    @SuppressFBWarnings(value = "DM_EXIT", justification = "should exit on missing configuration props")
    public OperationsRedisConfig provideOperationsRedisConfig(ConfigurationService configurationService) {
        OperationsRedisConfig operationsRedisConfig = configurationService.get(OperationsRedisConfig.class, "operations_redis");


        // sanity check that arguments exist.
        boolean fatal = false;
        if (isNullOrEmpty(operationsRedisConfig.getAddress())) {
            log.error("missing configuration value: operations_redis.address");
            fatal = true;
        }

        if (isNullOrEmpty(operationsRedisConfig.getPassword())) {
            log.error("missing configuration value: operations_redis.password");
            fatal = true;
        }

        // die if missing properties.
        if (fatal) {
            System.exit(-1);
        }

        return operationsRedisConfig;
    }
}
