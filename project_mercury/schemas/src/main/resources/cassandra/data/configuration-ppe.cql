-- The purpose of this file for now just to keep queries for configuration for later usage.
--
-- Inserting default configuration to config.env
--
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'plugin', '{
    "distribution.bucketName": "plugin.us-east-1.bronte-ppe.pearsondev.tech/distribution",
    "repository.bucketName": "plugin.us-east-1.bronte-ppe.pearsondev.tech/repository",
    "repository.publicUrl": "https://plugin-repository-bronte-ppe.pearson.com",
    "distribution.publicUrl": "https://plugin-distribution-bronte-ppe.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'schemas', '{
    "bucketName" : "plugin.us-east-1.bronte-ppe.pearsondev.tech",
    "prefix":"schemas"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'assets', '{
    "bucketName" : "bronte.us-east-1.bronte-ppe.pearsondev.tech",
    "prefix":"assets",
    "publicUrl":"https://assets-bronte-ppe.pearson.com",
    "submitTopicNameOrArn": "courseware-asset-resize-submit",
    "delayQueueNameOrArn": "courseware-asset-resize-retry-delay",
    "alfrescoUrl": "https://usppewip.cms.pearson.com",
    "alfrescoPushDelayTime": 10
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'routeConsumers', '{
    "leanerEvaluateCompleteConsumers": 16,
    "learnerStudentScopeSetConsumers": 4,
    "leanerProgressBroadcastConsumers": 4,
    "competencyDocumentUpdateConsumers": 1
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'redis', '{
    "address":"rediss://master.ppe-mercury.l5qzse.use1.cache.amazonaws.com:6379",
    "password":"CUmymESfdKuSaTA8NEJUEPtwe395evsh"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'operations_redis', '{
    "address":"rediss://master.ppe-mercury.l5qzse.use1.cache.amazonaws.com:6379",
    "password":"CUmymESfdKuSaTA8NEJUEPtwe395evsh",
    "connectionPoolSize":64,
    "connectionMinimumIdleSize":10
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'cache', '{
    "enableLearnerCache": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'sso.oidc', '{
    "redirectUrl": "placeholder",
    "callbackUrl": placeholder""
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'sso.ies', '{
    "baseUrl": "https://pqa-piapi-internal.stg-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'sso.mycloud', '{
    "baseUrl": "https://mycloudtest.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'sso.registrar', '{
    "baseUrl": "https://user-association-qa.dev-prsn.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'passport', '{
    "baseUrl": "https://passport-perf.stg-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'braintree', '{
    "environment": "sandbox",
    "merchantId": "nnnz6ywxkthv7vk5",
    "publicKey": "92fvzc8t4955jqm5",
    "privateKey": "71a53ffbd700bb0795e47ba43f8c8e44",
    "merchantAccountsByCurrency": {
        "AUD":"smartSparrowAUD",
        "USD":"dj397dhnxzb4ww88"
    }
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'system.credentials', '{
    "userId": "ffffffff5eb5bcd446e0fb013090c73d",
    "password": "UT2KkY93epHHcq3MDkwhwUN5nvygzrkg",
    "username": "gpd_bronte_system",
    "environment": "PRF"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'cassandra', '{
        "contactPoints":["10.82.79.8","10.82.79.49","10.82.79.81","10.82.79.108","10.82.79.158","10.82.79.187"],
        "username":"cassandra",
        "password":"cassandra",
        "localMaxRequestsPerConnection": 2048,
        "maxQueueSize": 512,
        "certificate":"-----BEGIN CERTIFICATE-----
