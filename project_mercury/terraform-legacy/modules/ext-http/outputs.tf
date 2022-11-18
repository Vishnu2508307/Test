output "lambda_ext_http" {
  value = "${data.archive_file.lambda_ext_http_processor.output_size}"
}

output "lambda_sqs_to_sns" {
  value = "${data.archive_file.lambda_sqs_to_sns.output_size}"
}
