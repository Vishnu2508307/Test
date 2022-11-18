-- The purpose of this file for now just to keep queries for configuration for later usage.
--
-- Inserting default configuration to config.env
--
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'plugin', '{
    "distribution.bucketName": "prod-aero-plugin.us-west-1.bronte.prd-prsn.com/distribution",
    "repository.bucketName": "prod-aero-plugin.us-west-1.bronte.prd-prsn.com/repository",
    "distribution.publicUrl": "https://plugin-distribution-bronte.pearson.com",
    "repository.publicUrl": "https://plugin-repository-bronte.pearson.com",
    "allowSync": false
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'schemas', '{
    "bucketName" : "prod-aero-plugin.us-west-1.bronte.prd-prsn.com",
    "prefix":"schemas"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'assets', '{
    "bucketName" : "prod-aero.us-west-1.bronte.prd-prsn.com",
    "prefix":"assets",
    "publicUrl":"https://assets-bronte.pearson.com",
    "submitTopicNameOrArn": "courseware-asset-resize-submit",
    "delayQueueNameOrArn": "courseware-asset-resize-retry-delay",
    "alfrescoUrl": "https://uswip.cms.pearson.com",
    "alfrescoPushDelayTime": 10
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'routeConsumers', '{
    "leanerEvaluateCompleteConsumers": 16,
    "learnerStudentScopeSetConsumers": 4,
    "leanerProgressBroadcastConsumers": 4,
    "competencyDocumentUpdateConsumers": 1
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'redis', '{
    "address":"rediss://master.prod-mercury.h1y2lg.usw1.cache.amazonaws.com:6379",
    "password":"vNhtKfkCC4fWmU4dR7keZVf562jzVana"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'operations_redis', '{
    "address":"rediss://master.prod-mercury.h1y2lg.usw1.cache.amazonaws.com:6379",
    "password":"vNhtKfkCC4fWmU4dR7keZVf562jzVana",
    "connectionPoolSize":64,
    "connectionMinimumIdleSize":10
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'cache', '{
    "enableLearnerCache": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'sso.oidc', '{
    "redirectUrl": "https://prod-aero.bronte.prd-prsn.com/sso/oidc/to",
    "callbackUrl": "https://prod-aero.bronte.prd-prsn.com/sso/oidc/callback"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'sso.ies', '{
    "baseUrl": "https://piapi-internal.openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'sso.mycloud', '{
    "baseUrl": "https://mycloud.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'sso.registrar', '{
    "baseUrl": "https://user-association.prd-prsn.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'passport', '{
    "baseUrl": "https://passport-api.openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'braintree', '{
    "environment": "production",
    "merchantId": "wtj3crj5pdhxwgmx",
    "publicKey": "qjc7frqyrv8ps8yc",
    "privateKey": "54d9234ceb9c88f3be52258a262efac2",
    "merchantAccountsByCurrency": {
        "AUD":"smartsparrowAUD",
        "USD":"SmartSparrowLLC_instant"
    }
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'system.credentials', '{
    "userId": "ffffffff5f298e8f52faff01da0d391a",
    "password": "Q6T2bMCPwhVBXLJkctqEAj58wxU8AXU2",
    "username": "gpd_bronte_system",
    "environment": "PRD"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'cassandra', '{
        "contactPoints":["10.82.39.8","10.82.39.30","10.82.39.175","10.82.39.154","10.82.39.124","10.82.39.70"],
        "username":"cassandra",
        "password":"cassandra",
        "localMaxRequestsPerConnection": 2048,
        "maxQueueSize": 512,
        "certificate":"-----BEGIN CERTIFICATE-----
