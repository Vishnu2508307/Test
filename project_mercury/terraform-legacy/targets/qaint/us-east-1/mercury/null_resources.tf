# Wait for ECS service to be stable
resource "null_resource" "sts_assume_role" {
  triggers {
    mercury = "${module.app.mercury_service_task_definition}"
  }
  provisioner "local-exec" {
    command = "aws sts assume-role --role-arn '${lookup(var.role_arn, var.ENV)}' --role-session-name 'terraform-user' > ./.creds"
  }
}

resource "null_resource" "sleep" {
  depends_on = ["null_resource.sts_assume_role"]
  triggers {
    mercury = "${module.app.mercury_service_task_definition}"
  }
  provisioner "local-exec" {
    command = "echo disabled"
    #command = "sleep 600"
  }
}

resource "null_resource" "wait_for_stable_mercury_service" {
  depends_on = ["null_resource.sleep"]
  triggers {
    mercury = "${module.app.mercury_service_task_definition}"
  }
  provisioner "local-exec" {
    command = "echo disabled"
    #command = "AWS_ACCESS_KEY_ID=$(jq -r '.Credentials .AccessKeyId' ./.creds)  AWS_SECRET_ACCESS_KEY=$(jq -r '.Credentials .SecretAccessKey' ./.creds) AWS_SESSION_TOKEN=$(jq -r '.Credentials .SessionToken' ./.creds) AWS_DEFAULT_REGION='us-east-1' aws ecs wait services-stable --region 'us-east-1' --cluster ${var.deploy_to_cluster} --services ${module.app.mercury_service_name}"
  }
}
