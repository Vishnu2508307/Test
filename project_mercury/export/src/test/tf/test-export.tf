provider "aws" {
  profile = "LOCAL"
  region = "ap-southeast-2"
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# this variable should be set to your name.
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
variable "prefix" {
  type = string
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# this is the endpoint to deliver sns to, without a trailing slash.
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
variable "base_endpoint" {
  type = string
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the export-submit topic & subscriptions
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sns_topic" "courseware-element-to-ambrosia-submit" {
  name = "${var.prefix}-courseware-element-to-ambrosia-submit"
}

resource "aws_sns_topic_subscription" "submit-to-courseware-element-to-ambrosia-lambda" {
  topic_arn = "${aws_sns_topic.courseware-element-to-ambrosia-submit.arn}"
  protocol = "lambda"
  endpoint = "${aws_lambda_function.courseware-element-to-ambrosia-processor.arn}"
}

resource "aws_lambda_permission" "with_sns" {
  statement_id = "AllowExecutionFromSNS"
  action = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.courseware-element-to-ambrosia-processor.arn}"
  principal = "sns.amazonaws.com"
  source_arn = "${aws_sns_topic.courseware-element-to-ambrosia-submit.arn}"
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the lambda result queue
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sqs_queue" "courseware-element-to-ambrosia-result" {
  name = "${var.prefix}-courseware-element-to-ambrosia-result"
  message_retention_seconds = 900
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the lambda error queue
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sqs_queue" "courseware-element-to-ambrosia-error" {
  name = "${var.prefix}-courseware-element-to-ambrosia-error"
  message_retention_seconds = 900
}

//#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
//# create the lambda retry topic
//#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
//resource "aws_sns_topic" "courseware-element-to-ambrosia-retry" {
//  name = "${var.prefix}-courseware-element-to-ambrosia-retry"
//}
//
//resource "aws_sns_topic_subscription" "courseware-element-to-ambrosia-retry-subscription" {
//  topic_arn = "${aws_sns_topic.courseware-element-to-ambrosia-retry.arn}"
//  protocol = "https"
//  endpoint = "${var.base_endpoint}/sns/courseware-element-to-ambrosia/retry"
//  endpoint_auto_confirms = true
//}
//
//#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
//# create the sqs queue for delayed retries
//#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
//resource "aws_sqs_queue" "courseware-element-to-ambrosia-retry-delay" {
//  name = "${var.prefix}-courseware-element-to-ambrosia-retry-delay"
//  receive_wait_time_seconds = 20
//  # https://docs.aws.amazon.com/lambda/latest/dg/dlq.html
//  # > If you are using Amazon SQS as an event source, configure a DLQ on the Amazon SQS queue itself and not the Lambda function.
//  redrive_policy = "{\"deadLetterTargetArn\":\"${aws_sqs_queue.courseware-element-to-ambrosia-retry-dead-letters.arn}\",\"maxReceiveCount\":4}"
//}
//
//resource "aws_lambda_event_source_mapping" "alesm1" {
//  event_source_arn = "${aws_sqs_queue.courseware-element-to-ambrosia-retry-delay.arn}"
//  function_name = "${aws_lambda_function.courseware-element-to-ambrosia-processor.arn}"
//}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the request processing lambda
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
#FIXME replace lambda with a test lambda for now
resource "aws_lambda_function" "courseware-element-to-ambrosia-processor" {
  filename = "../../../../../bronte-service-lambda-courseware-element-to-ambrosia/bronte-service-lambda-courseware-element-to-ambrosia-v1.0.10.zip"
  source_code_hash = "${filebase64sha256("../../../../../bronte-service-lambda-courseware-element-to-ambrosia/bronte-service-lambda-courseware-element-to-ambrosia-v1.0.10.zip")}"
  #filename = "../../../../../aero-service-lambda-fail/aero-service-lambda-fail-1.0.0.zip"
  #source_code_hash = "${filebase64sha256("../../../../../aero-service-lambda-sqs-to-sns/lambda-sqs-to-sns-0.0.1.zip")}"

  function_name = "${var.prefix}-courseware-element-to-ambrosia"
  role = "${aws_iam_role.iam_for_lambda.arn}"
  handler = "index.handler"
  runtime = "nodejs12.x"
  timeout = 30

  dead_letter_config {
    target_arn = "${aws_sqs_queue.courseware-element-to-ambrosia-processor-dead-letters.arn}"
  }

  environment {
    variables = {
      QUEUE_URL_COURSEWARE_ELEMENT_TO_AMBROSIA_ERROR = "${aws_sqs_queue.courseware-element-to-ambrosia-error.id}",
      QUEUE_URL_COURSEWARE_ELEMENT_TO_AMBROSIA_RESULT = "${aws_sqs_queue.courseware-element-to-ambrosia-result.id}",
      AMBROSIA_SNIPPET_BUCKET = "${aws_s3_bucket.snippet_bucket.bucket}",
      NODE_ENV = "local",
      DEBUG_LOG = "1",
    }
  }
}

//#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
//# create the retry (sqs-to-sns) processing lambda
//#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
//resource "aws_lambda_function" "courseware-element-to-ambrosia-retry-processor" {
//  filename = "../../../../../aero-service-lambda-sqs-to-sns/lambda-sqs-to-sns-0.0.1.zip"
//  source_code_hash = "${filebase64sha256("../../../../../aero-service-lambda-sqs-to-sns/lambda-sqs-to-sns-0.0.1.zip")}"
//  #filename = "../../../../../aero-service-lambda-fail/aero-service-lambda-fail-1.0.0.zip"
//  #source_code_hash = "${filebase64sha256("../../../../../aero-service-lambda-sqs-to-sns/lambda-sqs-to-sns-0.0.1.zip")}"
//
//  function_name = "${var.prefix}-courseware-element-to-ambrosia-sqs-to-sns"
//  role = "${aws_iam_role.iam_for_lambda.arn}"
//  handler = "index.handler"
//  runtime = "nodejs12.x"
//  timeout = 30
//
//  # the dead letter is configured on the SQS level for this one.
//
//  environment {
//    variables = {
//      TOPIC_ARN = "${aws_sns_topic.courseware-element-to-ambrosia-retry-dead-letters.arn}",
//      DEBUG_LOG = "1"
//    }
//  }
//}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the lambda processing assume role
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_iam_role" "iam_for_lambda" {
  name = "${var.prefix}-iam_for_lambda"

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
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# allow the lambdas to write to do things.
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_iam_policy" "courseware-element-to-ambrosia-lambda-publish-consume" {
  name = "${var.prefix}-courseware-element-to-ambrosia-lambda-publish-consume"
  path = "/"
  description = "Ability for lambda to publish-consume results"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
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
        "${aws_sqs_queue.courseware-element-to-ambrosia-result.arn}",
        "${aws_sqs_queue.courseware-element-to-ambrosia-error.arn}",
        "${aws_sqs_queue.courseware-element-to-ambrosia-processor-dead-letters.arn}"
      ]
    },
    {
      "Action": [
        "s3:ListBucket"
      ],
      "Effect": "Allow",
      "Resource": [
        "${aws_s3_bucket.snippet_bucket.arn}"
      ]
    },
    {
      "Action": ["*"],
      "Effect": "Allow",
      "Resource": [
        "${aws_s3_bucket.snippet_bucket.arn}/*"
      ]
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "courseware-element-to-ambrosia-lambda-publish-consume-policy" {
  role = "${aws_iam_role.iam_for_lambda.name}"
  policy_arn = "${aws_iam_policy.courseware-element-to-ambrosia-lambda-publish-consume.arn}"
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# setup cloudwatch logging
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_cloudwatch_log_group" "g1" {
  name = "/aws/lambda/${aws_lambda_function.courseware-element-to-ambrosia-processor.function_name}"
  retention_in_days = 7
}

//resource "aws_cloudwatch_log_group" "g2" {
//  name = "/aws/lambda/${aws_lambda_function.courseware-element-to-ambrosia-retry-processor.function_name}"
//  retention_in_days = 7
//}

resource "aws_iam_policy" "lambda_logging" {
  name = "${var.prefix}-lambda_logging"
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
resource "aws_sqs_queue" "courseware-element-to-ambrosia-processor-dead-letters" {
  name = "${var.prefix}-courseware-element-to-ambrosia-processor-dead-letters"
  receive_wait_time_seconds = 20
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create bucket for snippets
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_s3_bucket" "snippet_bucket" {
  bucket = "${var.prefix}-ambrosia-snippet"
  acl = "private"
}

//resource "aws_lambda_event_source_mapping" "alesm2" {
//  event_source_arn = "${aws_sqs_queue.courseware-element-to-ambrosia-processor-dead-letters.arn}"
//  function_name = "${aws_lambda_function.courseware-element-to-ambrosia-processor-dead-letters.arn}"
//}

//resource "aws_lambda_function" "courseware-element-to-ambrosia-processor-dead-letters" {
//  filename = "../../../../../aero-service-lambda-sqs-to-sns/lambda-sqs-to-sns-0.0.1.zip"
//  function_name = "${var.prefix}-courseware-element-to-ambrosia-processor-dead-letters"
//  role = "${aws_iam_role.iam_for_lambda.arn}"
//  handler = "index.handler"
//  source_code_hash = "${filebase64sha256("../../../../../aero-service-lambda-sqs-to-sns/lambda-sqs-to-sns-0.0.1.zip")}"
//  runtime = "nodejs12.x"
//  timeout = 30
//
//  environment {
//    variables = {
//      TOPIC_ARN = "${aws_sns_topic.courseware-element-to-ambrosia-processor-dead-letters.arn}",
//      DEBUG_LOG = "1"
//    }
//  }
//}

//resource "aws_sns_topic" "courseware-element-to-ambrosia-processor-dead-letters" {
//  name = "${var.prefix}-courseware-element-to-ambrosia-processor-dead-letters"
//}

//resource "aws_sns_topic_subscription" "courseware-element-to-ambrosia-processor-dead-letters-subscription" {
//  topic_arn = "${aws_sns_topic.courseware-element-to-ambrosia-processor-dead-letters.arn}"
//  protocol = "https"
//  endpoint = "${var.base_endpoint}/sns/courseware-element-to-ambrosia/submit/dead-letters"
//  endpoint_auto_confirms = true
//}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# retry dead letters
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
//resource "aws_sqs_queue" "courseware-element-to-ambrosia-retry-dead-letters" {
//  name = "${var.prefix}-courseware-element-to-ambrosia-retry-dead-letters"
//  receive_wait_time_seconds = 20
//}

//resource "aws_lambda_event_source_mapping" "alesm3" {
//  event_source_arn = "${aws_sqs_queue.courseware-element-to-ambrosia-retry-dead-letters.arn}"
//  function_name = "${aws_lambda_function.courseware-element-to-ambrosia-retry-dead-letters.arn}"
//}

//resource "aws_lambda_function" "courseware-element-to-ambrosia-retry-dead-letters" {
//  filename = "../../../../../aero-service-lambda-sqs-to-sns/lambda-sqs-to-sns-0.0.1.zip"
//  function_name = "${var.prefix}-courseware-element-to-ambrosia-retry-dead-letters"
//  role = "${aws_iam_role.iam_for_lambda.arn}"
//  handler = "index.handler"
//  source_code_hash = "${filebase64sha256("../../../../../aero-service-lambda-sqs-to-sns/lambda-sqs-to-sns-0.0.1.zip")}"
//  runtime = "nodejs12.x"
//  timeout = 30
//
//  environment {
//    variables = {
//      TOPIC_ARN = "${aws_sns_topic.courseware-element-to-ambrosia-retry-dead-letters.arn}",
//      DEBUG_LOG = "1"
//    }
//  }
//}

//resource "aws_sns_topic" "courseware-element-to-ambrosia-retry-dead-letters" {
//  name = "${var.prefix}-courseware-element-to-ambrosia-retry-dead-letters"
//}

//resource "aws_sns_topic_subscription" "courseware-element-to-ambrosia-retry-dead-letters-subscription" {
//  topic_arn = "${aws_sns_topic.courseware-element-to-ambrosia-retry-dead-letters.arn}"
//  protocol = "https"
//  endpoint = "${var.base_endpoint}/sns/courseware-element-to-ambrosia/retry/dead-letters"
//  endpoint_auto_confirms = true
//}

//data "aws_secretsmanager_secret_version" "newrelic_license_key" {
//  secret_id = "arn:aws:secretsmanager:ap-southeast-2:716062133555:secret:/dev/mercury/newrelic"
//}
