module "export" {
  providers = {
    "aws"   = "aws.us_east_1"
  }
  source              = "../../../../modules/ambrosia-export"
  main_revision       = "1.0.14"
  base_endpoint       = "${lookup(var.mercury_endpoints, "${var.ENV}.mercury")}"
  ambrosia_bucket     = "${lookup(var.s3_buckets, "${var.ENV}.ambrosia-snippet.us-east-1")}"
  env                 = "${var.ENV}"
  apm_app_name        = "${var.apm_app_name}"
  apm_app_account_id  = "${var.apm_app_account_id}"

  t_AppID             = "${var.t_AppID}"
  t_cost_centre       = "${var.t_cost_centre}"
  t_environment       = "${var.t_environment}"
  t_dcl               = "${var.t_dcl}"
}
