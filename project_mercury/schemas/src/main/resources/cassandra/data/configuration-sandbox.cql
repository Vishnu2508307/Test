-- The purpose of this file for now just to keep queries for configuration for later usage.
--
-- Inserting default configuration to config.env
--
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'plugin', '{
    "distribution.bucketName" : "sandbox-aero-plugin.phx-spr.com/distribution",
    "repository.bucketName": "sandbox-aero-plugin.phx-spr.com/repository",
    "distribution.publicUrl":"http://sandbox-aero-plugin.phx-spr.com.s3-website-ap-southeast-2.amazonaws.com",
    "allowSync": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'schemas', '{
    "bucketName" : "sandbox-aero-plugin.phx-spr.com",
    "prefix":"schemas"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'assets', '{
    "bucketName" : "sandbox-aero.phx-spr.com","prefix":"assets",
    "publicUrl":"http://sandbox-aero.phx-spr.com.s3-website-ap-southeast-2.amazonaws.com",
    "submitTopicNameOrArn": "courseware-asset-resize-submit",
    "delayQueueNameOrArn": "courseware-asset-resize-retry-delay",
    "alfrescoUrl": "https://usppewip.cms.pearson.com",
    "alfrescoPushDelayTime": 5
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'routeConsumers', '{
    "leanerEvaluateCompleteConsumers": 50,
    "learnerStudentScopeSetConsumers": 10,
    "leanerProgressBroadcastConsumers": 10,
    "competencyDocumentUpdateConsumers": 2
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'redis', '{
    "address":"redis://redis:6379",
    "password":"5k6LyvSmUBqM9TpX6gTCDA4pV8Z7N5tJ"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'operations_redis', '{
    "address":"redis://redis:6379",
    "password":"5k6LyvSmUBqM9TpX6gTCDA4pV8Z7N5tJ",
    "connectionPoolSize":64,
    "connectionMinimumIdleSize":10
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'cache', '{
    "enableLearnerCache": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'sso.oidc', '{
    "redirectUrl": "https://sandbox-aero.phx-spr.com/sso/oidc/to",
    "callbackUrl": "https://sandbox-aero.phx-spr.com/sso/oidc/callback"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'sso.ies', '{
    "baseUrl": "http://172.0.199.10:9999/services/rest"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'sso.mycloud', '{
    "baseUrl": "http://172.0.199.10:9999/services/rest"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'sso.registrar', '{
    "baseUrl": "https://user-association.stg-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'passport', '{
    "baseUrl": "http://172.0.199.10:9999/services/rest"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'braintree', '{
    "environment": "sandbox",
    "merchantId": "nnnz6ywxkthv7vk5",
    "publicKey": "92fvzc8t4955jqm5",
    "privateKey": "71a53ffbd700bb0795e47ba43f8c8e44",
    "merchantAccountsByCurrency": {
        "AUD":"smartSparrowAUD",
        "USD":"dj397dhnxzb4ww88"
    }
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'system.credentials', '{
    "userId": "ffffffff5eb5bc8f5d2f5b01312aa65e",
    "password": "ZbY58cH2gdbxFgsCnyVvyWdJNduRLgne",
    "username": "gpd_bronte_system",
    "environment": "INT"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'cassandra', '{
        "contactPoints":["cassandra_node1"],
        "username":"cassandra",
        "password":"cassandra",
        "localMaxRequestsPerConnection": 2048,
        "maxQueueSize": 512,
        "certificate":"-----BEGIN CERTIFICATE-----
