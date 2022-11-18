provider "aws" {
   region = "us-west-1"
}

provider "aws" {
  alias  = "us_west_1"
  version = "2.70.0"
  region = "us-west-1"
  max_retries = "3"

  assume_role {
    role_arn     = "${lookup(var.role_arn, "${var.ENV}")}"
    session_name = "aero_mercury_deploy"
  }
}

provider "archive" {
  version = "1.3.0"
}

provider "null" {
  version = "2.1.2"
}
