data "terraform_remote_state" "network" {
  backend = "s3"
  config {
    bucket       = "terraform.us-west-1.591900134539"
    key          = "v1/network/us-west-1/terraform.tfstate"
    region       = "us-west-1"
    role_arn     = "${lookup(var.role_arn, "${var.ENV}")}"
    session_name = "prod_usw1_infra_platform"
  }
}

data "terraform_remote_state" "deployment" {
  backend = "s3"
  config {
    bucket       = "terraform.us-west-1.591900134539"
    key          = "v1/deployment/us-west-1/terraform.tfstate"
    region       = "us-west-1"
    role_arn     = "${lookup(var.role_arn, "${var.ENV}")}"
    session_name = "prod_usw1_infra_platform"
  }
}

