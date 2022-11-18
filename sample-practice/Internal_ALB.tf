#Internal Load balancer  / Private facing load balanacer
#Create ALB
resource "aws_lb" "internal" {
  name               = "internal_alb"
  internal           = true
  load_balancer_type = "application"
  subnets            = aws_subnet.prisub.id  
  security_groups    = [aws_security_group.private_sg.id]
  enable_deletion_protection = true

  access_logs {
    bucket  = "${aws_s3_bucket.lb_logs.bucket}" # this is an example
    enabled = true
  }
}


#Create Listener
resource "aws_lb_listener" "internal_http" {
  load_balancer_arn = aws_lb.internals.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.internal_http.arn
  }
}


#Create Listener rule
resource "aws_lb_listener_rule" "internal_http" {
  listener_arn = aws_lb_listener.internal_http.arn
  priority     = 90
 
  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.internal.arn
  }
 
  condition {
    path_pattern {
      values = ["/*"]
    }
  }
}

# Target group for the web servers
resource "aws_lb_target_group" "internal" {
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

resource "aws_lb_target_group_attachment" "internal" {
  target_group_arn = aws_lb_target_group.internal.arn
  target_id        = aws_instance.pri_instance.id
  port             = 80
}