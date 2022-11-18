data "archive_file" "lambda_ext_http_processor" {
  type        = "zip"
  source_dir  = "${path.module}/files/lambda-ext-http-${var.main_revision}/"
  output_path = "/opt/gitlab-runner/builds/pearsontechnology/gpt/bronte/service/bronte-service-mercury/terraform-legacy/build/lambda-ext-http-${var.main_revision}.zip"
}

data "archive_file" "lambda_sqs_to_sns" {
  type        = "zip"
  source_dir  = "${path.module}/files/lambda-sqs-to-sns-0.0.1/"
  output_path = "/opt/gitlab-runner/builds/pearsontechnology/gpt/bronte/service/bronte-service-mercury/terraform-legacy/build/lambda-ext-http-sqs-to-sns-0.0.1.zip"
}
