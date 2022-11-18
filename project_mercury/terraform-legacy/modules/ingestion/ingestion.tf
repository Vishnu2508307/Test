#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the ingestion topics & subscription
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sns_topic" "ingestion_event_log_submit" {
  name = "ingestion-event-log-submit"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "ingestion_event_log_submit_subscription" {
  topic_arn              = "${aws_sns_topic.ingestion_event_log_submit.arn}"
  protocol               = "https"
  endpoint               = "https://${var.base_endpoint}/sns/ingestion/event-log/result"
  endpoint_auto_confirms = true
}

resource "aws_sns_topic" "adapter_error" {
  name = "ingestion-adapter-error"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "ingestion_adapter_error_subscription" {
  topic_arn              = "${aws_sns_topic.adapter_error.arn}"
  protocol               = "https"
  endpoint               = "https://${var.base_endpoint}/sns/ingestion/adapter/error"
  endpoint_auto_confirms = true
}

resource "aws_sns_topic" "adapter_result" {
  name = "ingestion-adapter-result"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "ingestion_adapter_result_subscription" {
  topic_arn              = "${aws_sns_topic.adapter_result.arn}"
  protocol               = "https"
  endpoint               = "https://${var.base_endpoint}/sns/ingestion/adapter/result"
  endpoint_auto_confirms = true
}

resource "aws_sns_topic" "ingestion_result" {
  name = "ingestion-ambrosia-result"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "ingestion_ambrosia_result_subscription" {
  topic_arn              = "${aws_sns_topic.ingestion_result.arn}"
  protocol               = "https"
  endpoint               = "https://${var.base_endpoint}/sns/ingestion/ambrosia/result"
  endpoint_auto_confirms = true
}

resource "aws_sns_topic" "ingestion_ambrosia_error" {
  name = "ingestion-ambrosia-error"

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}

resource "aws_sns_topic_subscription" "ingestion_ambrosia_error_subscription" {
  topic_arn              = "${aws_sns_topic.ingestion_ambrosia_error.arn}"
  protocol               = "https"
  endpoint               = "https://${var.base_endpoint}/sns/ingestion/ambrosia/error"
  endpoint_auto_confirms = true
}

#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▼ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
# create the ingestion-cancel-submit SQS
#━━━━━━━━━━━━━━━━━━━━━━━━━━━ ▲ ━━━━━━━━━━━━━━━━━━━━━━━━━━━
resource "aws_sqs_queue" "ingestion_cancel_submit" {
  name = "ingestion-cancel-submit"
  visibility_timeout_seconds = 120

  tags {
    t_AppID       = "${var.t_AppID}"
    t_cost_centre = "${var.t_cost_centre}"
    t_environment = "${var.t_environment}"
    t_dcl         = "${var.t_dcl}"
  }
}