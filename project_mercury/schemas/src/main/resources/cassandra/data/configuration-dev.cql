-- The purpose of this file for now just to keep queries for configuration for later usage.
--
-- Inserting default configuration to config.env
--
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'plugin', '{
    "distribution.bucketName": "dev-aero-plugin.ap-southeast-2.bronte.dev-prsn.com/distribution",
    "repository.bucketName": "dev-aero-plugin.ap-southeast-2.bronte.dev-prsn.com/repository",
    "distribution.publicUrl": "https://plugin-distribution-bronte-dev.pearson.com",
    "repository.publicUrl": "https://plugin-repository-bronte-dev.pearson.com",
    "allowSync": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'schemas', '{
    "bucketName" : "dev-aero-plugin.ap-southeast-2.bronte.dev-prsn.com",
    "prefix":"schemas"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'assets', '{
    "bucketName" : "dev-aero.ap-southeast-2.bronte.dev-prsn.com",
    "prefix":"assets",
    "publicUrl":"https://assets-bronte-dev.pearson.com",
    "submitTopicNameOrArn": "courseware-asset-resize-submit",
    "delayQueueNameOrArn": "courseware-asset-resize-retry-delay",
    "alfrescoUrl": "https://usppewip.cms.pearson.com",
    "alfrescoPushDelayTime": 10
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'routeConsumers', '{
    "leanerEvaluateCompleteConsumers": 16,
    "learnerStudentScopeSetConsumers": 4,
    "leanerProgressBroadcastConsumers": 4,
    "competencyDocumentUpdateConsumers": 1
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'redis', '{
    "address":"rediss://master.dev-mercury.a2hwhq.apse2.cache.amazonaws.com:6379",
    "password":"Uy2y8jkxPk9RxmRs4YBQrZkBnNbpYkpF"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'operations_redis', '{
    "address":"rediss://master.dev-mercury.a2hwhq.apse2.cache.amazonaws.com:6379",
    "password":"Uy2y8jkxPk9RxmRs4YBQrZkBnNbpYkpF",
    "connectionPoolSize":64,
    "connectionMinimumIdleSize":10
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'cache', '{
    "enableLearnerCache": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'sso.oidc', '{
    "redirectUrl": "https://dev-aero.global.ssl.fastly.net/sso/oidc/to",
    "callbackUrl": "https://dev-aero.global.ssl.fastly.net/sso/oidc/callback"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'sso.ies', '{
    "baseUrl": "https://tst-piapi-internal.dev-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'sso.mycloud', '{
    "baseUrl": "https://mycloudtest.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'sso.registrar', '{
    "baseUrl": "https://user-association-qa.dev-prsn.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'passport', '{
    "baseUrl": "https://passport.dev-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'braintree', '{
    "environment": "sandbox",
    "merchantId": "nnnz6ywxkthv7vk5",
    "publicKey": "92fvzc8t4955jqm5",
    "privateKey": "71a53ffbd700bb0795e47ba43f8c8e44",
    "merchantAccountsByCurrency": {
        "AUD":"smartSparrowAUD",
        "USD":"dj397dhnxzb4ww88"
    }
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'system.credentials', '{
    "userId": "ffffffff5eb5bc8f5d2f5b01312aa65e",
    "password": "ZbY58cH2gdbxFgsCnyVvyWdJNduRLgne",
    "username": "gpd_bronte_system",
    "environment": "INT"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'cassandra', '{
        "contactPoints":["10.87.16.11", "10.87.16.93", "10.87.16.59"],
        "username":"cassandra",
        "password":"cassandra",
        "localMaxRequestsPerConnection": 2048,
        "maxQueueSize": 512,
        "certificate":"-----BEGIN CERTIFICATE-----
