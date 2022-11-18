#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the asset-submit topic & subscriptions
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sns_topic" "courseware-asset-resize-submit" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "courseware-asset-resize-submit"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "submit-to-courseware-asset-resize-lambda" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  topic_arn = "${aws_sns_topic.courseware-asset-resize-submit.arn}"
  protocol = "lambda"
  endpoint = "${aws_lambda_function.courseware-asset-resize-processor.arn}"
}

resource "aws_lambda_permission" "with_sns" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  statement_id = "AllowExecutionFromSNS"
  action = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.courseware-asset-resize-processor.arn}"
  principal = "sns.amazonaws.com"
  source_arn = "${aws_sns_topic.courseware-asset-resize-submit.arn}"
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the lambda result topic
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sns_topic" "courseware-asset-resize-result" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "courseware-asset-resize-result"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "courseware-asset-resize-result-subscription" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  topic_arn = "${aws_sns_topic.courseware-asset-resize-result.arn}"
  protocol = "https"
  endpoint = "https://${var.base_endpoint}/sns/courseware-asset-resize/result"
  endpoint_auto_confirms = true
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the lambda error topic
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sns_topic" "courseware-asset-resize-error" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "courseware-asset-resize-error"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "courseware-asset-resize-error-subscription" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  topic_arn = "${aws_sns_topic.courseware-asset-resize-error.arn}"
  protocol = "https"
  endpoint = "https://${var.base_endpoint}/sns/courseware-asset-resize/error"
  endpoint_auto_confirms = true
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the lambda retry topic
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sns_topic" "courseware-asset-resize-retry" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "courseware-asset-resize-retry"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "courseware-asset-resize-retry-subscription" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  topic_arn = "${aws_sns_topic.courseware-asset-resize-retry.arn}"
  protocol = "https"
  endpoint = "https://${var.base_endpoint}/sns/courseware-asset-resize/retry"
  endpoint_auto_confirms = true
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the sqs queue for delayed retries
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sqs_queue" "courseware-asset-resize-retry-delay" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "courseware-asset-resize-retry-delay"
  receive_wait_time_seconds = 20
  # https://docs.aws.amazon.com/lambda/latest/dg/dlq.html
  # > If you are using Amazon SQS as an event source, configure a DLQ on the Amazon SQS queue itself and not the Lambda function.
  redrive_policy = "{\"deadLetterTargetArn\":\"${aws_sqs_queue.courseware-asset-resize-retry-dead-letters.arn}\",\"maxReceiveCount\":4}"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_lambda_event_source_mapping" "alesm1" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  event_source_arn = "${aws_sqs_queue.courseware-asset-resize-retry-delay.arn}"
  function_name = "${aws_lambda_function.courseware-asset-resize-processor.arn}"
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the request processing lambda
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_lambda_function" "courseware-asset-resize-processor" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  filename         = "${data.archive_file.lambda_image-manipulator_processor.output_path}"
  source_code_hash = "${data.archive_file.lambda_image-manipulator_processor.output_base64sha256}"

  function_name = "courseware-asset-resize-processor"
  role = "${aws_iam_role.iam_for_lambda.arn}"
  handler = "index.handler"
  runtime = "nodejs12.x"
  timeout = 30

  dead_letter_config {
    target_arn = "${aws_sns_topic.courseware-asset-resize-processor-dead-letters.arn}"
  }

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }

  environment {
    variables = {
      TOPIC_ARN_COURSEWARE_ASSET_RESIZE_ERROR = "${aws_sns_topic.courseware-asset-resize-error.arn}",
      TOPIC_ARN_COURSEWARE_ASSET_RESIZE_RESULT = "${aws_sns_topic.courseware-asset-resize-result.arn}",
      DEBUG_LOG = "1"
    }
  }
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the retry (sqs-to-sns) processing lambda
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_lambda_function" "courseware-asset-resize-retry-processor" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  filename         = "${data.archive_file.lambda_sqs_to_sns.output_path}"
  source_code_hash = "${data.archive_file.lambda_sqs_to_sns.output_base64sha256}"

  function_name = "courseware-asset-resize-sqs-to-sns"
  role = "${aws_iam_role.iam_for_lambda.arn}"
  handler = "index.handler"
  runtime = "nodejs12.x"
  timeout = 30

  # the dead letter is configured on the SQS level for this one.

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
  environment {
    variables = {
      TOPIC_ARN = "${aws_sns_topic.courseware-asset-resize-retry-dead-letters.arn}",
      DEBUG_LOG = "1"
    }
  }
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the lambda processing assume role
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_iam_role" "iam_for_lambda" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "iam_for_lambda"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
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

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# allow the lambdas to write to do things.
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_iam_policy" "courseware-asset-resize-lambda-publish-consume" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "courseware-asset-resize-lambda-publish-consume"
  path = "/"
  description = "Ability for lambda to publish-consume results"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublishOutcome",
      "Action": [
        "sns:Publish"
      ],
      "Effect": "Allow",
      "Resource": [
        "${aws_sns_topic.courseware-asset-resize-error.arn}",
        "${aws_sns_topic.courseware-asset-resize-result.arn}",
        "${aws_sns_topic.courseware-asset-resize-retry.arn}",
        "${aws_sns_topic.courseware-asset-resize-processor-dead-letters.arn}",
        "${aws_sns_topic.courseware-asset-resize-retry-dead-letters.arn}"
      ]
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
        "${aws_sqs_queue.courseware-asset-resize-retry-delay.arn}",
        "${aws_sqs_queue.courseware-asset-resize-processor-dead-letters.arn}",
        "${aws_sqs_queue.courseware-asset-resize-retry-dead-letters.arn}"
      ]
    },
    {
      "Sid": "ReadWriteS3",
      "Action": [
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Effect": "Allow",
      "Resource": ["*"]
    }
  ]
}
EOF
}
# If we know specific bucket, it'd be good to add above instead of a wildcard

