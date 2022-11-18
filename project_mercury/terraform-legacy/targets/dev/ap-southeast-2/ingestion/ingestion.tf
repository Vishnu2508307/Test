module "ingestion" {
  providers = {
    "aws" = "aws.ap_southeast_2"
  }

  source                 = "../../../../modules/ingestion"
  main_revision          = "0.0.1"
  aws_region             = "${data.aws_region.current.name}"
  base_endpoint          = "${lookup(var.mercury_endpoints, "${var.ENV}.mercury")}"
  env                    = "${var.ENV}"
  account_id             = "${var.dev_account_id}"

  project                = "${var.PROJECT}"
  launch_type            = "${var.ingestion_launch_type}"

  s3_workspace           = "${lookup(var.s3_buckets, "${var.ENV}.ingestion.${data.aws_region.current.name}")}"
  deploy_to_cluster      = "${data.terraform_remote_state.deployment.ingestion_cluster_name}"
  desired_amount         = "${var.ingestion_desired_amount}"
  autoscale_max_capacity = "${var.ingestion_autoscale_max_capacity}"

  security_group_id      = "${data.terraform_remote_state.network.ingestion_security_group}"
  port                   = "${var.ingestion_port}"
  tag                    = "${var.release_version}"
  subnet_ids             = [
    "${lookup(data.terraform_remote_state.network.ingestion_private_subnet, "ap-southeast-2a")}",
    "${lookup(data.terraform_remote_state.network.ingestion_private_subnet, "ap-southeast-2b")}"
  ]
  cpu                    = "${var.ingestion_cpu}"
  memory                 = "${var.ingestion_memory}"
  sns_alarm_arn          = ["arn:aws:sns:${data.aws_region.current.name}:${var.dev_account_id}:ALARMS"]

  t_AppID                = "${var.t_AppID}"
  t_cost_centre          = "${var.t_cost_centre}"
  t_environment          = "${var.t_environment}"
  t_dcl                  = "${var.t_dcl}"
}
