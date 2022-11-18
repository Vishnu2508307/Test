-- The purpose of this file for now just to keep queries for configuration for later usage.
--
-- Inserting default configuration to config.env
--
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'plugin', '{
    "distribution.bucketName": "plugin.us-east-1.bronte-stg.pearsondev.tech/distribution",
    "repository.bucketName": "plugin.us-east-1.bronte-stg.pearsondev.tech/repository",
    "repository.publicUrl": "https://plugin-repository-bronte-stg.pearson.com",
    "distribution.publicUrl": "https://plugin-distribution-bronte-stg.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'schemas', '{
    "bucketName" : "plugin.us-east-1.bronte-stg.pearsondev.tech",
    "prefix":"schemas"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'assets', '{
    "bucketName" : "bronte.us-east-1.bronte-stg.pearsondev.tech",
    "prefix":"assets",
    "publicUrl":"https://assets-bronte-stg.pearson.com",
    "submitTopicNameOrArn": "courseware-asset-resize-submit",
    "delayQueueNameOrArn": "courseware-asset-resize-retry-delay",
    "alfrescoUrl": "https://usppewip.cms.pearson.com",
    "alfrescoPushDelayTime": 10
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'routeConsumers', '{
    "leanerEvaluateCompleteConsumers": 16,
    "learnerStudentScopeSetConsumers": 4,
    "leanerProgressBroadcastConsumers": 4,
    "competencyDocumentUpdateConsumers": 1
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'redis', '{
    "address":"rediss://master.stg-mercury.olzn5t.use1.cache.amazonaws.com:6379",
    "password":"gd7LYzR9WNZg84P2dvAQ7UkbXtzTYhxV"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'operations_redis', '{
    "address":"rediss://master.stg-mercury.olzn5t.use1.cache.amazonaws.com:6379",
    "password":"gd7LYzR9WNZg84P2dvAQ7UkbXtzTYhxV",
    "connectionPoolSize":64,
    "connectionMinimumIdleSize":10
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'cache', '{
    "enableLearnerCache": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'sso.oidc', '{
    "redirectUrl": "placeholder",
    "callbackUrl": placeholder""
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'sso.ies', '{
    "baseUrl": "https://int-piapi-internal.stg-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'sso.mycloud', '{
    "baseUrl": "https://mycloudtest.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'sso.registrar', '{
    "baseUrl": "https://user-association.stg-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'passport', '{
    "baseUrl": "https://passport-api.stg-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'braintree', '{
    "environment": "sandbox",
    "merchantId": "nnnz6ywxkthv7vk5",
    "publicKey": "92fvzc8t4955jqm5",
    "privateKey": "71a53ffbd700bb0795e47ba43f8c8e44",
    "merchantAccountsByCurrency": {
        "AUD":"smartSparrowAUD",
        "USD":"dj397dhnxzb4ww88"
    }
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'system.credentials', '{
    "userId": "ffffffff5eb5bcb75c771e0131a9efc0",
    "password": "QBw54THN8R7LAKfezRg3MWYFLJEYcRY9",
    "username": "gpd_bronte_system",
    "environment": "STG"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'cassandra', '{
        "contactPoints":["10.82.80.33","10.82.80.91","10.82.80.159"],
        "username":"cassandra",
        "password":"cassandra",
        "localMaxRequestsPerConnection": 2048,
        "maxQueueSize": 512,
        "certificate":"-----BEGIN CERTIFICATE-----
