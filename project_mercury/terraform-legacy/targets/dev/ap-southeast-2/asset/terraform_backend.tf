terraform {
  backend "s3" {
    bucket       = "terraform.ap-southeast-2.716062133555"
    key          = "v1/asset/ap-southeast-2/terraform.tfstate"
    acl          = "bucket-owner-full-control"
    region       = "ap-southeast-2"
    role_arn     = "arn:aws:iam::716062133555:role/terraform"
    session_name = "dev_aps2_infra_platform"
  }
}
