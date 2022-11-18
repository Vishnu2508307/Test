data "terraform_remote_state" "network" {
  backend = "s3"
  config {
    bucket       = "terraform.us-east-1.324267317821"
    key          = "v1/network/us-east-1/terraform.tfstate"
    region       = "us-east-1"
    role_arn     = "${lookup(var.role_arn, "${var.ENV}")}"
    session_name = "stg_use1_infra_platform"
  }
}

data "terraform_remote_state" "deployment" {
  backend = "s3"
  config {
    bucket       = "terraform.us-east-1.324267317821"
    key          = "v1/deployment/us-east-1/terraform.tfstate"
    region       = "us-east-1"
    role_arn     = "${lookup(var.role_arn, "${var.ENV}")}"
    session_name = "stg_use1_infra_platform"
  }
}

