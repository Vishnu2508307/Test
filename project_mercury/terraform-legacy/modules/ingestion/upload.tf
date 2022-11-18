resource "aws_sns_topic" "ingestion_content_upload" {
  name = "ingestion-s3-notification-topic"

  policy = <<POLICY
  {
      "Version":"2012-10-17",
      "Statement":
    [
      {
          "Effect": "Allow",
          "Principal": { "Service": "s3.amazonaws.com" },
          "Action": "SNS:Publish",
          "Resource": "arn:aws:sns:*:*:ingestion-s3-notification-topic",
          "Condition":
          {
              "ArnLike":{"aws:SourceArn":"arn:aws:s3:::${var.s3_workspace}"}
          }
      }
    ]
  }
  POLICY
}

 resource "aws_sns_topic_subscription" "ingestion_content_upload" {
   topic_arn              = "${aws_sns_topic.ingestion_content_upload.arn}"
   protocol               = "https"
   endpoint               = "https://${var.base_endpoint}/sns/ingestion/upload/result"
   endpoint_auto_confirms = true
 }

resource "aws_s3_bucket_notification" "bucket_notification" {
  bucket = "${var.s3_workspace}"

  topic {
    topic_arn     = "${aws_sns_topic.ingestion_content_upload.arn}"
    events        = ["s3:ObjectCreated:*"]
    filter_suffix = ".epub"
  }
}

