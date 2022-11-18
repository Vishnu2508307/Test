terraform {
  backend "s3" {
    bucket       = "terraform.us-east-1.443982649941"
    key          = "v1/data/us-east-1/terraform.tfstate"
    acl          = "bucket-owner-full-control"
    region       = "us-east-1"
    role_arn     = "arn:aws:iam::443982649941:role/terraform"
    session_name = "ppe_use1_infra_platform"
  }
}
