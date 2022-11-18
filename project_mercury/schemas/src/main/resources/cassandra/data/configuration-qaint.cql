-- The purpose of this file for now just to keep queries for configuration for later usage.
--
-- Inserting default configuration to config.env
--
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'plugin', '{
    "distribution.bucketName": "plugin.us-east-1.bronte-qaint.pearsondev.tech/distribution",
    "repository.bucketName": "plugin.us-east-1.bronte-qaint.pearsondev.tech/repository",
    "repository.publicUrl": "https://plugin-repository-bronte-qaint.pearson.com",
    "distribution.publicUrl": "https://plugin-distribution-bronte-qaint.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'schemas', '{
    "bucketName" : "plugin.us-east-1.bronte-qaint.pearsondev.tech",
    "prefix":"schemas"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'assets', '{
    "bucketName" : "bronte.us-east-1.bronte-qaint.pearsondev.tech",
    "prefix":"assets",
    "publicUrl":"https://assets-bronte-qaint.pearson.com",
    "submitTopicNameOrArn": "courseware-asset-resize-submit",
    "delayQueueNameOrArn": "courseware-asset-resize-retry-delay",
    "alfrescoUrl": "https://usppewip.cms.pearson.com",
    "alfrescoPushDelayTime": 10
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'routeConsumers', '{
    "leanerEvaluateCompleteConsumers": 16,
    "learnerStudentScopeSetConsumers": 4,
    "leanerProgressBroadcastConsumers": 4,
    "competencyDocumentUpdateConsumers": 1
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'redis', '{
    "address":"rediss://master.qaint-mercury.z0uns1.use1.cache.amazonaws.com:6379",
    "password":"TvtH4zyhBhXQkZ2L5jQAgKSJXyLDvHDs"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'operations_redis', '{
    "address":"rediss://master.qaint-mercury.z0uns1.use1.cache.amazonaws.com:6379",
    "password":"TvtH4zyhBhXQkZ2L5jQAgKSJXyLDvHDs",
    "connectionPoolSize":64,
    "connectionMinimumIdleSize":10
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'cache', '{
    "enableLearnerCache": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'sso.oidc', '{
    "redirectUrl": "placeholder",
    "callbackUrl": placeholder""
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'sso.ies', '{
    "baseUrl": "https://tst-piapi-internal.dev-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'sso.mycloud', '{
    "baseUrl": "https://mycloudtest.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'sso.registrar', '{
    "baseUrl": "https://user-association-qa.dev-prsn.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'passport', '{
    "baseUrl": "https://passport-qa.dev-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'braintree', '{
    "environment": "sandbox",
    "merchantId": "nnnz6ywxkthv7vk5",
    "publicKey": "92fvzc8t4955jqm5",
    "privateKey": "71a53ffbd700bb0795e47ba43f8c8e44",
    "merchantAccountsByCurrency": {
        "AUD":"smartSparrowAUD",
        "USD":"dj397dhnxzb4ww88"
    }
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'system.credentials', '{
    "userId": "ffffffff5eb5bc8f5d2f5b01312aa65e",
    "password": "ZbY58cH2gdbxFgsCnyVvyWdJNduRLgne",
    "username": "gpd_bronte_system",
    "environment": "INT"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'cassandra', '{
        "contactPoints":["10.82.78.12","10.82.78.92","10.82.78.163"],
        "username":"cassandra",
        "password":"cassandra",
        "localMaxRequestsPerConnection": 2048,
        "maxQueueSize": 512,
        "certificate":"-----BEGIN CERTIFICATE-----
