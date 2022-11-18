#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the ingestion coordinator lambda
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━

resource "aws_lambda_function" "ingestion_coordinator" {
  depends_on       = [
    "data.archive_file.lambda_ingestion_coordinator",
  ]
  filename         = "${data.archive_file.lambda_ingestion_coordinator.output_path}"
  source_code_hash = "${data.archive_file.lambda_ingestion_coordinator.output_base64sha256}"

  function_name = "ingestion-coordinator"
  role = "${aws_iam_role.ingestion_coordinator_execution_role.arn}"
  handler = "app/index.handler"
  runtime = "nodejs12.x"
  timeout = 90

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }

  environment {
    variables = {
      SUBNETS     = "${var.subnet_ids[0]}",
      LAUNCH_TYPE = "FARGATE",
      CLUSTER     = "${var.deploy_to_cluster}",
      ENVIRONMENT = "${var.env}",
      REGION      = "${var.aws_region}"
    }
  }
}

resource "aws_cloudwatch_log_group" "ingestion_coordinator" {
  name = "/aws/lambda/ingestion-coordinator"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# subscribe to the different submit queues
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━

resource "aws_lambda_event_source_mapping" "subscribe_ambrosia_to_sqs" {
  depends_on       = ["aws_lambda_function.ingestion_coordinator"]
  event_source_arn = "${aws_sqs_queue.ingestion_ambrosia_submit.arn}"
  enabled          = true
  function_name    = "${aws_lambda_function.ingestion_coordinator.arn}"
  batch_size       = 1
}

resource "aws_lambda_event_source_mapping" "subscribe_cancel_to_sqs" {
  depends_on       = ["aws_lambda_function.ingestion_coordinator"]
  event_source_arn = "${aws_sqs_queue.ingestion_cancel_submit.arn}"
  enabled          = true
  function_name    = "${aws_lambda_function.ingestion_coordinator.arn}"
  batch_size       = 1
}

resource "aws_lambda_event_source_mapping" "subscribe_adapter_epub_to_sqs" {
  depends_on       = ["aws_lambda_function.ingestion_coordinator"]
  event_source_arn = "${aws_sqs_queue.ingestion_adapter_epub_submit.arn}"
  enabled          = true
  function_name    = "${aws_lambda_function.ingestion_coordinator.arn}"
  batch_size       = 1
}

resource "aws_lambda_event_source_mapping" "subscribe_adapter_docx_to_sqs" {
  depends_on       = ["aws_lambda_function.ingestion_coordinator"]
  event_source_arn = "${aws_sqs_queue.ingestion_adapter_docx_submit.arn}"
  enabled          = true
  function_name    = "${aws_lambda_function.ingestion_coordinator.arn}"
  batch_size       = 1
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the IAM access
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━

resource "aws_iam_role" "ingestion_coordinator_execution_role" {
  name = "ingestion-ambrosia-lambda-execution-role"

  assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Principal": {
            "Service": [
                "lambda.amazonaws.com"
            ]
            },
            "Effect": "Allow",
            "Sid": ""
        }
    ]
}
EOF
}

resource "aws_iam_policy" "ingestion_coordinator_task_manager_permissions" {
  depends_on = ["data.archive_file.lambda_ingestion_coordinator"]
  name = "ingestion-coordinator-tasks-consume"
  path = "/"
  description = "Ability for lambda to consume SQS and manage tasks"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublishOutcome",
      "Action": [
        "ecs:List*",
        "ecs:Describe*",
        "ecs:UpdateService",
        "ecs:RunTask",
        "ecs:StopTask"
      ],
      "Effect": "Allow",
      "Resource": "*",
      "Condition":
      {
        "ArnEquals":
        {
          "ecs:cluster": "arn:aws:ecs:${var.aws_region}:${var.account_id}:cluster/${var.deploy_to_cluster}"
        }
      }
    },
    {
      "Sid": "ReceiveMessageFromQueue",
      "Action": [
        "sqs:SendMessage",
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:GetQueueAttributes"
      ],
      "Effect": "Allow",
      "Resource": [
        "${aws_sqs_queue.ingestion_ambrosia_submit.arn}",
        "${aws_sqs_queue.ingestion_cancel_submit.arn}",
        "${aws_sqs_queue.ingestion_adapter_epub_submit.arn}",
        "${aws_sqs_queue.ingestion_adapter_docx_submit.arn}"
      ]
    },
    {
        "Effect": "Allow",
        "Action": "iam:PassRole",
        "Resource": "arn:aws:iam::${var.account_id}:role/*"
    },
    {
        "Effect": "Allow",
        "Action":
        [
            "logs:CreateLogStream",
            "logs:PutLogEvents",
            "logs:CreateLogGroup"
        ],
        "Resource": "${aws_cloudwatch_log_group.ingestion_coordinator.arn}"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "ingestion_coordinator_policy_attachment" {
  role       = "${aws_iam_role.ingestion_coordinator_execution_role.name}"
  policy_arn = "${aws_iam_policy.ingestion_coordinator_task_manager_permissions.arn}"
}