MIIDRzCCAi+gAwIBAgIEfRdw5zANBgkqhkiG9w0BAQsFADBTMQwwCgYDVQQGEwNh
dXMxGTAXBgNVBAoTEHNtYXJ0c3BhcnJvdy5jb20xEDAOBgNVBAsTB21lcmN1cnkx
FjAUBgNVBAMTDWNhc3NhbmRyYS1kZXYwIBcNMTgwNDE5MDY1NDU4WhgPMjExODAz
MjYwNjU0NThaMFMxDDAKBgNVBAYTA2F1czEZMBcGA1UEChMQc21hcnRzcGFycm93
LmNvbTEQMA4GA1UECxMHbWVyY3VyeTEWMBQGA1UEAxMNY2Fzc2FuZHJhLWRldjCC
ASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAIiGhnJKUjCi06lM5Ql5vr7u
eOHrQRhgrLVb3RvZxwmPtIREkWSVKtPAPrOHfBu68foCvLGzosxE/7qlYsicunRY
qymqNTrTvgpvkKUR+Z/7lc2Feg0UyfaYU+rY08FL1+XS9BRhC78OpGdvzq3bXnoL
fZDNWKWEevrKnQBgxrGNAo0C/xzGQI0T2pfeV+zfmjUJ4awwc0AxOUq/OPjZo4oh
Nxd7+k1IJHjrx/czdjHcgqHKmGPyCB5lzHdLrSSWRfaQuk7OR1X+dS6OCSB3PZ8S
Di7bTENucX89sKFav5lWDWvIxOtnUkKe7pr2zCjPLnx9lj19ypucDVaLRUyCgwsC
AwEAAaMhMB8wHQYDVR0OBBYEFNFRyCgkCW4BQZ+GAoFp+3D8cq5IMA0GCSqGSIb3
DQEBCwUAA4IBAQBjtCSId4peUhNh07EN5/hIsbCfE5wygRFtoQHQtoJvt7lsor2A
1YObogLr8XGVjONGwX1QvbAEXRlHh0uGUQqC93C8i2ArcLo8q7YxSRorTc039hp0
d/LfDM4rYLyPz4vEeHqP4A+83nrrWYkc+jkXrR6DrivSD/XcNopxprgy4ISNseMi
M2MXQ9GxAgVHjLGjGGyvRarMzowZwX6utzpwSg4qEdoyca0lAQKAFVW8urtfQEG3
smhzxP3EfJeqHNGxfsBpoT/3BCsms9+dGcaO/OZyTfAHW6FdyOOFt95nI1Ev1WIs
H2rbUrFrDphogBQopCaY4vi18magmH/kj2mv
-----END CERTIFICATE-----"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'ext_http', '{
    "submitTopicNameOrArn": "ext-http-submit",
    "delayQueueNameOrArn": "ext-http-delay-queue"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'learning_analytics', '{
    "adminBaseURL": "https://messaging-admin-int.dev-prsn.com",
    "appName": "gpd_bronte_system",
    "originatingSystemCode": "Bronte",
    "statusBaseURL": "https://messaging-status-int.dev-prsn.com",
    "publishBaseURL": "https://messaging-publishing-int.dev-prsn.com",
    "schemaRegistryBaseURL": "https://messaging-registry.prd-prsn.com",
    "autobahnEnvironment": "INT"
}');
-- TODO: change to a Citrus endpoint
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'csg', '{
    "indexUri": "https://ignored",
    "applicationId": "bronte",
    "enabled": false,
    "batchSize": 20,
    "completionTime": 2500
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'plp_sqs', '{
    "queueName": "BRNT-SANDBOX",
    "region": "us-east-2",
    "queueOwnerAWSAccountId": "815492460363",
    "enabled": "false"
}');

INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'courseware_export', '{
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
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'self_ingestion', '{
    "adapterEpubQueueNameOrArn": "ingestion-adapter-epub-submit",
    "adapterDocxQueueNameOrArn": "ingestion-adapter-docx-submit",
    "ambrosiaIngestionQueueNameOrArn": "ingestion-ambrosia-submit",
    "ingestionCancelQueueNameOrArn": "ingestion-cancel-submit",
    "bucketName": "ingestion-ap-southeast-2-bronte-pearsondev-tech",
    "bucketUrl": "https://ingestion-bronte-dev.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'plugin_log', '{
    "enabled": true,
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
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'evaluation.feature', '{
    "configuredFeature": "REACTIVE_EVALUATION"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'oculus.url', '{
    "baseUrl": "http://172.0.199.10:9999/services/rest"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'evaluation.mode', '{
    "processingMode": "WAIT_PENDING"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'math', '{
    "mathMLUri": "http://172.0.199.10:9999/services/rest",
    "mathMLPath": "/demo/plugins/app/showimage",
    "metrics": true,
    "centerbaseline": false,
    "bucketName" : "sandbox-aero.phx-spr.com",
    "submitTopicNameOrArn": "math-asset-resolver-submit",
    "delayQueueNameOrArn": "math-asset-resolver-retry-delay",
    "enabled": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'grade_passback', '{
    "masteringGradeSyncUrl": "https://api.staging.mastering.pearsoncmg.com",
    "checkGradePassbackQuestionCount": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'publish_metadata', '{
    "masteringLabMetadataUrl": "https://bronteil.mx-dev.pearsondev.tech/bronteil-service/labMetadata"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'user_content', '{
    "cacheNameOrArn": "user-content-request-notification"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('SANDBOX', 'lti.credentials', '{
    "key": "",
    "secret": ""
}');