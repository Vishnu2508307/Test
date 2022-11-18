output "lambda_courseware-element-to-ambrosia" {
  value = "${data.archive_file.lambda_image-manipulator_processor.output_size}"
}

output "lambda_sqs_to_sns" {
  value = "${data.archive_file.lambda_sqs_to_sns.output_size}"
}
