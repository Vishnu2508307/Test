resource "aws_elasticache_replication_group" "elasticache" {
  count                         = "${var.redis_with_elasticache == "1" ? 1:0}"
  replication_group_id          = "${var.elasticache_name}"
  replication_group_description = "${var.elasticache_name}"
  node_type                     = "${var.node_type}"
  engine                        = "${var.engine}"
  engine_version                = "${var.engine_version}"
  number_cache_clusters         = "${var.cache_nodes}"
  auth_token                    = "${data.aws_secretsmanager_secret_version.redis_password.secret_string}"
  parameter_group_name          = "${var.parameter_group_name}"
  port                          = "${var.port}"
  transit_encryption_enabled    = "${var.transit_encryption_enabled}"
  at_rest_encryption_enabled    = "${var.at_rest_encryption_enabled}"
  subnet_group_name             = "${aws_elasticache_subnet_group.elasticache.name}"
  security_group_ids            = ["${var.security_group_ids}"]
  automatic_failover_enabled    = "${var.auto_failover}"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_elasticache_subnet_group" "elasticache" {
  count       = "${var.redis_with_elasticache == "1" ? 1:0}"
  name        = "${var.elasticache_name}-subnet"
  description = "${var.project} project, ${var.elasticache_name}"
  subnet_ids  = ["${var.subnet_ids}"]
}