MIIFsTCCA5mgAwIBAgIBATANBgkqhkiG9w0BAQsFADB6MQswCQYDVQQGEwJBVTEe
MBwGA1UECgwVSW5zdGFjbHVzdHIgUHR5LiBMdGQuMREwDwYDVQQLDAhDbHVzdGVy
czE4MDYGA1UEAwwvQ2x1c3RlciAwMTI4YTVlYi0wMzcyLTQ0NTQtYTcxNS0zMjE4
ZjYyZDUxOGEgQ0EwHhcNMjEwMzE4MDIyMTIwWhcNMzEwMzE4MDAwMDAwWjB6MQsw
CQYDVQQGEwJBVTEeMBwGA1UECgwVSW5zdGFjbHVzdHIgUHR5LiBMdGQuMREwDwYD
VQQLDAhDbHVzdGVyczE4MDYGA1UEAwwvQ2x1c3RlciAwMTI4YTVlYi0wMzcyLTQ0
NTQtYTcxNS0zMjE4ZjYyZDUxOGEgQ0EwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAw
ggIKAoICAQC+V+m34lERo0Rz3yyg+/YpWYCrs55XM7MSnaV2JjXrK/DojNzEElA4
Z2hnW0TXAmZA8IwIzbvlEE7xN2gnnFY0HiC4WZiivQ7g9xor/xZAY8pYdT0Xbt75
cbT69WyfbYfbqi/r/wtr7jEFyHAB9xFIn2MrLG5L6A8jfonQ7C82qoo5aNq/8rMJ
x/67dwlFdf+6qmrKOeOYt/xlUTIzXHWRRx5hDzBYACm7xMXdunXFoKtM6+lhJuVa
TW9eNPDxLJWzVWo+olYODcbClh5FKsU965NAExVKbbrZSgk/U0cyfRqNIDGd8nGx
73mX9NJKupk3qTp35c5LRngoEJbAS3A9zRbBq2jKOWf2h8f/EqFImaHEmrQNSozx
yZ3Hm5eSkfAHAteseP0jFxnr0rEO9wl7r5QorrLhFnqulosBqm8Y74tVEqqqeU+U
NuV7OdldUVpA7A0nNpZum599n2lhUXovY6mZStZCFuZc5eeotPmKck17Z1ASIsa6
T6BnkD+wXrNW2ao0avgoHG9I2/WvoQUC1Mmd1wRnMqN0zETVsljW3x8L5XrKxTpl
7u/Wp4UHamB0Z40sT9EqirmLlonyFWzR1cKCuuSt/E4lsTp0qagZcs5cQrc59pZ8
tRBVMQ1NML6L+XCf2MV3/ldOD+Sypq+NaBd2zCPNmh5xdgHQcXk+qwIDAQABo0Iw
QDAdBgNVHQ4EFgQUkzu/ePufPWOCRmBnjfp3jfxvueMwDwYDVR0TAQH/BAUwAwEB
/zAOBgNVHQ8BAf8EBAMCAQYwDQYJKoZIhvcNAQELBQADggIBAEzYyUgXxNk0f8PR
viB2HBh+mYPAPT3tQmo9+TaubhjdJT7j/hybBxivjNAevV5JGgraOY4HpznFgDC1
siFYwabc916jklM7OMeJpBlkH+h2yv1qVABfdBkeeK0aeckUFaTUtJJ0z2KW0EGy
HdOsM+aV5bN3urJXXLbuyVC29oy7YVrtF+CUMYjRPpjSoUOe/ojKZEfc80/mG2nk
+mf/4GO37FIT1pk/y7ANQiFLRcQn5T4vCkfjwgmRnHOCeKtDGCwFyIfH/MJrazJ6
GgHjB5xCrxuMAlOYl70iX+gWRmwFr/cpStQzM/1LGOckCctOVkExSWB+yX51TDOt
cr3HNPfdDL38k0dxKVdCeX1V9IC6OEDuGsvUkHbbGljuRoHjgEnTwVpX8Aa9WO5Q
i8adeVoIYECiwo/AMsxQD0m/yc9h0ZWSdedQ85D6rMWjHd9cs8TqanLliu+KJXah
XF+DalqQx/Qb7XyZZSJxhvUuOkAw9xQm1dh6lQvCIJvowsr9Wu4J1FTot6u4Wd05
yGQmLwe4Vlb9Tzd6Ol/rBkUBfc+GgqlYu8ajjnUmgKYiqd5CcQW7iRdV+AeJ2NXB
XPzUWcBzYeCvOw4zqLHrGMbhOsHM5kqHVml+bnaoVAWscO0cvfD7ReCF7w3fFoZ9
45UuZRKifkEWVHmZlsoHGYc0s/1L
-----END CERTIFICATE-----"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'ext_http', '{
    "submitTopicNameOrArn": "ext-http-submit",
    "delayQueueNameOrArn": "ext-http-delay-queue"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'learning_analytics', '{
    "adminBaseURL": "https://messaging-admin-int.dev-prsn.com",
    "appName": "gpd_bronte_system",
    "originatingSystemCode": "Bronte",
    "statusBaseURL": "https://messaging-status-int.dev-prsn.com",
    "publishBaseURL": "https://messaging-publishing-int.dev-prsn.com",
    "schemaRegistryBaseURL": "https://messaging-registry.prd-prsn.com",
    "autobahnEnvironment": "INT"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'csg', '{
    "indexUri": "https://dragonfly-qa.stg-openclass.com/pxereader-cm/2.1/api/cm/documents",
    "applicationId": "bronte",
    "enabled": true,
    "batchSize": 20,
    "completionTime": 2500
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'plp_sqs', '{
    "queueName": "BRNT-QAINT",
    "region": "us-east-2",
    "queueOwnerAWSAccountId": "815492460363",
    "enabled": "false"
}');

INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'courseware_export', '{
    "submitTopicNameOrArn": "courseware-element-to-ambrosia-submit",
    "resultQueueName": "courseware-element-to-ambrosia-result",
    "errorQueueName": "courseware-element-to-ambrosia-error",
    "delayQueueNameOrArn": "courseware-element-to-ambrosia-retry-delay",
    "bucketName": "workspace.us-east-1.bronte-qaint.pearsondev.tech/export",
    "snippetBucketName": "ambrosia-snippet.us-east-1.bronte-qaint.pearsondev.tech",
    "bucketUrl": "https://workspace-bronte-qaint.pearson.com/export",
    "concurrentConsumers": 5,
    "maxMessagesPerPoll": 1,
    "submitExportFailureTopic": "epub-transform-publication-notification",
    "waitTimeSeconds": 10,
    "snippetsStorage": "S3"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'self_ingestion', '{
    "adapterEpubQueueNameOrArn": "ingestion-adapter-epub-submit",
    "adapterDocxQueueNameOrArn": "ingestion-adapter-docx-submit",
    "ambrosiaIngestionQueueNameOrArn": "ingestion-ambrosia-submit",
    "ingestionCancelQueueNameOrArn": "ingestion-cancel-submit",
    "bucketName": "ingestion-us-east-1-bronte-qaint-pearsondev-tech",
    "bucketUrl": "https://ingestion-bronte-qaint.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'plugin_log', '{
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
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'evaluation.feature', '{
    "configuredFeature": "REACTIVE_EVALUATION"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'oculus.url', '{
    "baseUrl": "https://paperapi.stg-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'evaluation.mode', '{
    "processingMode": "WAIT_PENDING"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'math', '{
    "mathMLUri": "https://www.wiris.net",
    "mathMLPath": "/demo/plugins/app/showimage",
    "metrics": true,
    "centerbaseline": false,
    "bucketName" : "bronte.us-east-1.bronte-qaint.pearsondev.tech",
    "submitTopicNameOrArn": "math-asset-resolver-submit",
    "delayQueueNameOrArn": "math-asset-resolver-retry-delay",
    "enabled": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'grade_passback', '{
    "masteringGradeSyncUrl": "https://api.staging.mastering.pearsoncmg.com",
    "checkGradePassbackQuestionCount": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'publish_metadata', '{
    "masteringLabMetadataUrl": "https://bronteil.mx-dev.pearsondev.tech/bronteil-service/labMetadata"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'user_content', '{
    "cacheNameOrArn": "user-content-request-notification"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('QAINT', 'lti.credentials', '{
    "key": "Qa2ptmoNWTMcqlN8FgpGvk7PcVO6JBsQ",
    "secret": "RRRzJfQ9c6g_aWrRgkX9psKAKiwntFiCJ1k-5ItfRK0bOgv1"
}');