resource "aws_appautoscaling_target" "ecs_target" {
  count              = "${(var.launch_type == "FARGATE" ? 1:0) * (var.enable_autoscaling)}"
  max_capacity       = "${var.autoscale_max_capacity}"
  min_capacity       = "${var.autoscale_min_capacity}"
  resource_id        = "service/${var.deploy_to_cluster}/${aws_ecs_service.fargate_mercury.name}"
  role_arn           = "${data.aws_iam_role.mercury.arn}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

resource "aws_appautoscaling_policy" "ecs_policy" {
  count              = "${(var.launch_type == "FARGATE" ? 1:0) * (var.enable_autoscaling)}"
  name               = "AdjustMercuryInstancesBasedOnCPU"
  policy_type        = "TargetTrackingScaling"
  resource_id        = "${aws_appautoscaling_target.ecs_target.resource_id}"
  scalable_dimension = "${aws_appautoscaling_target.ecs_target.scalable_dimension}"
  service_namespace  = "${aws_appautoscaling_target.ecs_target.service_namespace}"

  target_tracking_scaling_policy_configuration {
    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }

    target_value = "${var.autoscale_track_target_value}"
  }
}