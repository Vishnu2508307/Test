locals {
  redis_container_name = "${var.service_prefix_name == "" ? "redis":"${var.service_prefix_name}-redis"}"
}
