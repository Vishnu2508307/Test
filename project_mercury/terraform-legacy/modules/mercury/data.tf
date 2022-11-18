data "aws_lb_target_group" "mercury" {
  count = "${var.desired_amount <= 0 ? 0:1}"
  name = "${var.attach_to_target_group}"
}

data "aws_iam_role" "mercury" {
  count = "${var.desired_amount <= 0 ? 0:1}"
  name = "${var.env}_aero_mercury_role"
}
