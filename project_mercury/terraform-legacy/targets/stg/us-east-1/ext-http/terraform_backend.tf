terraform {
  backend "s3" {
    bucket       = "terraform.us-east-1.324267317821"
    key          = "v1/ext-http/us-east-1/terraform.tfstate"
    acl          = "bucket-owner-full-control"
    region       = "us-east-1"
    role_arn     = "arn:aws:iam::324267317821:role/terraform"
    session_name = "stg_use1_infra_platform"
  }
}
