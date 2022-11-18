terraform {
  backend "s3" {
    bucket       = "terraform.us-west-1.591900134539"
    key          = "v1/asset/us-west-1/terraform.tfstate"
    acl          = "bucket-owner-full-control"
    region       = "us-west-1"
    role_arn     = "arn:aws:iam::591900134539:role/terraform"
    session_name = "prod_usw1_infra_platform"
  }
}
