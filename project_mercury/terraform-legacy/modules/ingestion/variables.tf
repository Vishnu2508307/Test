#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# this is the endpoint to deliver sns to, without a trailing slash.
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
variable "base_endpoint" { type = "string" }
variable "env" { type = "string" }
variable "aws_region" { type = "string" }
variable "project" { type = "string" }
variable "account_id" { type = "string" }
variable "sns_alarm_arn" { type = "string" }
variable "deploy_to_cluster" { type = "string" }
variable "launch_type" { type = "string" }
variable "desired_amount" { type = "string" }
variable "memory" { type = "string" }
variable "cpu" { type = "string" }
variable "subnet_ids" { type = "list" }
variable "security_group_id" { type = "string" }
variable "s3_workspace" { type = "string" }
variable "port" { type = "string" }
variable "autoscale_max_capacity" { default = 20 }
variable "main_revision" { type = "string" }

# release version?
variable "tag" { type = "string" }

variable "ingestion_ambrosia_ecr_repo" { default = "ingestion/ambrosia" }
variable "epub_adapter_ecr_repo" { default = "ingestion/adapter/epub" }
variable "docx_adapter_ecr_repo" { default = "ingestion/adapter/docx" }

variable "t_AppID" { type = "string" }
variable "t_cost_centre" { type = "string" }
variable "t_environment" { type = "string" }
variable "t_dcl" { type = "string" }
