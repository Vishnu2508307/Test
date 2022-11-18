output "redis_service_task_definition" {
  value = "${element(concat(aws_ecs_service.fargate_redis.*.task_definition, aws_ecs_service.redis.*.task_definition, aws_ecs_service.redis_with_service_discovery.*.task_definition, list("")), 0)}"
}

output "redis_service_name" {
  value = "${element(concat(aws_ecs_service.fargate_redis.*.name, aws_ecs_service.redis.*.name, aws_ecs_service.redis_with_service_discovery.*.name, list("")), 0)}"
}

output "redis_service_arn" {
  value = "${element(concat(aws_ecs_service.fargate_redis.*.id, aws_ecs_service.redis.*.id, aws_ecs_service.redis_with_service_discovery.*.id, list("")), 0)}"
}
