# ec2 in public subnet 
resource "aws_instance" "pub_instance" {
    ami = "ami-0162dd7febeafb455"
    instance_type = "t2.micro"
    subnet_id = aws_subnet.pubsub.id
    key_name = "MyKeyFinal"
    vpc_security_group_ids = aws_security_group.public_sg.id
    
    tags = {
        Name = "public_ec2_instance"
     }
}

# ec2 in Private subnet 
resource "aws_instance" "pri_instance" {
    ami = "ami-0162dd7febeafb455"
    instance_type = "t2.micro"
    subnet_id = aws_subnet.prisub.id
    key_name = "MyKeyFinal"
    vpc_security_group_ids = aws_security_group.private_sg.id
    
    tags = {
        Name = "private_ec2_instance"
     }
}
