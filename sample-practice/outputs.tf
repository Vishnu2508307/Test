output "vpc" {
    value = aws_vpc.my_vpc.id  
}

output "publicsubnet" {
    value = aws_subnet.pubsub.id
}

output "privatesubnet" {
    value = aws_subnet.prisub.id  
}

output "public_security_group_id" {
    value = aws_security_group.public_sg.id  
}

output "private_security_group_id" {
    value = aws_security_group.private_sg.id  
}