MIIFsTCCA5mgAwIBAgIBATANBgkqhkiG9w0BAQsFADB6MQswCQYDVQQGEwJBVTEe
MBwGA1UECgwVSW5zdGFjbHVzdHIgUHR5LiBMdGQuMREwDwYDVQQLDAhDbHVzdGVy
czE4MDYGA1UEAwwvQ2x1c3RlciAzOWVlZjIwOS1kYzE5LTRkMGYtOWYyMi00MjVh
ZThjOGRhMmQgQ0EwHhcNMjIwMTA1MDAxNDMwWhcNMzIwMTA1MDAwMDAwWjB6MQsw
CQYDVQQGEwJBVTEeMBwGA1UECgwVSW5zdGFjbHVzdHIgUHR5LiBMdGQuMREwDwYD
VQQLDAhDbHVzdGVyczE4MDYGA1UEAwwvQ2x1c3RlciAzOWVlZjIwOS1kYzE5LTRk
MGYtOWYyMi00MjVhZThjOGRhMmQgQ0EwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAw
ggIKAoICAQDe9XK8tA/ByDCs4XkKVGIxFmDWPWhWMIN/QoLMOeyp/LUpy4CYCA++
LDiCccdGhE5FMderx7/MKq3WY6l34OUXR08BxnRPwMtyBNAEIpp/408QuRB6WLm2
aaA09aUOoDvegj4Jh1HbYYHrTJKMaUz4p2W95YPdIKPCm0orXlJgW4QvfvoeRYg4
Olk3Zk/h6QU7FChlIr9iekgCxaCEylj+N/irJzCHRT5EVKdrMGI6oX4nSpv9R6eh
PhsJ15/4iitqCwZDORp+qMRTP7/8UxROdrw031woWrTcw1b/m7oxXRbl7wOwvH2H
gj1U+g16jkmaPVNsB5KE6TxCJrHOaiIkovJJPHMzuZUVbudr82IuV/epyv5e98kp
ohk2jJ9TJzu85/zuEI7lBAF0GUX4in/Z3axvl+jCMVpz9KfglMuXWvLBlB/2dMUt
fsZuLpXVxSAFI/hDYp0XODpTc7t5jl8BHVX7kC4N6oRC0046BJ9xY/320mUMToDb
iQChx4MoyrzJNa/kZ8VpIhM2KIs6rYSSkKCNuyvbHsHTiI3mKDbCSLlQQMlUcgP8
bEC7G8q1xtzK6lJoGZDdmyzvxUP+zm+jAVr01Cj5HnHZMkTn2mpzULlK23iszsBL
b8+19YoQ3hHdnDLGUHLnGUnMo307tNZfDScnfphzWVRL94dGlytyBwIDAQABo0Iw
QDAdBgNVHQ4EFgQUS/JDkjnfeCSJYn03rtKdpTZogMYwDwYDVR0TAQH/BAUwAwEB
/zAOBgNVHQ8BAf8EBAMCAQYwDQYJKoZIhvcNAQELBQADggIBANk/+YBzpbOe+0Sn
3AUeIeynTY7/gRdEZWxbnCSgPrvAbaumZx4NloN18JDiJ3B1ey13Mf1cvkN4e5nl
OD4zs4jSrkho6IZa7xsXe+qBXnCgpRlDcp8gwrIx2uOEa0vcOR7R8DYjP5k3jCqz
7xKNFUwGTZPb0Mcg4TzjsXC+2xcPwvRXTW6lKIbMO8tvQ99gV8/snlLA//oRdPDF
Tgl5abflwg4GxChAxqbKwz0Dpp6oVRoaj+1zCzJd8swrF5vqxVwDkyX2eJfg6Hu1
JVIk12u6Bxc8oCl8NwBSgjIwCnLpqXIpiDSYM2PChWWCa3N1xTUjiIqugQM9Jagj
RMbZ95hag2zIA9zSpOty1/7avyaHCV9ebFWKPKP848phxieQxNnHWnQpMCY5Gsyw
DasFM96oe83PZqPzCWydEyIXy9wCRopOZj+io+KuZTVR/J5qSRZeA8e/N3vuTbkr
yf5VY7X0HySVrBNFMNpuED+17QGqmNkHqA3NQoHIEGvYwqkBpgsJ8wveZrgVK+uI
Ak2LT+gb0XYTJWMB7ECagIxD2RFSOPqd0cw5FMaDy8NISiJ3B/w24MNqIF3582NR
MfkHCEJqAWyxM2nLV9Kx/qdBHr7uc1cJ6HLXzjXYHB1K4fqjMsQfkiw2mJbSJnR4
4nizXua+V3KbkZAFJtGeAn2uCnbj
-----END CERTIFICATE-----"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'ext_http', '{
    "submitTopicNameOrArn": "ext-http-submit",
    "delayQueueNameOrArn": "ext-http-delay-queue"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'learning_analytics', '{
    "adminBaseURL": "https://messaging-admin-int.dev-prsn.com",
    "appName": "gpd_bronte_system",
    "originatingSystemCode": "Bronte",
    "statusBaseURL": "https://messaging-status-int.dev-prsn.com",
    "publishBaseURL": "https://messaging-publishing-int.dev-prsn.com",
    "schemaRegistryBaseURL": "https://messaging-registry.prd-prsn.com",
    "autobahnEnvironment": "INT"
}');
-- TODO: change to Dev endpoint (which seems to never be working)
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'csg', '{
    "indexUri": "https://dragonfly-qa.stg-openclass.com/pxereader-cm/2.1/api/cm/documents",
    "applicationId": "bronte",
    "enabled": true,
    "batchSize": 20,
    "completionTime": 2500
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'plp_sqs', '{
    "queueName": "BRNT-DEV",
    "region": "us-east-2",
    "queueOwnerAWSAccountId": "815492460363",
    "enabled": "false"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'courseware_export', '{
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
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'self_ingestion', '{
    "adapterEpubQueueNameOrArn": "ingestion-adapter-epub-submit",
    "adapterDocxQueueNameOrArn": "ingestion-adapter-docx-submit",
    "ambrosiaIngestionQueueNameOrArn": "ingestion-ambrosia-submit",
    "ingestionCancelQueueNameOrArn": "ingestion-cancel-submit",
    "bucketName": "ingestion-ap-southeast-2-bronte-pearsondev-tech",
    "bucketUrl": "https://ingestion-bronte-dev.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'plugin_log', '{
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
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'evaluation.feature', '{
    "configuredFeature": "REACTIVE_EVALUATION"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'oculus.url', '{
    "baseUrl": "https://paperapi.stg-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'evaluation.mode', '{
    "processingMode": "WAIT_PENDING"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'math', '{
    "mathMLUri": "https://www.wiris.net",
    "mathMLPath": "/demo/plugins/app/showimage",
    "metrics": true,
    "centerbaseline": false,
    "bucketName" : "dev-aero.ap-southeast-2.bronte.dev-prsn.com",
    "submitTopicNameOrArn": "math-asset-resolver-submit",
    "delayQueueNameOrArn": "math-asset-resolver-retry-delay",
    "enabled": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'grade_passback', '{
    "masteringGradeSyncUrl": "https://api.staging.mastering.pearsoncmg.com",
    "checkGradePassbackQuestionCount": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'publish_metadata', '{
    "masteringLabMetadataUrl": "https://bronteil.mx-dev.pearsondev.tech/bronteil-service/labMetadata"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'user_content', '{
    "cacheNameOrArn": "user-content-request-notification"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('DEV', 'lti.credentials', '{
    "key": "Qa2ptmoNWTMcqlN8FgpGvk7PcVO6JBsQ",
    "secret": "RRRzJfQ9c6g_aWrRgkX9psKAKiwntFiCJ1k-5ItfRK0bOgv1"
}');
