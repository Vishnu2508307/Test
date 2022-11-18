-- The purpose of this file for now just to keep queries for configuration for later usage.
--
-- Inserting default configuration to config.env.
--
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'plugin', '{
    "distribution.bucketName" : "local-dev-aero-plugin.ap-southeast-2.bronte.dev-prsn.com/distribution",
    "repository.bucketName": "local-dev-aero-plugin.ap-southeast-2.bronte.dev-prsn.com/repository",
    "distribution.publicUrl": "https://plugin-distribution-bronte-dev.pearson.com",
    "repository.publicUrl": "https://plugin-repository-bronte-dev.pearson.com",
    "allowSync": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'schemas', '{
    "bucketName" : "local-dev-aero-plugin.ap-southeast-2.bronte.dev-prsn.com",
    "prefix":"schemas"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'assets', '{
    "bucketName" : "local-dev-aero.ap-southeast-2.bronte.dev-prsn.com",
    "prefix":"assets",
    "publicUrl":"http://local-dev-aero.ap-southeast-2.bronte.dev-prsn.com.s3-website-ap-southeast-2.amazonaws.com",
    "submitTopicNameOrArn": "courseware-asset-resize-submit",
    "delayQueueNameOrArn": "courseware-asset-resize-retry-delay",
    "alfrescoUrl": "https://usppewip.cms.pearson.com",
    "alfrescoPushDelayTime": 10
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'routeConsumers', '{
    "leanerEvaluateCompleteConsumers": 50,
    "learnerStudentScopeSetConsumers": 10,
    "leanerProgressBroadcastConsumers": 10,
    "competencyDocumentUpdateConsumers": 2
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'redis', '{
    "address":"redis://redis.local.phx-spr.com:6379",
    "password":"aelpredis"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'operations_redis', '{
    "address":"redis://redis.local.phx-spr.com:6379",
    "password":"aelpredis",
    "connectionPoolSize":64,
    "connectionMinimumIdleSize":10
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'cache', '{
    "enableLearnerCache": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'sso.oidc', '{
    "redirectUrl": "http:/localhost:8080/sso/oidc/to",
    "callbackUrl": "http://localhost:8080/sso/oidc/callback"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'sso.ies', '{
    "baseUrl": "https://tst-piapi-internal.dev-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'sso.mycloud', '{
    "baseUrl": "https://mycloudtest.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'sso.registrar', '{
    "baseUrl": "https://user-association-qa.dev-prsn.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'passport', '{
    "baseUrl": "https://passport.dev-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'braintree', '{
    "environment": "sandbox",
    "merchantId": "nnnz6ywxkthv7vk5",
    "publicKey": "92fvzc8t4955jqm5",
    "privateKey": "71a53ffbd700bb0795e47ba43f8c8e44",
    "merchantAccountsByCurrency": {
        "AUD":"smartSparrowAUD",
        "USD":"dj397dhnxzb4ww88"
    }
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'system.credentials', '{
    "userId": "ffffffff5eb5bc8f5d2f5b01312aa65e",
    "password": "ZbY58cH2gdbxFgsCnyVvyWdJNduRLgne",
    "username": "gpd_bronte_system",
    "environment": "INT"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'ext_http', '{
    "submitTopicNameOrArn": "local-ext-http-submit",
    "delayQueueNameOrArn": "local-ext-http-delay-queue"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'learning_analytics', '{
    "adminBaseURL": "https://messaging-admin-int.dev-prsn.com",
    "appName": "gpd_bronte_system",
    "originatingSystemCode": "Bronte",
    "statusBaseURL": "https://messaging-status-int.dev-prsn.com",
    "publishBaseURL": "https://messaging-publishing-int.dev-prsn.com",
    "schemaRegistryBaseURL": "https://messaging-registry.prd-prsn.com",
    "autobahnEnvironment": "INT"
}');
-- TODO: change to a Citrus endpoint
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'csg', '{
    "indexUri": "https://dragonfly-qa.stg-openclass.com/pxereader-cm/2.1/api/cm/documents",
    "applicationId": "bronte",
    "enabled": false,
    "batchSize": 20,
    "completionTime": 2500
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'plp_sqs', '{
    "queueName": "BRNT-DEV",
    "region": "us-east-2",
    "queueOwnerAWSAccountId": "815492460363",
    "enabled": "false"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'courseware_export', '{
    "submitTopicNameOrArn": "courseware-element-to-ambrosia-submit",
    "resultQueueName": "courseware-element-to-ambrosia-result",
    "errorQueueName": "courseware-element-to-ambrosia-error",
    "delayQueueNameOrArn": "courseware-element-to-ambrosia-retry-delay",
    "bucketName": "workspace.ap-southeast-2.bronte.dev-prsn.com/export",
    "snippetBucketName": "ambrosia-snippet.ap-southeast-2.bronte.dev-prsn.com",
    "bucketUrl": "https://workspace-bronte-dev.pearson.com/export",
    "concurrentConsumers": 5,
    "maxMessagesPerPoll": 1,
    "submitExportFailureTopic": "epub-transform-publication-notification",
    "waitTimeSeconds": 10,
    "snippetsStorage": "S3"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'self_ingestion', '{
    "adapterEpubQueueNameOrArn": "ingestion-adapter-epub-submit",
    "adapterDocxQueueNameOrArn": "ingestion-adapter-docx-submit",
    "ambrosiaIngestionQueueNameOrArn": "ingestion-ambrosia-submit",
    "ingestionCancelQueueNameOrArn": "ingestion-cancel-submit",
    "bucketName": "ingestion-ap-southeast-2-bronte-pearsondev-tech",
    "bucketUrl": "https://ingestion-bronte-dev.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'prelugin_log', '{
    "enabled": false,
    "bucketConfigs": [
      {
        "tableName":"generic_log_statement_by_plugin",
        "maxRecordCount":1000,
        "retentionPolicy":"WEEK",
        "logBucketInstances": 5
      },
      {
        "tableName":"workspace_log_statement_by_plugin",
        "maxRecordCount":1000,
        "retentionPolicy":"WEEK",
        "logBucketInstances": 5
      },
      {
        "tableName":"learnspace_log_statement_by_plugin",
        "maxRecordCount":1000,
        "retentionPolicy":"WEEK",
        "logBucketInstances": 5
      }
    ]
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'evaluation.feature', '{
    "configuredFeature": "REACTIVE_EVALUATION"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'oculus.url', '{
    "baseUrl": "https://paperapi.stg-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'evaluation.mode', '{
    "processingMode": "WAIT_PENDING"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'math', '{
    "mathMLUri": "https://www.wiris.net",
    "mathMLPath": "/demo/plugins/app/showimage",
    "metrics": true,
    "centerbaseline": false,
    "bucketName" : "local-dev-aero.ap-southeast-2.bronte.dev-prsn.com",
    "submitTopicNameOrArn": "math-asset-resolver-submit",
    "delayQueueNameOrArn": "math-asset-resolver-retry-delay",
    "enabled": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'grade_passback', '{
    "masteringGradeSyncUrl": "https://api.staging.mastering.pearsoncmg.com",
    "checkGradePassbackQuestionCount": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'publish_metadata', '{
    "masteringLabMetadataUrl": "https://bronteil.mx-dev.pearsondev.tech/bronteil-service/labMetadata"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'user_content', '{
    "cacheNameOrArn": "user-content-request-notification"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('LOCAL', 'lti.credentials', '{
    "key": "testKey",
    "secret": "testSecret"
}');
