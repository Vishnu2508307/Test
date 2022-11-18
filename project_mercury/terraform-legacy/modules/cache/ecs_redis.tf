data "aws_ecs_task_definition" "redis" {
  depends_on      = [ "aws_ecs_task_definition.redis" ]
  count           = "${var.redis_with_elasticache == "0" ? 1:0}"
  task_definition = "${aws_ecs_task_definition.redis.family}"
}

data "template_file" "redis" {
  template = "${file("${path.module}/task-definitions/redis.json")}"

  vars {
    ecs_cluster                      = "${var.deploy_to_cluster}"
    container_name                   = "${local.redis_container_name}"
    redis_password_secrets_reference = "${var.secrets_redis_password_name}"
    region                           = "${data.aws_region.current.name}"
    log_group                        = "${var.cloudwatch_log_group}"
    app_id                           = "${var.t_AppID}"
    cost_centre                      = "${var.t_cost_centre}"
    environment                      = "${var.t_environment}"
    dcl                              = "${var.t_dcl}"
  }
}

# EC2 based redis must use awsvpc network mode for service discovery
resource "aws_service_discovery_service" "service_discovery" {
  count = "${(var.redis_with_elasticache == "0" ? 1:0) * (var.aws_dns_namespace_id == "" ? 0:1) * (var.service_discovery_redis_dns_name == "" ? 0:1)}"
  name  = "${lower(var.service_discovery_redis_dns_name)}"
  dns_config {
    namespace_id = "${var.aws_dns_namespace_id}"
    dns_records {
      ttl  = 10
      type = "A"
    }
    routing_policy = "MULTIVALUE"
  }

  # Custom healthcheck must be added otherwise service discovery will timeout
  # https://github.com/terraform-providers/terraform-provider-aws/issues/4082
  health_check_custom_config {
    failure_threshold = 1
  }

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_ecs_task_definition" "redis" {
  count                    = "${var.redis_with_elasticache == "0" ? 1:0}"
  family                   = "${var.deploy_to_cluster}_${local.redis_container_name}"
  network_mode             = "${var.network_mode}"
  container_definitions    = "${data.template_file.redis.rendered}"
  requires_compatibilities = ["${var.launch_type}"]
  cpu                      = "${var.cpu}"
  memory                   = "${var.memory}"
  task_role_arn            = "${data.aws_iam_role.db.arn}"
  execution_role_arn       = "${data.aws_iam_role.db.arn}"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_ecs_service" "fargate_redis" {
  count           = "${(var.redis_with_elasticache == "0" ? 1:0) * (var.launch_type == "FARGATE" ? 1:0)  * (var.network_mode == "awsvpc" ? 1:0) * (var.aws_dns_namespace_id == "" ? 0:1) * (var.service_discovery_redis_dns_name == "" ? 0:1)}"
  name            = "${local.redis_container_name}"
  cluster         = "${var.deploy_to_cluster}"
  task_definition = "${aws_ecs_task_definition.redis.family}:${max("${aws_ecs_task_definition.redis.revision}", "${data.aws_ecs_task_definition.redis.revision}")}"
  launch_type     = "${var.launch_type}"
  desired_count   = "${var.cache_nodes}"

  deployment_maximum_percent         = "100"
  deployment_minimum_healthy_percent = "0"

  network_configuration {
    subnets         = ["${var.subnet_ids}"]
    security_groups = ["${var.security_group_ids}"]
  }

  service_registries {
    registry_arn   = "${aws_service_discovery_service.service_discovery.arn}"
    container_name = "${local.redis_container_name}"
  }

  tags {
    "Environment" = "${var.env}"
  }

  lifecycle { 
    create_before_destroy = true
  }

  #propagate_tags = "SERVICE"
  #tags {
  #  t_AppID       = "${var.t_AppID}"
  #  t_cost_centre = "${var.t_cost_centre}"
  #  t_environment = "${var.t_environment}"
  #  t_dcl         = "${var.t_dcl}"
  #}
}

resource "aws_ecs_service" "redis" {
  count           = "${(var.redis_with_elasticache == "0" ? 1:0) * (var.launch_type == "EC2" ? 1:0) * (var.network_mode != "awsvpc" ? 1:0) * (var.aws_dns_namespace_id == "" ? 0:1) * (var.service_discovery_redis_dns_name == "" ? 0:1)}"
  name            = "${local.redis_container_name}"
  cluster         = "${var.deploy_to_cluster}"
  task_definition = "${aws_ecs_task_definition.redis.family}:${max("${aws_ecs_task_definition.redis.revision}", "${data.aws_ecs_task_definition.redis.revision}")}"
  launch_type     = "${var.launch_type}"
  desired_count   = "${var.cache_nodes}"

  deployment_maximum_percent         = "100"
  deployment_minimum_healthy_percent = "0"

  lifecycle { 
    create_before_destroy = true
  }

  #propagate_tags = "SERVICE"
  #tags {
  #  t_AppID       = "${var.t_AppID}"
  #  t_cost_centre = "${var.t_cost_centre}"
  #  t_environment = "${var.t_environment}"
  #  t_dcl         = "${var.t_dcl}"
  #}
}

resource "aws_ecs_service" "redis_with_service_discovery" {
  count           = "${(var.redis_with_elasticache == "0" ? 1:0) * (var.launch_type == "EC2" ? 1:0) * (var.network_mode == "awsvpc" ? 1:0) * (var.aws_dns_namespace_id == "" ? 0:1) * (var.service_discovery_redis_dns_name == "" ? 0:1)}"
  name            = "${local.redis_container_name}"
  cluster         = "${var.deploy_to_cluster}"
  task_definition = "${aws_ecs_task_definition.redis.family}:${max("${aws_ecs_task_definition.redis.revision}", "${data.aws_ecs_task_definition.redis.revision}")}"
  launch_type     = "${var.launch_type}"
  desired_count   = "${var.cache_nodes}"

  deployment_maximum_percent         = "100"
  deployment_minimum_healthy_percent = "0"

  network_configuration {
    subnets         = ["${var.subnet_ids}"]
    security_groups = ["${var.security_group_ids}"]
  }

  service_registries {
    registry_arn   = "${aws_service_discovery_service.service_discovery.arn}"
    container_name = "${local.redis_container_name}"
  }

  lifecycle { 
    create_before_destroy = true
  }

  #propagate_tags = "SERVICE"
  #tags {
  #  t_AppID       = "${var.t_AppID}"
  #  t_cost_centre = "${var.t_cost_centre}"
  #  t_environment = "${var.t_environment}"
  #  t_dcl         = "${var.t_dcl}"
  #}
}
