variable "env" { type = "string" }
variable "project" { type = "string" }
variable "security_group_ids" { type = "list" } # description = "List of security group ids to assign the redis/elasticache"
variable "subnet_ids" { type  = "list" } # description = "List of subnet ids to spin up the redis/elasticache nodes"

variable "at_rest_encryption_enabled" { default = true }
variable "transit_encryption_enabled" { default = true } # description = "Encrypt traffic in transit"
variable "auto_failover" { default = false } # description = "Enables automatic failover"
variable "redis_with_elasticache" { default = true } # description = "Use elasticache if true else, use ecs"

variable "cache_nodes" { default = 1 } # description = "Number of nodes to deploy to elasticache replication group/ecs redis"
variable "port" { default = 6379 } # description = "Port number for elasticache/ecs redis"
variable "launch_type" { default = "FARGATE" } # description = "Specify the launch type when redis_with_elasticache=false"
variable "cpu" { default = "1024" } # description = "CPU to allocate a container. 1024 = 1 vCPU"
variable "memory" { default = "2048" } # description = "Maximum memory to allocate a container in MiBs. If this exceeded, the container is killed"
variable "network_mode" { default = "awsvpc" } # description = "awsvpc, host network mode for redis service (bridge is not supported)"

variable "aws_dns_namespace_id" { default = "" } # description = "DNS namespace id to have ECS perform service registration to the zone"
variable "iam_role_name" { default = "" } # description = "Iam role to assign the redis containers"
variable "service_discovery_redis_dns_name" { default = "" } # description = "DNS record to register any redis ecs node in the domain registered by the variable aws_dns_namespace_id"
variable "elasticache_name" { default = "" } # description = "Name of elasticache"
variable "engine" { default = "" } # description = "Engine type to use. Memcached or Redis"
variable "service_prefix_name" { default = "" } # description = "Optional name to preprend to the redis service. This has no impact when redis_with_elasticache=true"
variable "secrets_redis_password_name" { default = "" } # description = "Reference to the secrets name to set the password to access redis"
variable "cloudwatch_log_group" { default = "" } # description = "Version of redis to use for elasticache replication group"
variable "engine_version" { default = "" } # description = "Version of redis to use for elasticache replication group"
variable "node_type" { default = "" } # description = "Node type to use for elasticache replication group"
variable "parameter_group_name" { default = "" } # description = "Elasticache parameter group name"
variable "deploy_to_cluster" { default = "" } # description = "Only applicable when redis_with_elasticache is false. The ECS cluster to deploy the redis container"

variable "t_AppID" { type = "string" }
variable "t_cost_centre" { type = "string" }
variable "t_environment" { type = "string" }
variable "t_dcl" { type = "string" }
