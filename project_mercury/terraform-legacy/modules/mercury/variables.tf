variable "env" { type = "string" }
variable "tag" { type = "string" }
variable "project" { type = "string" }
variable "deploy_to_cluster" { type = "string" }
variable "cloudwatch_log_group" { type = "string" }
variable "attach_to_target_group" { type = "string" }
variable "account_id" { type = "string" }

variable "ecr_repo" { default = "services/mercury" }

variable "subnet_ids" { type = "list" }
variable "security_group_ids" { type = "list" }

variable "enable_autoscaling" { default = false }
variable "enable_cloudwatch_alarms" { default = false }

variable "cpu" { default = "" }
variable "memory" { default = "" }
variable "service_prefix_name" { default = "" }
variable "sns_alarm_arn" { default = [] }

variable "port" { default = 8080 }
variable "desired_amount" { default = 0 }
variable "health_grace_period" { default = 120 }

variable "deployment_maximum_percent" { default = "200" }
variable "deployment_minimum_healthy_percent" { default = "100" }
variable "launch_type" { default = "EC2" }
variable "high_cpu_threshold" { default = "85" }
variable "high_memory_threshold" { default = "85" }
variable "autoscale_max_capacity" { default = 1 }
variable "autoscale_min_capacity" { default = 1 }
variable "autoscale_track_target_value" { default = 30 } # Ensures the autoscaling stays as closely underneath the desired CPU as possible

# Below are the application system properties
variable "env_region" { type = "string" }
variable "cassandra_contactpoints" { type = "string" }
variable "cassandra_authentication_password" { type = "string" }
variable "fetch_truststore_source_path" { type = "string" }
variable "cassandra_keystore_password" { default = "" }
variable "apm_enabled" { default = "false" }
variable "apm_app_name" { default = "gpt-bronte" }
variable "fetch_truststore_provider" { default = "secretsmanager" }
variable "cassandra_authentication_username" { default = "cassandra" }
variable "java_opts" { default = "-XX:InitialRAMPercentage=70.0 -XX:MinRAMPercentage=70.0 -XX:MaxRAMPercentage=70.0" }

variable "t_AppID" { type = "string" }
variable "t_cost_centre" { type = "string" }
variable "t_environment" { type = "string" }
variable "t_dcl" { type = "string" }
