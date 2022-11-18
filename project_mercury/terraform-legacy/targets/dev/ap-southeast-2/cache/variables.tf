variable "secrets_redis_password_key" { type = "string" }
variable "dev_account_id" { type = "string" }
variable "eng_prod_account_id" { type = "string" }
variable "role_arn" { type = "map" }

variable "elasticache_instance_type" { default = "" }
variable "elasticache_auto_failover" { default = false }
variable "elasticache_cache_nodes" { default = 0 }
variable "backend_name" { default = "mercury" }

variable "t_AppID" { type = "string" }
variable "t_cost_centre" { type = "string" }
variable "t_environment" { type = "string" }
variable "t_dcl" { type = "string" }
