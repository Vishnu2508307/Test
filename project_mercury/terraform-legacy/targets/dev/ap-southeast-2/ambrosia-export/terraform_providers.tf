provider "aws" {
  alias  = "ap_southeast_2"
  version = "2.70.0"
  region = "ap-southeast-2"
  max_retries = "3"

  assume_role {
    role_arn     = "${lookup(var.role_arn, "${var.ENV}")}"
    session_name = "dev_aero_mercury_export"
  }
}

provider "archive" {
  version = "1.3.0"
}

provider "null" {
  version = "2.1.2"
}
