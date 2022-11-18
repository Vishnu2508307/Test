provider "aws" {
  alias  = "us_east_1"
  version = "2.70.0"
  region = "us-east-1"
  max_retries = "3"

  assume_role {
    role_arn     = "${lookup(var.role_arn, "${var.ENV}")}"
    session_name = "ppe_aero_mercury_ext-http"
  }
}

provider "archive" {
  version = "1.3.0"
}

provider "null" {
  version = "2.1.2"
}
