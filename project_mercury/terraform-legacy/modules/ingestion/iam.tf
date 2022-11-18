resource "aws_iam_policy" "ingestion_access_policy" {
  name = "ingestion-access-policy"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::${var.s3_workspace}"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:AbortMultipartUpload",
        "s3:ListBucketMultipartUploads",  
        "s3:ListMultipartUploadParts"
      ],
      "Resource": [
        "arn:aws:s3:::${var.s3_workspace}/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "sns:Publish"
      ],
      "Resource": [
        "${aws_sns_topic.ingestion_event_log_submit.arn}",
        "${aws_sns_topic.adapter_result.arn}",
        "${aws_sns_topic.ingestion_result.arn}",
        "${aws_sns_topic.ingestion_ambrosia_error.arn}",
        "${aws_sns_topic.adapter_error.arn}"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "ecr:BatchCheckLayerAvailability",
        "ecr:BatchGetImage",
        "ecr:GetDownloadUrlForLayer",
        "ecr:GetAuthorizationToken"
      ],
      "Resource": ["*"]
    }
  ]
}
EOF
}
