data "terraform_remote_state" "network" {
  backend = "s3"
  config {
    bucket       = "terraform.ap-southeast-2.716062133555"
    key          = "v1/network/ap-southeast-2/terraform.tfstate"
    region       = "ap-southeast-2"
    role_arn     = "${lookup(var.role_arn, "${var.ENV}")}"
    session_name = "dev_aps2_infra_platform"
  }
}
