#Creation of VPC
resource "aws_vpc" "my_vpc" { 
  cidr_block       = var.vpccidr
  instance_tenancy = "default"

  tags = {
    Name = "my_vpc"
  }
}

