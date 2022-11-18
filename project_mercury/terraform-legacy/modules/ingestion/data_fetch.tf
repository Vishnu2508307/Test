data "archive_file" "lambda_ingestion_coordinator" {
  type        = "zip"
  source_dir  = "${path.module}/files/lambda-ingestion-coordinator-${var.main_revision}/"
  output_path = "/opt/gitlab-runner/builds/pearsontechnology/gpt/bronte/service/bronte-service-mercury/terraform-legacy/build/lambda-ingestion-coordinator-${var.main_revision}.zip"
}
