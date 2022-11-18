module "asset" {
  providers = {
    "aws"   = "aws.us_west_1"
  }
  source        = "../../../../modules/asset"
  main_revision = "0.0.2"
  base_endpoint = "${lookup(var.mercury_endpoints, "${var.ENV}.mercury")}"
  env           = "${var.ENV}"

  t_AppID       = "${var.t_AppID}"
  t_cost_centre = "${var.t_cost_centre}"
  t_environment = "${var.t_environment}"
  t_dcl         = "${var.t_dcl}"
}
