data "archive_file" "lambda_courseware-element-to-ambrosia_processor" {
  type        = "zip"
  source_dir  = "${path.module}/files/lambda-courseware-element-to-ambrosia-${var.main_revision}/"
  output_path = "/opt/gitlab-runner/builds/pearsontechnology/gpt/bronte/service/bronte-service-mercury/terraform-legacy/build/lambda-courseware-element-to-ambrosia-${var.main_revision}.zip"
}

data "aws_region" "current" {}
