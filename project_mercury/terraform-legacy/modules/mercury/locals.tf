locals {
  mercury_container_name                   = "${var.service_prefix_name == "" ? "mercury":"${var.service_prefix_name}-mercury"}"
  mercury_truststore_destination_file_path = "/opt/mercury/certs/truststore.jks"
}