MIIFsTCCA5mgAwIBAgIBATANBgkqhkiG9w0BAQsFADB6MQswCQYDVQQGEwJBVTEe
MBwGA1UECgwVSW5zdGFjbHVzdHIgUHR5LiBMdGQuMREwDwYDVQQLDAhDbHVzdGVy
czE4MDYGA1UEAwwvQ2x1c3RlciBlNzhhNGNjMy0xYWE3LTRkMzMtYmU4MC1jNWIy
OGI0YTE5MDQgQ0EwHhcNMjEwMzA5MDQ1NjQ3WhcNMzEwMzA5MDAwMDAwWjB6MQsw
CQYDVQQGEwJBVTEeMBwGA1UECgwVSW5zdGFjbHVzdHIgUHR5LiBMdGQuMREwDwYD
VQQLDAhDbHVzdGVyczE4MDYGA1UEAwwvQ2x1c3RlciBlNzhhNGNjMy0xYWE3LTRk
MzMtYmU4MC1jNWIyOGI0YTE5MDQgQ0EwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAw
ggIKAoICAQCor3aGiBmPgDY+SEiZ41En8XFynHvXkYoCkJdH2TACaxBhU7TvnZit
Dal+StGEW0rdkVUkvUV2Y5VLPdYNBksRojVexOHpmoLJgTG/krKPeWp86E9nc1JY
PgF3q86OsHW1wmFCQmn6nJjcNMnhXKUjj83rpMHSDPnJE7Mn/gSB5LDvLXqcA9MJ
lXaeXkRLUShOcqysrjAGMkf4yoVPjzeNvLbXJTla3jRBK4TcxdrXStTB7QlClyyz
MbQv7QcKeyWjB7IB1lC9stm6Wcua002uUZaq8XUQt7psWH0bm0SfqGykI61X9Ve7
IXSkqOpy7zBVwWm1ML9D6N/iD9f5tDy4DBb5UaZredMgz8TkektxjXkq2QBpCMfu
yPmFw3gigEb//Tww3n+fK1iY/tK+rYFSRJ4qDVOwySbbIsgC+8kfJFakRqjWj3d6
O5DR6NVhZgGBUtWsEJkU+e1cLqhL+Cli24Jr3hMt64Its+e8rc6NagX6HlWpqs8U
a+kUl7QHs/9FIlTz/Qtz68khgwd3O/lIPK95ByvHXDBP8z3HKgeMF4NrhEKbccWC
RAvBsA/10nGgBIcRU36eAHc3Lyt0vHcncCBse09NXVdtqxbCjNyCn+nr8/NLn+rA
b9RLPWJLOk507xmvqDcuuXC+qJE5Spd2Yul3AurBnX6jjGRy3vbJUQIDAQABo0Iw
QDAdBgNVHQ4EFgQUAP3OhUdwt/WB1IpP8jquUQiJtGAwDwYDVR0TAQH/BAUwAwEB
/zAOBgNVHQ8BAf8EBAMCAQYwDQYJKoZIhvcNAQELBQADggIBAHjpvXbhlZLiWsN5
xi2Nh/2RRQ1mO2U83/QS6BLPqg5pIjnoBesCBtmn57oFsZNvfKuJk5ogUY/vvK9k
OJBBQstW37vMHVu65S+q5Zf4ReEamLkL8wIicF04wNDrl6rY/X/1O9TZ+PEReXPB
C9QsWbNjUr1GoCrpWTBNzNuG1L3jilifkC9Y6Dsl5I94nP6oC2sG/muize8mLAea
xy3VQSQOT1U5Wrb7ESuX4ZCfxvKJGn3Zumc1jULRZWBN8B9k7seM1heXuncUDSV0
jLRWjxHJoulcpDyV5dC+iY6tff3AEYS9EaidLRtGFoIckFGaYSdrAvcj8ZfC8oQp
azvw9LpIP5PZahIAIarodVCg5M130TiBUwck13En4MHYihNEeCTFfHs8Q0T2rtN0
EupBGQwrf4sz/BnJOjQV0eYUGNEtSOR/XDUnRArP90fOYYU9xmdKKFdt+HAul9pL
Ss9x+8spXB9Ken1O/ZNeHH8VuzmW+MBxmPViba80AJFQ/ZPRDEluH7UIVwCVmeJI
nvkclV11syCJ8tSbH/9AFnMa/q0rrMPk+VrTJ8V3zobBZmlXeYI+Bziw3JE0UDYi
ojPmKxJxLU1fS2fe9J2MbycZSyqRhqkM0NPxFLgdOPa0rd0oNHk36TeAyZuitQ8s
1FQogx5//a1GxkD7vjWlJWzIcpAa
-----END CERTIFICATE-----"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'ext_http', '{
    "submitTopicNameOrArn": "ext-http-submit",
    "delayQueueNameOrArn": "ext-http-delay-queue"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'learning_analytics', '{
    "adminBaseURL": "https://messaging-admin-prf.stg-prsn.com",
    "appName": "gpd_bronte_system",
    "originatingSystemCode": "Bronte",
    "statusBaseURL": "https://messaging-status-prf.stg-prsn.com",
    "publishBaseURL": "https://messaging-publishing-prf.stg-prsn.com",
    "schemaRegistryBaseURL": "https://messaging-registry.prd-prsn.com",
    "autobahnEnvironment": "PRF"
}');

INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'csg', '{
    "indexUri": "https://dragonfly-perf.stg-openclass.com/pxereader-cm/2.1/api/cm/documents",
    "applicationId": "bronte",
    "enabled": true,
    "batchSize": 20,
    "completionTime": 2500
}');

INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'plp_sqs', '{
    "queueName": "BRNT-PROD",
    "region": "us-east-2",
    "queueOwnerAWSAccountId": "815492460363",
    "enabled": "false"
}');

INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'courseware_export', '{
    "submitTopicNameOrArn": "courseware-element-to-ambrosia-submit",
    "resultQueueName": "courseware-element-to-ambrosia-result",
    "errorQueueName": "courseware-element-to-ambrosia-error",
    "delayQueueNameOrArn": "courseware-element-to-ambrosia-retry-delay",
    "bucketName": "workspace.us-east-1.bronte-ppe.pearsondev.tech/export",
    "snippetBucketName": "ambrosia-snippet.us-east-1.bronte-ppe.pearsondev.tech",
    "bucketUrl": "https://workspace-bronte-ppe.pearson.com/export",
    "concurrentConsumers": 10,
    "maxMessagesPerPoll": 1,
    "submitExportFailureTopic": "epub-transform-publication-notification",
    "waitTimeSeconds": 10,
    "snippetsStorage": "S3"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'self_ingestion', '{
    "adapterEpubQueueNameOrArn": "ingestion-adapter-epub-submit",
    "adapterDocxQueueNameOrArn": "ingestion-adapter-docx-submit",
    "ambrosiaIngestionQueueNameOrArn": "ingestion-ambrosia-submit",
    "ingestionCancelQueueNameOrArn": "ingestion-cancel-submit",
    "bucketName": "ingestion-us-east-1-bronte-ppe-pearsondev-tech",
    "bucketUrl": "https://ingestion-bronte-ppe.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'plugin_log', '{
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
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'evaluation.feature', '{
    "configuredFeature": "REACTIVE_EVALUATION"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'oculus.url', '{
    "baseUrl": "https://paperapi.stg-openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'evaluation.mode', '{
    "processingMode": "WAIT_PENDING"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'math', '{
    "mathMLUri": "https://www.wiris.net",
    "mathMLPath": "/demo/plugins/app/showimage",
    "metrics": true,
    "centerbaseline": false,
    "bucketName" : "bronte.us-east-1.bronte-ppe.pearsondev.tech",
    "submitTopicNameOrArn": "math-asset-resolver-submit",
    "delayQueueNameOrArn": "math-asset-resolver-retry-delay",
    "enabled": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'grade_passback', '{
    "masteringGradeSyncUrl": "https://api.ppe.mastering.pearsoncmg.com",
    "checkGradePassbackQuestionCount": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'publish_metadata', '{
    "masteringLabMetadataUrl": "https://bronteil.mx-stg.pearsondev.tech/bronteil-service/labMetadata"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'user_content', '{
    "cacheNameOrArn": "user-content-request-notification"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PPE', 'lti.credentials', '{
    "key": "lVSjM96RTYYh-ujFpF6qFeG9sewsR6rQ",
    "secret": "6if5z_CxLd4cenITr_9Sw6lr85CvEcLLSpBgAJzROJ-h8ISl"
}');