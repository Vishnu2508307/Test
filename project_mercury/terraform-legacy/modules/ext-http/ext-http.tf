#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the ext-http-submit topic & subscriptions
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sns_topic" "ext-http-submit" {
  depends_on       = ["data.archive_file.lambda_ext_http_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "ext-http-submit"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "submit-to-ext-http-processor-lambda" {
  topic_arn = "${aws_sns_topic.ext-http-submit.arn}"
  protocol = "lambda"
  endpoint = "${aws_lambda_function.ext-http-processor.arn}"
}

resource "aws_lambda_permission" "with_sns" {
  statement_id = "AllowExecutionFromSNS"
  action = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.ext-http-processor.arn}"
  principal = "sns.amazonaws.com"
  source_arn = "${aws_sns_topic.ext-http-submit.arn}"
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the lambda result topic
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sns_topic" "ext-http-result" {
  depends_on       = ["data.archive_file.lambda_ext_http_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "ext-http-result"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "ext-http-result-subscription" {
  topic_arn = "${aws_sns_topic.ext-http-result.arn}"
  protocol = "https"
  endpoint = "https://${var.base_endpoint}/sns/ext_http/result"
  endpoint_auto_confirms = true
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the lambda error topic
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sns_topic" "ext-http-error" {
  depends_on       = ["data.archive_file.lambda_ext_http_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "ext-http-error"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "ext-http-error-subscription" {
  topic_arn = "${aws_sns_topic.ext-http-error.arn}"
  protocol = "https"
  endpoint = "https://${var.base_endpoint}/sns/ext_http/error"
  endpoint_auto_confirms = true
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the lambda retry topic
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sns_topic" "ext-http-retry" {
  depends_on       = ["data.archive_file.lambda_ext_http_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "ext-http-retry"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "ext-http-retry-subscription" {
  topic_arn = "${aws_sns_topic.ext-http-retry.arn}"
  protocol = "https"
  endpoint = "https://${var.base_endpoint}/sns/ext_http/retry"
  endpoint_auto_confirms = true
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the sqs queue for delayed retries
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sqs_queue" "ext-http-retry-delay" {
  name = "ext-http-retry-delay"
  receive_wait_time_seconds = 20
  # https://docs.aws.amazon.com/lambda/latest/dg/dlq.html
  # > If you are using Amazon SQS as an event source, configure a DLQ on the Amazon SQS queue itself and not the Lambda function.
  redrive_policy = "{\"deadLetterTargetArn\":\"${aws_sqs_queue.ext-http-retry-dead-letters.arn}\",\"maxReceiveCount\":4}"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_lambda_event_source_mapping" "alesm1" {
  event_source_arn = "${aws_sqs_queue.ext-http-retry-delay.arn}"
  function_name = "${aws_lambda_function.ext-http-retry-processor.arn}"
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the request processing lambda
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_lambda_function" "ext-http-processor" {
  depends_on       = ["data.archive_file.lambda_ext_http_processor","data.archive_file.lambda_sqs_to_sns"]
  filename         = "${data.archive_file.lambda_ext_http_processor.output_path}"
  source_code_hash = "${data.archive_file.lambda_ext_http_processor.output_base64sha256}"

  function_name = "ext-http-process"
  role = "${aws_iam_role.iam_for_lambda.arn}"
  handler = "index.handler"
  runtime = "nodejs12.x"
  timeout = 300
  
  dead_letter_config {
    target_arn = "${aws_sns_topic.ext-http-processor-dead-letters.arn}"
  }

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }

  environment {
    variables = {
      TOPIC_ARN_EXT_HTTP_ERROR = "${aws_sns_topic.ext-http-error.arn}",
      TOPIC_ARN_EXT_HTTP_RESULT = "${aws_sns_topic.ext-http-result.arn}",
      DEBUG_LOG = "1"
    }
  }
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the ext-http-process error handling configuration
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_lambda_function_event_invoke_config" "ext-http-processor" {
  function_name = "${aws_lambda_function.ext-http-processor.arn}"
  maximum_retry_attempts = 0
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the retry (sqs-to-sns) processing lambda
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_lambda_function" "ext-http-retry-processor" {
  depends_on       = ["data.archive_file.lambda_ext_http_processor","data.archive_file.lambda_sqs_to_sns"]
  filename         = "${data.archive_file.lambda_sqs_to_sns.output_path}"
  source_code_hash = "${data.archive_file.lambda_sqs_to_sns.output_base64sha256}"

  function_name = "ext-http-sqs-to-sns"
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
      TOPIC_ARN = "${aws_sns_topic.ext-http-retry.arn}",
      DEBUG_LOG = "1"
    }
  }
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the lambda processing assume role
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_iam_role" "iam_for_lambda" {
  depends_on       = ["data.archive_file.lambda_ext_http_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "${var.env}-iam_for_lambda"

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
resource "aws_iam_policy" "ext-http-lambda-publish-consume" {
  name = "ext-http-lambda-publish-consume"
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
        "${aws_sns_topic.ext-http-error.arn}",
        "${aws_sns_topic.ext-http-result.arn}",
        "${aws_sns_topic.ext-http-retry.arn}",
        "${aws_sns_topic.ext-http-processor-dead-letters.arn}",
        "${aws_sns_topic.ext-http-retry-dead-letters.arn}"
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
        "${aws_sqs_queue.ext-http-retry-delay.arn}",
        "${aws_sqs_queue.ext-http-processor-dead-letters.arn}",
        "${aws_sqs_queue.ext-http-retry-dead-letters.arn}"
      ]
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "ext-http-lambda-publish-consume-policy" {
  role = "${aws_iam_role.iam_for_lambda.name}"
  policy_arn = "${aws_iam_policy.ext-http-lambda-publish-consume.arn}"
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# setup cloudwatch logging
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_cloudwatch_log_group" "g1" {
  name = "/aws/lambda/${aws_lambda_function.ext-http-processor.function_name}"
  retention_in_days = 7

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_cloudwatch_log_group" "g2" {
  name = "/aws/lambda/${aws_lambda_function.ext-http-retry-processor.function_name}"
  retention_in_days = 7

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_iam_policy" "lambda_logging" {
  name = "${var.env}-lambda_logging"
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
  role = "${aws_iam_role.iam_for_lambda.name}"
  policy_arn = "${aws_iam_policy.lambda_logging.arn}"
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# processor dead letters
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sqs_queue" "ext-http-processor-dead-letters" {
  name = "ext-http-processor-dead-letters"
  receive_wait_time_seconds = 20

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_lambda_event_source_mapping" "alesm2" {
  event_source_arn = "${aws_sqs_queue.ext-http-processor-dead-letters.arn}"
  function_name = "${aws_lambda_function.ext-http-processor-dead-letters.arn}"
}

resource "aws_lambda_function" "ext-http-processor-dead-letters" {
  depends_on       = ["data.archive_file.lambda_ext_http_processor","data.archive_file.lambda_sqs_to_sns"]
  filename         = "${data.archive_file.lambda_sqs_to_sns.output_path}"
  source_code_hash = "${data.archive_file.lambda_sqs_to_sns.output_base64sha256}"

  function_name = "ext-http-processor-dead-letters"
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
      TOPIC_ARN = "${aws_sns_topic.ext-http-processor-dead-letters.arn}",
      DEBUG_LOG = "1"
    }
  }
}

