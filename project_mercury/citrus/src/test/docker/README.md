
## Overview

This sub-project is intended to run a self contained test docker-compose that will run:

1. Cassandra nodes
2. Redis 
3. A cqlsh "runner" that will perform the pre-data initialization
4. An openjdk image that will applySchema and exec citrus tests.

## Requires

1. The `jetty/Dockerfile` built as a container named `bronte/mercury:test` 

## How

`$ make build` -- create all the necessary resources
`$ make test` -- run the docker-compose
`$ make teardown` -- teardown the docker-compose
`$ make logs` -- tail and follow the logs from the containers

## To test locally you may need to change the shell in docker_init to bash in the shebang
## Also may have to uncomment mounting cassandra data dir in cassandra_node1
