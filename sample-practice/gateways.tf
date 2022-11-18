#Internet Gateway
resource "aws_internet_gateway" "igw" {
vpc_id = aws_vpc.my_vpc.id
tags = {
Name = "my_igw"
}
}

#Elastic IP for NAT Gateway resource
resource "aws_eip" "nat" {
vpc = true
tags = {
Name = "my_nat" }
}

#NAT Gateway object and attachment of the Elastic IP Address from above
resource "aws_nat_gateway" "ngw" {
allocation_id = aws_eip.nat.id
subnet_id = aws_subnet.pubsub.id
depends_on = [aws_internet_gateway.igw]
tags = {
 Name = "my_ngw"
}
}

