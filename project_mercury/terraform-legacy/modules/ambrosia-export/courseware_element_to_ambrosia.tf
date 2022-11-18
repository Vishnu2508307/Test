#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the courseware-element-to-ambrosia-submit topic & subscriptions
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sns_topic" "courseware-element-to-ambrosia-submit" {
  depends_on       = ["data.archive_file.lambda_courseware-element-to-ambrosia_processor"]
  name = "courseware-element-to-ambrosia-submit"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "submit-to-courseware-element-to-ambrosia-processor-lambda" {
  depends_on       = ["data.archive_file.lambda_courseware-element-to-ambrosia_processor"]
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
# create the lambda result dead letters queue
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sqs_queue" "courseware-element-to-ambrosia-result-dead-letters" {
  name = "courseware-element-to-ambrosia-result-dead-letters"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the lambda result queue
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sqs_queue" "courseware-element-to-ambrosia-result" {
  name = "courseware-element-to-ambrosia-result"
  visibility_timeout_seconds = 180

  redrive_policy = <<EOF
{
    "deadLetterTargetArn": "${aws_sqs_queue.courseware-element-to-ambrosia-result-dead-letters.arn}",
    "maxReceiveCount": 3
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
# create the lambda error queue
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sqs_queue" "courseware-element-to-ambrosia-error" {
  name = "courseware-element-to-ambrosia-error",

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the request processing lambda
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━

resource "aws_lambda_function" "courseware-element-to-ambrosia-processor" {
  depends_on       = [
    "data.archive_file.lambda_courseware-element-to-ambrosia_processor"
  ]
  filename         = "${data.archive_file.lambda_courseware-element-to-ambrosia_processor.output_path}"
  source_code_hash = "${data.archive_file.lambda_courseware-element-to-ambrosia_processor.output_base64sha256}"

  function_name = "courseware-element-to-ambrosia-process"
  role = "${aws_iam_role.iam_for_lambda_courseware-element-to-ambrosia.arn}"
  handler = "index.handler"
  runtime = "nodejs12.x"
  memory_size = 256
  timeout = 90

  dead_letter_config {
    target_arn = "${aws_sqs_queue.courseware-element-to-ambrosia-processor-dead-letters.arn}"
  }

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }

  environment {
    variables = {
      QUEUE_URL_COURSEWARE_ELEMENT_TO_AMBROSIA_ERROR = "${aws_sqs_queue.courseware-element-to-ambrosia-error.id}",
      QUEUE_URL_COURSEWARE_ELEMENT_TO_AMBROSIA_RESULT = "${aws_sqs_queue.courseware-element-to-ambrosia-result.id}",
      AMBROSIA_SNIPPET_BUCKET = "${var.ambrosia_bucket}",
      DEBUG_LOG = "1",
      NODE_ENV  = "${var.env}",
      NEW_RELIC_DISTRIBUTED_TRACING_ENABLED="1",
      NEW_RELIC_LAMBDA_EXTENSION_ENABLED="1",
      NEW_RELIC_EXTENSION_SEND_FUNCTION_LOGS="1",
      NEW_RELIC_LAMBDA_HANDLER="${var.lambda_function_handler}",
      NEW_RELIC_ACCOUNT_ID="${var.apm_app_account_id}",
      SNIPPETS_STORAGE_IN_S3 = "true",
    }
  }
  layers= ["arn:aws:lambda:${data.aws_region.current.name}:451483290750:layer:NewRelicNodeJS12X:45"]
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the lambda processing assume role
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_iam_role" "iam_for_lambda_courseware-element-to-ambrosia" {
  depends_on       = ["data.archive_file.lambda_courseware-element-to-ambrosia_processor"]
  name = "${var.env}-iam_for_lambda_courseware-element-to-ambrosia"

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

data "template_file" "bucket_policy" {
  template = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "s3:ListBucket"
      ],
      "Effect": "Allow",
      "Resource": [
        "arn:aws:s3:::$${source_bucket}"
      ]
    },
    {
      "Action": [
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Effect": "Allow",
      "Resource": [
        "arn:aws:s3:::$${source_bucket}/*"
      ]
    }
  ]
}
EOF

  vars {
    source_bucket = "${var.ambrosia_bucket}"
  }
}

resource "aws_iam_policy" "bucket_policy" {
  name   = "policy_to_allow_access_to_ambrosia_snippet_bucket"
  policy = "${data.template_file.bucket_policy.rendered}"
}

resource "aws_iam_role_policy_attachment" "bucket_policy" {
  role       = "${aws_iam_role.iam_for_lambda_courseware-element-to-ambrosia.name}"
  policy_arn = "${aws_iam_policy.bucket_policy.arn}"
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# allow the lambdas to write to do things.
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_iam_policy" "courseware-element-to-ambrosia-lambda-publish-consume" {
  depends_on       = ["data.archive_file.lambda_courseware-element-to-ambrosia_processor"]
  name = "courseware-element-to-ambrosia-lambda-publish-consume"
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
        "arn:aws:s3:::${var.ambrosia_bucket}"
      ]
    },
    {
      "Action": [
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Effect": "Allow",
      "Resource": [
        "arn:aws:s3:::${var.ambrosia_bucket}/*"
      ]
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "courseware-element-to-ambrosia-lambda-publish-consume-policy" {
  depends_on       = ["data.archive_file.lambda_courseware-element-to-ambrosia_processor"]
  role = "${aws_iam_role.iam_for_lambda_courseware-element-to-ambrosia.name}"
  policy_arn = "${aws_iam_policy.courseware-element-to-ambrosia-lambda-publish-consume.arn}"
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# setup cloudwatch logging
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_cloudwatch_log_group" "g1" {
  depends_on       = ["data.archive_file.lambda_courseware-element-to-ambrosia_processor"]
  name = "/aws/lambda/${aws_lambda_function.courseware-element-to-ambrosia-processor.function_name}"
  retention_in_days = 7

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_iam_policy" "lambda_logging_courseware-element-to-ambrosia" {
  depends_on       = ["data.archive_file.lambda_courseware-element-to-ambrosia_processor"]
  name = "${var.env}-lambda_logging_courseware-element-to-ambrosia"
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
  depends_on       = ["data.archive_file.lambda_courseware-element-to-ambrosia_processor"]
  role = "${aws_iam_role.iam_for_lambda_courseware-element-to-ambrosia.name}"
  policy_arn = "${aws_iam_policy.lambda_logging_courseware-element-to-ambrosia.arn}"
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# processor dead letters
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sqs_queue" "courseware-element-to-ambrosia-processor-dead-letters" {
  depends_on       = ["data.archive_file.lambda_courseware-element-to-ambrosia_processor"]
  name = "courseware-element-to-ambrosia-processor-dead-letters"
  receive_wait_time_seconds = 20

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}
