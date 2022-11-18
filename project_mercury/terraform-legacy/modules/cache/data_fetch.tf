data "aws_region" "current" {}

data "aws_iam_role" "db" {
  count = "${var.redis_with_elasticache == "0" ? 1:0}"
  name = "${var.iam_role_name}"
}

data "aws_secretsmanager_secret_version" "redis_password" {
  secret_id = "${var.secrets_redis_password_name}"
}