resource "aws_sns_topic" "ext-http-processor-dead-letters" {
  depends_on       = ["data.archive_file.lambda_ext_http_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "ext-http-processor-dead-letters"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "ext-http-processor-dead-letters-subscription" {
  topic_arn = "${aws_sns_topic.ext-http-processor-dead-letters.arn}"
  protocol = "https"
  endpoint = "https://${var.base_endpoint}/sns/ext_http/submit/dead-letters"
  endpoint_auto_confirms = true
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# retry dead letters
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sqs_queue" "ext-http-retry-dead-letters" {
  name = "ext-http-retry-dead-letters"
  receive_wait_time_seconds = 20

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_lambda_event_source_mapping" "alesm3" {
  event_source_arn = "${aws_sqs_queue.ext-http-retry-dead-letters.arn}"
  function_name = "${aws_lambda_function.ext-http-retry-dead-letters.arn}"
}

resource "aws_lambda_function" "ext-http-retry-dead-letters" {
  depends_on       = ["data.archive_file.lambda_ext_http_processor","data.archive_file.lambda_sqs_to_sns"]
  filename         = "${data.archive_file.lambda_sqs_to_sns.output_path}"
  source_code_hash = "${data.archive_file.lambda_sqs_to_sns.output_base64sha256}"

  function_name = "ext-http-retry-dead-letters"
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
      TOPIC_ARN = "${aws_sns_topic.ext-http-retry-dead-letters.arn}",
      DEBUG_LOG = "1"
    }
  }
}

resource "aws_sns_topic" "ext-http-retry-dead-letters" {
  depends_on       = ["data.archive_file.lambda_ext_http_processor","data.archive_file.lambda_sqs_to_sns"]
  name = "ext-http-retry-dead-letters"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "ext-http-retry-dead-letters-subscription" {
  topic_arn = "${aws_sns_topic.ext-http-retry-dead-letters.arn}"
  protocol = "https"
  endpoint = "https://${var.base_endpoint}/sns/ext_http/retry/dead-letters"
  endpoint_auto_confirms = true
}
