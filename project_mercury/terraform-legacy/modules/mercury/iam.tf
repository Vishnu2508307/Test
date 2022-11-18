data "aws_iam_policy_document" "ecs_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "manage_ecs_service" {
  statement {
		sid = "RegisterServiceToLB"
		effect = "Allow"
    actions = [
		  "ec2:Describe*",
      "elasticloadbalancing:DeregisterInstancesFromLoadBalancer",
      "elasticloadbalancing:DeregisterTargets",
      "elasticloadbalancing:Describe*",
      "elasticloadbalancing:RegisterInstancesWithLoadBalancer",
      "elasticloadbalancing:RegisterTargets"
		]
		resources = [
			"*"
		]
  }
}

resource "aws_iam_policy" "register_ecs_service" {
  count = "${(var.launch_type == "EC2" ? 1:0) * (var.desired_amount <= 0 ? 0:1)}"
  name     = "register_${var.deploy_to_cluster}_${local.mercury_container_name}_ecs_service_to_lb"
  path     = "/ecs/"
  policy   = "${data.aws_iam_policy_document.manage_ecs_service.json}"
}

resource "aws_iam_role" "ecs_service_role" {
  count = "${(var.launch_type == "EC2" ? 1:0) * (var.desired_amount <= 0 ? 0:1)}"
  name               = "manage_${var.deploy_to_cluster}_${local.mercury_container_name}_ecs_service_role"
  description        = "Role to register ${local.mercury_container_name} to a load balancer"
  assume_role_policy = "${data.aws_iam_policy_document.ecs_role.json}"

  lifecycle { 
    create_before_destroy = true 
  }
}

resource "aws_iam_policy_attachment" "attach_manage_ecs_service" {
  count = "${(var.launch_type == "EC2" ? 1:0) * (var.desired_amount <= 0 ? 0:1)}"
  name       = "attach_manage_ecs_service"
  roles      = ["${aws_iam_role.ecs_service_role.name}"]
  policy_arn = "${aws_iam_policy.register_ecs_service.arn}"
}

resource "aws_iam_instance_profile" "ecs_service" {
  count = "${(var.launch_type == "EC2" ? 1:0) * (var.desired_amount <= 0 ? 0:1)}"
  depends_on = ["aws_iam_role.ecs_service_role"]
  name       = "manage_${var.deploy_to_cluster}_${local.mercury_container_name}_ecs_service_role"
  role       = "${aws_iam_role.ecs_service_role.name}"
}