MIIFsTCCA5mgAwIBAgIBATANBgkqhkiG9w0BAQsFADB6MQswCQYDVQQGEwJBVTEe
MBwGA1UECgwVSW5zdGFjbHVzdHIgUHR5LiBMdGQuMREwDwYDVQQLDAhDbHVzdGVy
czE4MDYGA1UEAwwvQ2x1c3RlciA1YzE4YzM1ZS1kZDU2LTRjNjUtOTlkOC05NTE2
NjE1Zjk3NTQgQ0EwHhcNMjEwMzE5MDEwMjM3WhcNMzEwMzE5MDAwMDAwWjB6MQsw
CQYDVQQGEwJBVTEeMBwGA1UECgwVSW5zdGFjbHVzdHIgUHR5LiBMdGQuMREwDwYD
VQQLDAhDbHVzdGVyczE4MDYGA1UEAwwvQ2x1c3RlciA1YzE4YzM1ZS1kZDU2LTRj
NjUtOTlkOC05NTE2NjE1Zjk3NTQgQ0EwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAw
ggIKAoICAQC5PRqo7rYgJQgCx9z6WfmiLfwNVcXyXbH3vYPDYPw83HMW49KwqVFa
HJO3rpLcen8J9dSHZIQCQv4pw7EoaInIlakXthb1jb2bWA8tcBFWoQGtGh0F80uD
x/UJVw4e1gG+j/njlYEtFQ6nAcvuzvtbou3f/i45KhnEiWbxe+Yi1Bs+fh2cuEet
dC/XcWe/44jgkw1xCJMO4dEks8wcv5r24SXC0ipgt+XMgFC1SF8kkRHI2Gy5d6Po
75YAZyCKlScYGTYr8cQDi+6YUSK84Ph9MiaRD4Zh7JQUIA/2045/TAGrMbOm9RYW
92F8oLXb1Q7MuqbwdeAWlU0T+JuzKigALig6JEBVNSwcU9C2BtCEuKaChTa2LJEm
qN9ITc998Dcg3hKBb+M0//qdxRKstRZLqtA5pjzDbMCdm+UjHkzKE//WRnAsa7Ia
V+LETtJoLsYM2pXL6bR7FSn3CkTnbTKLjSenoWuLjPb4+J6lihvdGRngQ2dTz9aL
ivonJZPlG5YXtaZQPeTMqjSQDJfY0fgR0e8pBB9VY1U31XAfAhItgrCpSuZ8gilT
JpPG/0YvJ8SKYd3LL4bKufjpGOX+XyJwe8JufM2oFAHO20pjpi3ZFZdVqS/A153G
ZkbToBbbxTvH7tyih0yzqLdPSBr2PSjGfzzVdjZMDeyF7B8dTMRQAQIDAQABo0Iw
QDAdBgNVHQ4EFgQUa978801I06wNKytJtJkSM9XFofYwDwYDVR0TAQH/BAUwAwEB
/zAOBgNVHQ8BAf8EBAMCAQYwDQYJKoZIhvcNAQELBQADggIBAIxkMO2ye5/cnoY4
Yy8B1DS2maHNO/o4UpLb7fXgfo3OGm7Ix84lzCIzR59Z4VV+fSW1C2zXUTnVjcQJ
UiJcnv7NUcySX8brOboUwif6B5NIQQYpSCrIg/rOL5aPZZkGWasvVi5OCvMCWq1A
i5w8Vt5On67RcWb46HKTBvXEYYlIwrVEu2JkLIKF2ybn/k2e9bebTtFqRDYaDNrM
GbRceJK0k8bSyU48kIbQhE19XdHbNOjIhp/1ixWInFB4iGex5krEejeVQxS5m4aN
RtxeMRMLifFWl/oe5DSZlLfGY6f0UR3JAqCjBrjyF9dxRA39Il5kNf/tWJbRwtUt
pmOZ4Ygqz+gVzr/iqyzR5E0QfAuOMhFMowFgblsqNg5pwU6HVp8btTkUElKLA96F
VFo608Uo0K9okh6hm9mS6JghUu596m2nhLtkqIwINyTeloJkFE/owqOJmlpFEsDF
2WyE4t6Efe1SdF1sNqwfvUc4RqXERLfV10jcs59FPYUwItsgXX0hI7egH1Ei9lSF
IgUOLsp6oOUSPuOSBeSrWuyCynJy7PG/uEK4mr1lw23WZD46CDcK3fmdJv/urMaq
MMva2Gpm2NpLHRohs9PkJyl3kBH6e2VzP4uwbl8o3GWA2TaBMgrdBVGDH2pg9T5I
2R73LwV61XA8zZIeZqGYfaIfP2VH
-----END CERTIFICATE-----"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'ext_http', '{
    "submitTopicNameOrArn": "ext-http-submit",
    "delayQueueNameOrArn": "ext-http-delay-queue"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'learning_analytics', '{
    "adminBaseURL": "https://messaging-admin.prd-prsn.com",
    "appName": "gpd_bronte_system",
    "originatingSystemCode": "Bronte",
    "statusBaseURL": "https://messaging-status.prd-prsn.com",
    "publishBaseURL": "https://messaging-publishing.prd-prsn.com",
    "schemaRegistryBaseURL": "https://messaging-registry.prd-prsn.com",
    "autobahnEnvironment": "PRD"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'csg', '{
    "indexUri": "https://dragonfly.openclass.com/pxereader-cm/2.1/api/cm/documents",
    "applicationId": "bronte",
    "enabled": true,
    "batchSize": 20,
    "completionTime": 2500
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'plp_sqs', '{
    "queueName": "BRNT-PROD",
    "region": "us-east-2",
    "queueOwnerAWSAccountId": "815492460363",
    "enabled": "false"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'courseware_export', '{
    "submitTopicNameOrArn": "courseware-element-to-ambrosia-submit",
    "resultQueueName": "courseware-element-to-ambrosia-result",
    "errorQueueName": "courseware-element-to-ambrosia-error",
    "delayQueueNameOrArn": "courseware-element-to-ambrosia-retry-delay",
    "bucketName": "workspace.us-west-1.bronte.prd-prsn.com/export",
    "snippetBucketName": "ambrosia-snippet.us-west-1.bronte.prd-prsn.com",
    "bucketUrl": "https://workspace-bronte.pearson.com/export",
    "concurrentConsumers": 10,
    "maxMessagesPerPoll": 1,
    "submitExportFailureTopic": "epub-transform-publication-notification",
    "waitTimeSeconds": 10,
    "snippetsStorage": "S3"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'self_ingestion', '{
    "adapterEpubQueueNameOrArn": "ingestion-adapter-epub-submit",
    "adapterDocxQueueNameOrArn": "ingestion-adapter-docx-submit",
    "ambrosiaIngestionQueueNameOrArn": "ingestion-ambrosia-submit",
    "ingestionCancelQueueNameOrArn": "ingestion-cancel-submit",
    "bucketName": "ingestion-us-west-1-bronte-pearsonprd-tech",
    "bucketUrl": "https://ingestion-bronte.pearson.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'plugin_log', '{
    "enabled": false,
    "bucketConfigs": [
      {
        "tableName":"generic_log_statement_by_plugin",
        "maxRecordCount":20000,
        "retentionPolicy":"WEEK",
        "logBucketInstances": 5
      },
      {
        "tableName":"workspace_log_statement_by_plugin",
        "maxRecordCount":20000,
        "retentionPolicy":"WEEK",
        "logBucketInstances": 5
      },
      {
        "tableName":"learnspace_log_statement_by_plugin",
        "maxRecordCount":20000,
        "retentionPolicy":"WEEK",
        "logBucketInstances": 5
      }
    ]
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'evaluation.feature', '{
    "configuredFeature": "REACTIVE_EVALUATION"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'oculus.url', '{
    "baseUrl": "https://paperapi.openclass.com"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'evaluation.mode', '{
    "processingMode": "WAIT_PENDING"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'math', '{
    "mathMLUri": "https://www.wiris.net",
    "mathMLPath": "/demo/plugins/app/showimage",
    "metrics": true,
    "centerbaseline": false,
    "bucketName" : "prod-aero.us-west-1.bronte.prd-prsn.com",
    "submitTopicNameOrArn": "math-asset-resolver-submit",
    "delayQueueNameOrArn": "math-asset-resolver-retry-delay",
    "enabled": true
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'grade_passback', '{
    "masteringGradeSyncUrl": "https://api.mastering.pearsoncmg.com",
    "checkGradePassbackQuestionCount": false
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'publish_metadata', '{
    "masteringLabMetadataUrl": "https://bronteil.mx.mx.pearsonprd.tech/bronteil-service/labMetadata"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'user_content', '{
    "cacheNameOrArn": "user-content-request-notification"
}');
INSERT INTO config.env (env_region, key, value) VALUES ('PROD_USW1', 'lti.credentials', '{
    "key": "ym5nhwAHFEr7fCtO5iyUK_FuucyhTBAu",
    "secret": "KS-VSzRkfxtMSJvInEs-qlpvrwZaNKkhCZA8KBKbuqdkPGwP"
}');