MIIFsTCCA5mgAwIBAgIBATANBgkqhkiG9w0BAQsFADB6MQswCQYDVQQGEwJBVTEe
MBwGA1UECgwVSW5zdGFjbHVzdHIgUHR5LiBMdGQuMREwDwYDVQQLDAhDbHVzdGVy
czE4MDYGA1UEAwwvQ2x1c3RlciA0YjkyYWI4Ni0yNDAxLTRiNzItOTU2ZS0zYmJk
YWUyZTk1ZWYgQ0EwHhcNMjEwMzE1MjI1NjQyWhcNMzEwMzE1MDAwMDAwWjB6MQsw
CQYDVQQGEwJBVTEeMBwGA1UECgwVSW5zdGFjbHVzdHIgUHR5LiBMdGQuMREwDwYD
VQQLDAhDbHVzdGVyczE4MDYGA1UEAwwvQ2x1c3RlciA0YjkyYWI4Ni0yNDAxLTRi
NzItOTU2ZS0zYmJkYWUyZTk1ZWYgQ0EwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAw
ggIKAoICAQDO4sHD2HUh90AhYBm8Lburi/soGKw66ioo9sJRIujkCHKfZ9ktGTaF
paDCkEPJDEScVV4DpGa4kG7EImgtiPgXz7psJq9Uhtn2w38kOEwTahK4MiZDwVKs
2uJeJ6553ff4o+d9VhgwKfIufv4JA3rUXlBXNF7jsvhTwFtpBHWdgKVFsEjj2HKo
OKYBJnR9S00jnb1J8AzAw96B6yUZ4t8fPPDGWkIujtub/mVBkEWlmRQj9HDENCri
bF2+RBVcqe/z4oD4Obe4+AX4/n3koIP3usggq3RUdmnzKTi6Zqeu0xjlDILaJ63z
1YwF53ZnhQF2BNqQph6TOqWCT4nYPMvFGtmFu0yyqcLiZWVS8H/61Jq/VnMYiQmA
Y7p6iBSnSQTYmi6kP1JkTI50PilwGy+pX7ZZV+/89oTSYEEvzhEkDREuFduy+CPC
vXZYeFSOc4NEVLkssRVDvP/XhxDYN2tTgMIyAfQ/pcJXmeVnjo7xnum54f/esM0I
0hX4niJRmkzq9Q9UDLvBi6JW3TjDicAxmHilozQg0YiPOwMQeWrMxXTRMWEWVacu
ypEgAhuLdVpF7aKlCz1aTiyf3ZoL0FNzmQ2H96/+rd8su3qkmh8u3Bxi4eoZbJcg
pMM0/rUGX1X3osZcTwNg62QkZBvRBscBekO/6+0G0DTONJWMh0tLnwIDAQABo0Iw
QDAdBgNVHQ4EFgQUTsFf8bo9YrMtKUO/hfiXVbmCcV0wDwYDVR0TAQH/BAUwAwEB
/zAOBgNVHQ8BAf8EBAMCAQYwDQYJKoZIhvcNAQELBQADggIBAAhKMlxXKkrHIOVr
eqbv26FU99Dnnq6I9tpXW/7t1zIyseynBp831OQF3ExM/A9KDxfdubyy/NOl/VK4
63Qh4Vq28mOPfCvAjRVsK53mVhLxMQ3esqBbQ/etp1osAqmJSNDO5KwtDkfw+t0z
7q83b965I4IRv8J/yduVy2gdNAlX5rpJqyrezBuSMNjwxPBV1wzNAo6ZIxBa1qSR
8ivbP1YeArfSsOs00sTgWKbryaeyTk53MxmEki5AtjzSA7+HEX+0Yx15uFFjFfnd
/BKleP+OCT2bF/xesa6OcBLemgltKysVV+W6D1Lj3aRDKjmU5zsVxKGB9BIsyPAn
SAEsKpXSMU/uA5CuDgjEVM1dJOLMw9fAPjtlZMpFtwdEvgzpG8bodwX+aZVWfUN+
qHcXD71L9ZnQwp610XmM/VG8SlvHvsyceBKJr/Ib+MlRBzmzEZCRjwjKjfsc7aUK
I5jcXz7yEqjJFA3Iq8vR3XPKdyD/AomonyivrO7mn/ibfcnEvJ/a9NO6tWxhETUq
QFs6xTSl9D2MMopjn5g07oXsZFAgheoEa7Ko3e502HZ4240dSGzkqx6EdPebWZSO
Fy32dE44dylu6NUHVuzTXPyuQLktmcvu6HLFnf1hM6uSZAQRt3QzknPUfHWamIkq
ZAI+0jwCxeFArXa9HVmP1BKS0MsL
-----END CERTIFICATE-----"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'ext_http', '{
    "submitTopicNameOrArn": "ext-http-submit",
    "delayQueueNameOrArn": "ext-http-delay-queue"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'learning_analytics', '{
    "adminBaseURL": "https://messaging-admin.stg-prsn.com",
    "appName": "gpd_bronte_system",
    "originatingSystemCode": "Bronte",
    "statusBaseURL": "https://messaging-status.stg-prsn.com",
    "publishBaseURL": "https://messaging-publishing.stg-prsn.com",
    "schemaRegistryBaseURL": "https://messaging-registry.prd-prsn.com",
    "autobahnEnvironment": "STG"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'csg', '{
    "indexUri": "https://dragonfly.stg-openclass.com/pxereader-cm/2.1/api/cm/documents",
    "applicationId": "bronte",
    "enabled": true,
    "batchSize": 20,
    "completionTime": 2500
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'plp_sqs', '{
    "queueName": "BRNT-PROD",
    "region": "us-east-2",
    "queueOwnerAWSAccountId": "815492460363",
    "enabled": "false"
}');

INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'courseware_export', '{
    "submitTopicNameOrArn": "courseware-element-to-ambrosia-submit",
    "resultQueueName": "courseware-element-to-ambrosia-result",
    "errorQueueName": "courseware-element-to-ambrosia-error",
    "delayQueueNameOrArn": "courseware-element-to-ambrosia-retry-delay",
    "bucketName": "workspace.us-east-1.bronte-stg.pearsondev.tech/export",
    "snippetBucketName": "ambrosia-snippet.us-east-1.bronte-stg.pearsondev.tech",
    "bucketUrl": "https://workspace-bronte-stg.pearson.com/export",
    "concurrentConsumers": 5,
    "maxMessagesPerPoll": 1,
    "submitExportFailureTopic": "epub-transform-publication-notification",
    "waitTimeSeconds": 10,
    "snippetsStorage": "S3"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'self_ingestion', '{
    "adapterEpubQueueNameOrArn": "ingestion-adapter-epub-submit",
    "adapterDocxQueueNameOrArn": "ingestion-adapter-docx-submit",
    "ambrosiaIngestionQueueNameOrArn": "ingestion-ambrosia-submit",
    "ingestionCancelQueueNameOrArn": "ingestion-cancel-submit",
    "bucketName": "ingestion-us-east-1-bronte-stg-pearsondev-tech",
    "bucketUrl": "https://ingestion-bronte-stg.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'plugin_log', '{
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
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'evaluation.feature', '{
    "configuredFeature": "REACTIVE_EVALUATION"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'oculus.url', '{
    "baseUrl": "https://paperapi.stg-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'evaluation.mode', '{
    "processingMode": "WAIT_PENDING"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'math', '{
    "mathMLUri": "https://www.wiris.net",
    "mathMLPath": "/demo/plugins/app/showimage",
    "metrics": true,
    "centerbaseline": false,
    "bucketName" : "bronte.us-east-1.bronte-stg.pearsondev.tech",
    "submitTopicNameOrArn": "math-asset-resolver-submit",
    "delayQueueNameOrArn": "math-asset-resolver-retry-delay",
    "enabled": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'grade_passback', '{
    "masteringGradeSyncUrl": "https://api.staging.mastering.pearsoncmg.com",
    "checkGradePassbackQuestionCount": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'publish_metadata', '{
    "masteringLabMetadataUrl": "https://bronteil.mx-dev.pearsondev.tech/bronteil-service/labMetadata"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'user_content', '{
    "cacheNameOrArn": "user-content-request-notification"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('STG', 'lti.credentials', '{
    "key": "Qa2ptmoNWTMcqlN8FgpGvk7PcVO6JBsQ",
    "secret": "RRRzJfQ9c6g_aWrRgkX9psKAKiwntFiCJ1k-5ItfRK0bOgv1"
}');