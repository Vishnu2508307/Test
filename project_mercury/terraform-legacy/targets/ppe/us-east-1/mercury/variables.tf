variable "desired_amount" { type = "string" }
variable "deploy_to_cluster" { type = "string" }
variable "secrets_cassandra_password_key" { type = "string" }
variable "secrets_cassandra_keystore_key" { type = "string" }
variable "env_region" { type = "string" }
variable "mercury_launch_type" { type = "string" }
variable "cassandra_user" { type = "string" }
variable "contact_points" { type = "string" }
variable "target_group_name" { type = "string" }
variable "deployment_minimum_healthy_percent" { type = "string" }
variable "deployment_maximum_percent" { type = "string" }
variable "enable_cloudwatch_alarms" { type = "string" }
variable "autoscale_min_capacity" { type = "string" }
variable "autoscale_max_capacity" { type = "string" }
variable "enable_autoscaling" { type = "string" }
variable "health_grace_period" {type = "string"}
variable "release_version" { type = "string" }
variable "apm_enabled" { type = "string" }
variable "apm_app_name" { type = "string" }
variable "role_arn" { type = "map" }
variable "ppe_account_id" { type = "string" }
variable "eng_prod_account_id" { type = "string" }
variable "secrets_cassandra_keystore_password_key" { type = "string" }

variable "mercury_cpu" { default = "" }
variable "mercury_memory" { default = "" }

variable "t_AppID" { type = "string" }
variable "t_cost_centre" { type = "string" }
variable "t_environment" { type = "string" }
variable "t_dcl" { type = "string" }
