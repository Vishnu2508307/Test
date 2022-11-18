data "terraform_remote_state" "network" {
  backend = "s3"
  config {
    bucket       = "terraform.us-east-1.658380148811"
    key          = "v1/network/us-east-1/terraform.tfstate"
    region       = "us-east-1"
    role_arn     = "${lookup(var.role_arn, "${var.ENV}")}"
    session_name = "qaint_use1_infra_platform"
  }
}
