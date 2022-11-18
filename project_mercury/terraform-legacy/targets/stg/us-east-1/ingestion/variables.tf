variable "role_arn" { type = "map" }
variable "mercury_endpoints" { type = "map" }

variable "stg_account_id" { type = "string" }
variable "release_version" { type = "string" }
variable "ingestion_launch_type" { type = "string" }
variable "ingestion_cpu" { default = "" }
variable "ingestion_memory" { default = "" }
variable "ingestion_desired_amount" { type = "string" }
variable "ingestion_port" { type = "string" }
variable "ingestion_autoscale_max_capacity" { type = "string" }
variable "s3_buckets" { type = "map" }

variable "t_AppID" { type = "string" }
variable "t_cost_centre" { type = "string" }
variable "t_environment" { type = "string" }
variable "t_dcl" { type = "string" }
