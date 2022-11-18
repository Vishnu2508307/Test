data "aws_ecs_task_definition" "mercury" {
  depends_on      = [ "aws_ecs_task_definition.mercury" ]
  count           = "${var.desired_amount <= 0 ? 0:1}"
  task_definition = "${aws_ecs_task_definition.mercury.family}"
}

data "template_file" "mercury" {
  count    = "${var.desired_amount <= 0 ? 0:1}"
  template = "${file("${path.module}/task-definitions/application.json")}"

  vars {
    env                               = "${var.env}"
    ecs_cluster                       = "${var.deploy_to_cluster}"
    aws_region_logs                   = "${data.aws_region.current.name}"
    aws_log_group                     = "${var.cloudwatch_log_group}"
    container_port                    = "${var.port}"
    host_port                         = "${var.launch_type == "FARGATE" ? var.port:0}"
    container_name                    = "${local.mercury_container_name}"
    image                             = "${var.account_id}.dkr.ecr.${data.aws_region.current.name}.amazonaws.com/${var.ecr_repo}:${var.tag}"
    java_opts                         = "${var.java_opts}"
    env_region                        = "${upper(var.env_region)}"
    cassandra_contactpoints           = "${var.cassandra_contactpoints}"
    cassandra_authentication_username = "${var.cassandra_authentication_username}"
    cassandra_authentication_password = "${var.cassandra_authentication_password}"
    fetch_truststore_provider         = "${var.fetch_truststore_provider}"
    fetch_truststore_source_path      = "arn:aws:secretsmanager:${data.aws_region.current.name}:${var.account_id}:secret:${var.fetch_truststore_source_path}"
    cassandra_keystore_password       = "${var.cassandra_keystore_password}"
    apm_enabled                       = "${var.apm_enabled}"
    apm_app_name                      = "${var.apm_app_name}"
    account_id                        = "${var.account_id}"
    region                            = "${data.aws_region.current.name}"
    app_id                            = "${var.t_AppID}"
    cost_centre                       = "${var.t_cost_centre}"
    environment                       = "${var.t_environment}"
    dcl                               = "${var.t_dcl}"
  }
}

resource "aws_ecs_task_definition" "mercury" {
  count                    = "${var.desired_amount <= 0 ? 0:1}"
  family                   = "${var.deploy_to_cluster}_${local.mercury_container_name}"
  network_mode             = "${var.launch_type == "FARGATE" ? "awsvpc":"bridge"}"
  container_definitions    = "${data.template_file.mercury.rendered}"
  requires_compatibilities = ["${var.launch_type}"]
  cpu                      = "${var.cpu}"
  memory                   = "${var.memory}"
  task_role_arn            = "${data.aws_iam_role.mercury.arn}"
  execution_role_arn       = "${data.aws_iam_role.mercury.arn}"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_ecs_service" "fargate_mercury" {
  count           = "${(var.launch_type == "FARGATE" ? 1:0) * (var.desired_amount <= 0 ? 0:1)}"
  name            = "${local.mercury_container_name}"
  cluster         = "${var.deploy_to_cluster}"
  task_definition = "${aws_ecs_task_definition.mercury.family}:${max("${aws_ecs_task_definition.mercury.revision}", "${data.aws_ecs_task_definition.mercury.revision}")}"
  launch_type     = "${var.launch_type}"
  desired_count   = "${var.desired_amount}"

  deployment_maximum_percent         = "${var.deployment_maximum_percent}"
  deployment_minimum_healthy_percent = "${var.deployment_minimum_healthy_percent}"

  health_check_grace_period_seconds  = "${var.health_grace_period}"

  load_balancer {
    target_group_arn = "${data.aws_lb_target_group.mercury.arn}"
    container_name   = "${local.mercury_container_name}"
    container_port   = "${var.port}"
  }

  network_configuration {
    subnets         = ["${var.subnet_ids}"]
    security_groups = ["${var.security_group_ids}"]
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

resource "aws_ecs_service" "mercury" {
  count           = "${(var.launch_type == "EC2" ? 1:0) * (var.desired_amount <= 0 ? 0:1)}"
  name            = "${local.mercury_container_name}"
  cluster         = "${var.deploy_to_cluster}"
  task_definition = "${aws_ecs_task_definition.mercury.family}:${max("${aws_ecs_task_definition.mercury.revision}", "${data.aws_ecs_task_definition.mercury.revision}")}"
  launch_type     = "${var.launch_type}"
  iam_role        = "${aws_iam_role.ecs_service_role.arn}"
  desired_count   = "${var.desired_amount}"

  deployment_maximum_percent         = "${var.deployment_maximum_percent}"
  deployment_minimum_healthy_percent = "${var.deployment_minimum_healthy_percent}"

  health_check_grace_period_seconds  = "${var.health_grace_period}"

  load_balancer {
    target_group_arn = "${data.aws_lb_target_group.mercury.arn}"
    container_name   = "${local.mercury_container_name}"
    container_port   = "${var.port}"
  }

  ordered_placement_strategy {
    type  = "spread"
    field = "attribute:ecs.availability-zone"
  }

  placement_constraints {
    type       = "memberOf"
    expression = "attribute:smartsparrow.deploy == app"
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

resource "aws_cloudwatch_metric_alarm" "cpu_utilization_high" {
  count                     = "${var.enable_cloudwatch_alarms ? 1:0}"
  alarm_name                = "${var.deploy_to_cluster}-${local.mercury_container_name}-CPUUtilization-Above-${var.high_cpu_threshold}"
  comparison_operator       = "GreaterThanThreshold"
  evaluation_periods        = "2"
  metric_name               = "CPUUtilization"
  namespace                 = "AWS/ECS"
  period                    = "60"
  statistic                 = "Average"
  threshold                 = "${var.high_cpu_threshold}"
  alarm_description         = "${var.deploy_to_cluster}-${local.mercury_container_name} CPU Utilization is above ${var.high_cpu_threshold}%"
  alarm_actions             = ["${var.sns_alarm_arn}"]
  ok_actions                = ["${var.sns_alarm_arn}"]
  insufficient_data_actions = ["${var.sns_alarm_arn}"]

  dimensions                = {
    "ClusterName"           = "${var.deploy_to_cluster}"
    "ServiceName"           = "${local.mercury_container_name}"
  }
}

resource "aws_cloudwatch_metric_alarm" "memory_utilization_high" {
  count                     = "${var.enable_cloudwatch_alarms ? 1:0}"
  alarm_name                = "${var.deploy_to_cluster}-${local.mercury_container_name}-MemoryUtilization-Above-${var.high_memory_threshold}"
  comparison_operator       = "GreaterThanThreshold"
  evaluation_periods        = "2"
  metric_name               = "MemoryUtilization"
  namespace                 = "AWS/ECS"
  period                    = "60"
  statistic                 = "Average"
  threshold                 = "${var.high_memory_threshold}"
  alarm_description         = "${var.deploy_to_cluster}-${local.mercury_container_name} Memory Utilization is above ${var.high_memory_threshold}%"
  alarm_actions             = ["${var.sns_alarm_arn}"]
  ok_actions                = ["${var.sns_alarm_arn}"]
  insufficient_data_actions = ["${var.sns_alarm_arn}"]

  dimensions                = {
    "ClusterName"           = "${var.deploy_to_cluster}"
    "ServiceName"           = "${local.mercury_container_name}"
  }
}
