#Public Subnet 

resource "aws_subnet" "pubsub" {
    cidr_block = var.pubsubcidr
    vpc_id = aws_vpc.my_vpc.id
    map_public_ip_on_launch = true
    
    tags = {
    Name = "sub-pubsub"
    }
}

#Private Subnet 1
resource "aws_subnet" "prisub" {
    cidr_block = var.prisubcidr
    vpc_id = aws_vpc.my_vpc.id
    map_public_ip_on_launch = true
    
    tags = {
        Name = "sub-prisub"
    }
}
