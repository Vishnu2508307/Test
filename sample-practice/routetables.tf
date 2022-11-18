 #Public Route Table
resource "aws_route_table" "routetablepublic" {
    vpc_id = aws_vpc.my_vpc.id
    
    route {
        cidr_block = "0.0.0.0/0"
        gateway_id = aws_internet_gateway.igw.id
}
    tags = {
        Name = "rt-pubrt"
}

}

 #Associate Public Route Table to Public Subnet
 resource "aws_route_table_association" "pub_rt" {
  subnet_id      = aws_subnet.pubsub.id
  route_table_id = aws_route_table.routetablepublic.id
}


#Private Route Table
resource "aws_route_table" "routetableprivate" {
    vpc_id = aws_vpc.my_vpc.id
    
    route {
        cidr_block = "0.0.0.0/0"
        gateway_id = aws_nat_gateway.ngw.id
    }

    tags = {
    Name = "rt-prirt"
    }
}


#Associate Private Route Table to Private Subnet
resource "aws_route_table_association" "pri_rt" {
  subnet_id      = aws_subnet.prisub.id
  route_table_id = aws_route_table.routetableprivate.id
}