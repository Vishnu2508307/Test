t_AppID       = "SVC02507"
t_cost_centre = "9516.9130.226301.773000.00000.0000.0000.0000"
t_environment = "PRF" # valid values are DEV, QA, PRF, STG, PRD
t_dcl         = "4"

################ Backend ################

cassandra_datacenters      = {
  us-east-1      = "us-east-1"
}

## Mercury
deploy_to_cluster                       = "ppe-aero"
target_group_name                       = "ppe-mercury"
mercury_launch_type                     = "FARGATE"

desired_amount                          = 0

autoscale_min_capacity                  = 0
autoscale_max_capacity                  = 0
enable_autoscaling                      = false
health_grace_period                     = 180

enable_cloudwatch_alarms                = true

env_region                              = "PPE"
mercury_cpu                             = "4096"
mercury_memory                          = "8192"
contact_points                          = "10.82.79.8,10.82.79.49,10.82.79.81,10.82.79.108,10.82.79.158,10.82.79.187"
deployment_minimum_healthy_percent      = "100"
deployment_maximum_percent              = "200"

cassandra_user                          = "cassandra"
secrets_cassandra_keystore_key          = "/ppe/aero/mercury/cassandra/truststore.jks"
secrets_cassandra_password_key          = "/ppe/aero/mercury/cassandra/password"
secrets_cassandra_keystore_password_key = "/ppe/aero/mercury/cassandra/truststore_password"
apm_enabled                             = "true"
apm_app_name                            = "gpt-bronte-ppe"
apm_app_account_id                      = "693300"

## Elasticache/Redis
secrets_redis_password_key              = "/ppe/mercury/redis/auth_password"
elasticache_instance_type               = "cache.m4.large"
elasticache_cache_nodes                 = 2
elasticache_auto_failover               = true

## Ingestion
ingestion_launch_type                     = "FARGATE"
ingestion_port                            = 80
ingestion_cpu                             = "4096"
ingestion_memory                          = "8192"
ingestion_desired_amount                  = 0
ingestion_autoscale_max_capacity          = 20