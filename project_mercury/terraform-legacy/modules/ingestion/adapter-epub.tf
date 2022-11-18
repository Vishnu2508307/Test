data "template_file" "adapter_epub" {
  template = "${file("${path.module}/task-definitions/adapter-epub.json")}"

  vars {
    env                               = "${var.env}"
    ecs_cluster                       = "${var.deploy_to_cluster}"
    ecs_service_name                  = "adapter-epub"
    aws_region_logs                   = "${var.aws_region}"
    aws_log_group                     = "${aws_cloudwatch_log_group.adapter_epub.name}"
    container_port                    = "${var.port}"
    host_port                         = "${var.port}"
    container_name                    = "adapter-epub"
    image                             = "${var.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com/${var.epub_adapter_ecr_repo}:latest"
    account_id                        = "${var.account_id}"
    region                            = "${var.aws_region}"
    app_id                            = "${var.t_AppID}"
    cost_centre                       = "${var.t_cost_centre}"
    environment                       = "${var.t_environment}"
    dcl                               = "${var.t_dcl}"
    ingestion_bucket_name             = "${var.s3_workspace}"
    queue_name                        = "${aws_sqs_queue.ingestion_adapter_epub_submit.name}"
    result_topic                      = "${aws_sns_topic.adapter_result.arn}"
    event_topic                       = "${aws_sns_topic.ingestion_event_log_submit.arn}"
    error_topic                       = "${aws_sns_topic.adapter_error.arn}"
  }
}

resource "aws_ecs_task_definition" "adapter_epub" {
  depends_on = ["data.template_file.adapter_epub"]
  family                   = "adapter-epub"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  container_definitions    = "${data.template_file.adapter_epub.rendered}"
  cpu                      = "${var.cpu}"
  memory                   = "${var.memory}"
  execution_role_arn       = "${aws_iam_role.adapter_epub_execution_role.arn}"
  task_role_arn            = "${aws_iam_role.adapter_epub_execution_role.arn}"

  tags {
     t_AppID       = "${var.t_AppID}"
     t_cost_centre = "${var.t_cost_centre}"
     t_environment = "${var.t_environment}"
     t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_ecs_service" "adapter_epub" {
  name             = "adapter-epub"
  cluster          = "${var.deploy_to_cluster}"
  task_definition  = "${aws_ecs_task_definition.adapter_epub.arn}"
  desired_count    = 0
  launch_type      = "FARGATE"
  platform_version = "1.4.0"

  network_configuration {
    security_groups = ["${var.security_group_id}"]
    subnets         = ["${var.subnet_ids}"]
  }
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the ingestion-adapter-epub-submit SQS
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sqs_queue" "ingestion_adapter_epub_submit" {
  name = "ingestion-adapter-epub-submit"
  visibility_timeout_seconds = 120

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the cloudwatch log group
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_cloudwatch_log_group" "adapter_epub" {
  name = "/aws/ecs/adapter-epub"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the IAM access
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━

resource "aws_iam_role" "adapter_epub_execution_role" {
  name = "adapter-epub-execution-role"

  assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Principal": {
            "Service": [
                "ecs-tasks.amazonaws.com"
            ]
            },
            "Effect": "Allow",
            "Sid": ""
        }
    ]
}
EOF
  tags {
     t_AppID       = "${var.t_AppID}"
     t_cost_centre = "${var.t_cost_centre}"
     t_environment = "${var.t_environment}"
     t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_iam_policy" "adapter_epub_access_policy" {
  name = "adapter-epub-access-policy"

  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement":
    [
        {
            "Effect": "Allow",
            "Action":
            [
                "sqs:ChangeMessageVisibility",
                "sqs:ReceiveMessage",
                "sqs:DeleteMessage",
                "sqs:GetQueueUrl"
            ],
            "Resource": "${aws_sqs_queue.ingestion_adapter_epub_submit.arn}"
        },
        {
            "Effect": "Allow",
            "Action":
            [
                "logs:CreateLogStream",
                "logs:PutLogEvents",
                "logs:CreateLogGroup"
            ],
            "Resource": "${aws_cloudwatch_log_group.adapter_epub.arn}"
        },
        {
            "Effect": "Allow",
            "Action":
            [
                "application-autoscaling:Describe*",
                "application-autoscaling:PutScalingPolicy",
                "application-autoscaling:DeleteScalingPolicy",
                "application-autoscaling:RegisterScalableTarget",
                "application-autoscaling:DeregisterScalableTarget",
                "ecs:List*",
                "ecs:Describe*",
                "ecs:UpdateService"
            ],
            "Resource": "arn:aws:ecs:${var.aws_region}:${var.account_id}:service/${var.deploy_to_cluster}/${aws_ecs_service.adapter_epub.name}"
        }
    ]
}
EOF
}
resource "aws_iam_role_policy_attachment" "epub_adapter_ingestion_inline_policy_attachment" {
  depends_on = ["aws_iam_policy.ingestion_access_policy"]
  role       = "${aws_iam_role.adapter_epub_execution_role.name}"
  policy_arn = "${aws_iam_policy.ingestion_access_policy.arn}"
}

resource "aws_iam_role_policy_attachment" "epub_adapter_inline_policy_attachment" {
  role       = "${aws_iam_role.adapter_epub_execution_role.name}"
  policy_arn = "${aws_iam_policy.adapter_epub_access_policy.arn}"
}

resource "aws_iam_role_policy_attachment" "epub_adapter_execution_role_policy_attachment" {
  role       = "${aws_iam_role.adapter_epub_execution_role.name}"
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}
