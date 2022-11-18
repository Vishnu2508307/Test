#External Load balancer  / Public / internet facing load balanacer
#Create ALB
resource "aws_lb" "web_servers" {
  name               = "my_pub_alb"
  internal           = false
  load_balancer_type = "application"
  subnets            = aws_subnet.pubsub.id  
  security_groups    = [aws_security_group.public_sg.id]
  enable_deletion_protection = true

  access_logs {
    bucket  = "${aws_s3_bucket.lb_logs.bucket}" # this is an example
    enabled = true
  }
}

#Create Listener
resource "aws_lb_listener" "front_end" {
  load_balancer_arn = aws_lb.web_servers.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.web_servers.arn
  }
}


#Create Listener rule
resource "aws_lb_listener_rule" "forward" {
  listener_arn = aws_lb_listener.front_end.arn
  priority     = 90
 
  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.web_servers.arn
  }
 
  condition {
    path_pattern {
      values = ["/*"]
    }
  }
}

# Target group for the web servers
resource "aws_lb_target_group" "web_servers" {
  name     = "my_pub_alb"
  port     = 80
  protocol = "HTTP"
  vpc_id   = aws_vpc.my_vpc.id

  health_check {
    port                = "80"
    healthy_threshold   = 2
    interval            = 30
    protocol            = "HTTP"
    unhealthy_threshold = 2
  }
}

resource "aws_lb_target_group_attachment" "web_servers" {
  target_group_arn = aws_lb_target_group.web_servers.arn
  target_id        = aws_instance.pub_instance.id
  port             = 80
}