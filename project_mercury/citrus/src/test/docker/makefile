# copy the necessary artifacts to run the test; docker will modify them.
build:
    # setup cassandra node 1
	mkdir -pv ../../../build/docker/cassandra_node1/data/
	mkdir -pv ../../../build/docker/cassandra_node1/etc/
	cp -v cassandra.yaml ../../../build/docker/cassandra_node1/etc/
	cp -v cqlshrc ../../../build/docker/cassandra_node1/etc/
	cp -v ../../../../etc/tls/dse/sandbox/keystore.jks ../../../build/docker/cassandra_node1/etc/
	cp -v ../../../../etc/tls/dse/sandbox/cassandra.pem ../../../build/docker/cassandra_node1/etc/

	# setup cassandra init data runner to run the first alter
	mkdir -pv ../../../build/docker/cassandra_init/etc/
	cp -v cqlshrc ../../../build/docker/cassandra_init/etc/
	cp -v ../../../../etc/tls/dse/sandbox/cassandra.pem ../../../build/docker/cassandra_init/etc/
	cp -v ../../../../schemas/src/main/resources/cassandra/data/init-sandbox.cql ../../../build/docker/cassandra_init/

	# setup cassandra init data runner to apply the config
	cp -v ../../../../schemas/src/main/resources/cassandra/data/configuration-sandbox.cql ../../../build/docker/cassandra_init/
	cp -v ../../../../schemas/src/main/resources/cassandra/data/citrus-test-data.cql ../../../build/docker/cassandra_init/

test: build
	CITRUS_RUNNER_NUM=1 docker-compose up --force-recreate --exit-code-from citrus_runner

teardown:
	docker-compose down --remove-orphans

logs:
	docker-compose logs -f --tail="all" -t 

clean:
	rm -Rfv ../../../build/docker
