# Creating a New Key
resource "aws_key_pair" "Key-Pair" {
    key_name   = "MyKeyFinal"
    public_key = file("~/.ssh/authorized_keys")
  
 }