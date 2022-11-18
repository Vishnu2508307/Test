###
# Public Security Group
##
 
resource "aws_security_group" "public_sg" {
  name = "public_security_group"
  description = "Public internet access"
  vpc_id = aws_vpc.my_vpc.id
 
  tags = {
    Name        = "public_security_group"
    Role        = "public"
    Project     = "sample_project"
    ManagedBy   = "terraform"
  }
}


resource "aws_security_group_rule" "public_out" {
  type        = "egress"
  from_port   = 0
  to_port     = 0
  protocol    = "-1"
  cidr_blocks = ["0.0.0.0/0"]
 
  security_group_id = aws_security_group.public_sg.id
}
 
resource "aws_security_group_rule" "public_in_ssh" {
  type              = "ingress"
  from_port         = 22
  to_port           = 22
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.public_sg.id
}
 
resource "aws_security_group_rule" "public_in_http" {
  type              = "ingress"
  from_port         = 80
  to_port           = 80
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.public_sg.id
}
 
resource "aws_security_group_rule" "public_in_https" {
  type              = "ingress"
  from_port         = 443
  to_port           = 443
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.public_sg.id
}
 
###
# Private Security Group
##
 
resource "aws_security_group" "private_sg" {
  name = "private_security_group"
  description = "Private internet access"
  vpc_id = aws_vpc.my_vpc.id
 
  tags = {
    Name        = "private_security_group"
    Role        = "private"
    Project     = "sample_project"
    ManagedBy   = "terraform"
  }
}
 
resource "aws_security_group_rule" "private_out" {
  type        = "egress"
  from_port   = 0
  to_port     = 0
  protocol    = "-1"
  cidr_blocks = ["0.0.0.0/0"]
 
  security_group_id = aws_security_group.private_sg.id
}
 
resource "aws_security_group_rule" "private_in" {
  type              = "ingress"
  from_port         = 0
  to_port           = 65535
  protocol          = "-1"
  cidr_blocks = [aws_vpc.my_vpc.cidr_block]
 
  security_group_id = aws_security_group.private_sg.id
}