#!/bin/bash

SSL_CERTFILE=etc/tls/dse/local/cassandra.pem \
cqlsh --ssl -u cassandra -p cassandra 192.168.192.201 --cqlversion '3.4.4' -f schemas/src/main/resources/cassandra/data/local-init.cql