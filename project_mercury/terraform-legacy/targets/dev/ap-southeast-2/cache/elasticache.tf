module "redis" {
  source    = "../../../../modules/cache"
  providers = {
    "aws"   = "aws.ap_southeast_2"
  }
  project                     = "${var.PROJECT}"
  env                         = "${var.ENV}"
  elasticache_name            = "${var.ENV}-${var.backend_name}"
  engine                      = "redis"
  node_type                   = "${var.elasticache_instance_type}"
  parameter_group_name        = "default.redis3.2"
  engine_version              = "3.2.6"
  redis_with_elasticache      = true
  secrets_redis_password_name = "arn:aws:secretsmanager:ap-southeast-2:${var.dev_account_id}:secret:${var.secrets_redis_password_key}"
  cache_nodes                 = "${var.elasticache_cache_nodes}"
  auto_failover               = "${var.elasticache_auto_failover}"
  security_group_ids          = [
    "${lookup(data.terraform_remote_state.network.mercury_redis, "ap-southeast-2")}",
    "${lookup(data.terraform_remote_state.network.mercury_redis_from_spr_network, "ap-southeast-2")}"
  ]
  subnet_ids                  = [
    "${lookup(data.terraform_remote_state.network.mercury_redis_private_subnet, "ap-southeast-2a")}",
    "${lookup(data.terraform_remote_state.network.mercury_redis_private_subnet, "ap-southeast-2b")}"
  ]

  t_AppID       = "${var.t_AppID}"
  t_cost_centre = "${var.t_cost_centre}"
  t_environment = "${var.t_environment}"
  t_dcl         = "${var.t_dcl}"
}
