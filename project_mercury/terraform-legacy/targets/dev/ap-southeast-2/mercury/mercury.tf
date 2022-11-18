module "app" {
  source    = "../../../../modules/mercury"
  providers = {
    "aws"   = "aws.ap_southeast_2"
  }
  env                                = "${var.ENV}"
  project                            = "${var.PROJECT}"
  launch_type                        = "${var.mercury_launch_type}"
  attach_to_target_group             = "${var.target_group_name}"
  deploy_to_cluster                  = "${var.deploy_to_cluster}"
  env_region                         = "${var.env_region}"
  cassandra_contactpoints            = "${var.contact_points}"
  cassandra_authentication_username  = "${var.cassandra_user}"
  cassandra_authentication_password  = "${var.secrets_cassandra_password_key}"
  fetch_truststore_provider          = "secretsmanager"
  fetch_truststore_source_path       = "${var.secrets_cassandra_keystore_key}"
  cassandra_keystore_password        = "${var.secrets_cassandra_keystore_password_key}"

  cloudwatch_log_group               = "/${var.ENV}/ap-southeast-2/${var.PROJECT}"
  desired_amount                     = "${var.desired_amount}"

  security_group_ids                 = [
    "${lookup(data.terraform_remote_state.network.mercury_app, "ap-southeast-2")}",
    "${lookup(data.terraform_remote_state.network.mercury_app_from_spr_network, "ap-southeast-2")}"
  ]
  subnet_ids                         = [
    "${lookup(data.terraform_remote_state.network.mercury_app_private_subnet, "ap-southeast-2a")}",
    "${lookup(data.terraform_remote_state.network.mercury_app_private_subnet, "ap-southeast-2b")}"
  ]
  cpu                                = "${var.mercury_cpu}"
  memory                             = "${var.mercury_memory}"

  deployment_maximum_percent         = "${var.deployment_maximum_percent}"
  deployment_minimum_healthy_percent = "${var.deployment_minimum_healthy_percent}"

  enable_cloudwatch_alarms           = "${var.enable_cloudwatch_alarms}"
  sns_alarm_arn                      = [
    "arn:aws:sns:ap-southeast-2:${var.dev_account_id}:ALARMS"
  ]
  enable_autoscaling                 = "${var.enable_autoscaling}"
  autoscale_min_capacity             = "${var.autoscale_min_capacity}"
  autoscale_max_capacity             = "${var.autoscale_max_capacity}"
  health_grace_period                = "${var.health_grace_period}"
  tag                                = "${var.release_version}"
  apm_enabled                        = "${var.apm_enabled}"
  apm_app_name                       = "${var.apm_app_name}"
  account_id                         = "${var.dev_account_id}"

  t_AppID       = "${var.t_AppID}"
  t_cost_centre = "${var.t_cost_centre}"
  t_environment = "${var.t_environment}"
  t_dcl         = "${var.t_dcl}"
}
