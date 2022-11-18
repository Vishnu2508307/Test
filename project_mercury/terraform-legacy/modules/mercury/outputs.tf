output "mercury_service_task_definition" {
  value = "${element(concat(aws_ecs_service.fargate_mercury.*.task_definition, aws_ecs_service.mercury.*.task_definition, list("")), 0)}"
}

output "mercury_service_name" {
  value = "${element(concat(aws_ecs_service.fargate_mercury.*.name, aws_ecs_service.mercury.*.name, list("")), 0)}"
}

output "mercury_service_arn" {
  value = "${element(concat(aws_ecs_service.fargate_mercury.*.id, aws_ecs_service.mercury.*.id, list("")), 0)}"
}