resource "aws_iam_role_policy_attachment" "courseware-asset-resize-lambda-publish-consume-policy" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  role = "${aws_iam_role.iam_for_lambda.name}"
  policy_arn = "${aws_iam_policy.courseware-asset-resize-lambda-publish-consume.arn}"
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# setup cloudwatch logging
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_cloudwatch_log_group" "g1" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "/aws/lambda/${aws_lambda_function.courseware-asset-resize-processor.function_name}"
  retention_in_days = 7

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_cloudwatch_log_group" "g2" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "/aws/lambda/${aws_lambda_function.courseware-asset-resize-retry-processor.function_name}"
  retention_in_days = 7

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_iam_policy" "lambda_logging" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "lambda_logging"
  path = "/"
  description = "IAM policy for logging from a lambda"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "arn:aws:logs:*:*:*",
      "Effect": "Allow"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "lambda_logs" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  role = "${aws_iam_role.iam_for_lambda.name}"
  policy_arn = "${aws_iam_policy.lambda_logging.arn}"
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# processor dead letters
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sqs_queue" "courseware-asset-resize-processor-dead-letters" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "courseware-asset-resize-processor-dead-letters"
  receive_wait_time_seconds = 20

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_lambda_event_source_mapping" "alesm2" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  event_source_arn = "${aws_sqs_queue.courseware-asset-resize-processor-dead-letters.arn}"
  function_name = "${aws_lambda_function.courseware-asset-resize-processor-dead-letters.arn}"
}

resource "aws_lambda_function" "courseware-asset-resize-processor-dead-letters" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  filename         = "${data.archive_file.lambda_sqs_to_sns.output_path}"
  source_code_hash = "${data.archive_file.lambda_sqs_to_sns.output_base64sha256}"

  function_name = "courseware-asset-resize-processor-dead-letters"
  role = "${aws_iam_role.iam_for_lambda.arn}"
  handler = "index.handler"
  runtime = "nodejs12.x"
  timeout = 30

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }

  environment {
    variables = {
      TOPIC_ARN = "${aws_sns_topic.courseware-asset-resize-processor-dead-letters.arn}",
      DEBUG_LOG = "1"
    }
  }
}

resource "aws_sns_topic" "courseware-asset-resize-processor-dead-letters" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "courseware-asset-resize-processor-dead-letters"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "courseware-asset-resize-processor-dead-letters-subscription" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  topic_arn = "${aws_sns_topic.courseware-asset-resize-processor-dead-letters.arn}"
  protocol = "https"
  endpoint = "https://${var.base_endpoint}/sns/courseware-asset-resize/submit/dead-letters"
  endpoint_auto_confirms = true
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# retry dead letters
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sqs_queue" "courseware-asset-resize-retry-dead-letters" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "courseware-asset-resize-retry-dead-letters"
  receive_wait_time_seconds = 20

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_lambda_event_source_mapping" "alesm3" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  event_source_arn = "${aws_sqs_queue.courseware-asset-resize-retry-dead-letters.arn}"
  function_name = "${aws_lambda_function.courseware-asset-resize-retry-dead-letters.arn}"
}

resource "aws_lambda_function" "courseware-asset-resize-retry-dead-letters" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  filename         = "${data.archive_file.lambda_sqs_to_sns.output_path}"
  source_code_hash = "${data.archive_file.lambda_sqs_to_sns.output_base64sha256}"

  function_name = "courseware-asset-resize-retry-dead-letters"
  role = "${aws_iam_role.iam_for_lambda.arn}"
  handler = "index.handler"
  runtime = "nodejs12.x"
  timeout = 30

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }

  environment {
    variables = {
      TOPIC_ARN = "${aws_sns_topic.courseware-asset-resize-retry-dead-letters.arn}",
      DEBUG_LOG = "1"
    }
  }
}

resource "aws_sns_topic" "courseware-asset-resize-retry-dead-letters" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "courseware-asset-resize-retry-dead-letters"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "courseware-asset-resize-retry-dead-letters-subscription" {
  depends_on       = ["data.archive_file.lambda_image-manipulator_processor","data.archive_file.lambda_sqs_to_sns"]
  topic_arn = "${aws_sns_topic.courseware-asset-resize-retry-dead-letters.arn}"
  protocol = "https"
  endpoint = "https://${var.base_endpoint}/sns/courseware-asset-resize/retry/dead-letters"
  endpoint_auto_confirms = true
}

