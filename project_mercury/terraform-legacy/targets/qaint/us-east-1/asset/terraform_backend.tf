terraform {
  backend "s3" {
    bucket       = "terraform.us-east-1.658380148811"
    key          = "v1/asset/us-east-1/terraform.tfstate"
    acl          = "bucket-owner-full-control"
    region       = "us-east-1"
    role_arn     = "arn:aws:iam::658380148811:role/terraform"
    session_name = "qaint_use1_infra_platform"
  }
}
